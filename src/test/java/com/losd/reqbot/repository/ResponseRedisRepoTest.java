package com.losd.reqbot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.config.JedisConfiguration;
import com.losd.reqbot.config.RepoConfiguration;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.test.IntegrationTest;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2015 Andrew Braithwaite
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RedisConfiguration.class, RepoConfiguration.class, JedisConfiguration.class})
@Category(IntegrationTest.class)
public class ResponseRedisRepoTest {
    @Autowired
    ResponseRepo repo;

    @Autowired
    JedisPool pool;

    Jedis jedis;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @Before
    public void setup() {
        jedis = pool.getResource();
        jedis.flushDB();
    }

    @After
    public void after() {
        jedis.close();
    }

    @Test
    public void it_gets() throws Exception {
        String headerKey = RandomStringUtils.randomAlphabetic(10);
        String headerValue = RandomStringUtils.randomAlphabetic(10);

        Response response = new Response.Builder().addHeader(headerKey, headerValue).build();

        jedis.set("response:" + response.getUuid().toString(), gson.toJson(response, Response.class));

        Response result = repo.get(response.getUuid().toString());

        // check it worked
        assertThat(result.getBody(), is(equalTo(response.getBody())));
        assertThat(result.getUuid(), is(equalTo(response.getUuid())));
        assertThat(result.getHeaders(), hasEntry(headerKey, headerValue));
    }

    @Test
    public void it_saves() throws Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "test-header-text")
                .tags(Arrays.asList("tag1", "tag2"))
                .body("testresponsebody")
                .build();

        repo.save(response);

        Response result = gson.fromJson(jedis.get("response:" + response.getUuid().toString()), Response.class);

        List<String> tag1 = jedis.lrange("tag:tag1", 0, jedis.llen(ResponseRedisRepo.TAG_PREFIX + "tag1"));
        List<String> tag2 = jedis.lrange("tag:tag2", 0, jedis.llen(ResponseRedisRepo.TAG_PREFIX + "tag2"));

        assertThat(result.getBody(), is(equalTo("testresponsebody")));
        assertThat(result.getHeaders(), hasEntry("test-header", "test-header-text"));
        assertThat(result.getHeaders().size(), is(equalTo(1)));
        assertThat(result.getTags(), hasSize(2));
        assertThat(result.getTags(), contains("tag1", "tag2"));
        assertThat(result.getUuid(), is(equalTo(response.getUuid())));

        assertThat(tag1, hasSize(1));
        assertThat(tag1, hasItem(ResponseRedisRepo.RESPONSE_KEY_PREFIX + response.getUuid()));

        assertThat(tag2, hasSize(1));
        assertThat(tag2, hasItem(ResponseRedisRepo.RESPONSE_KEY_PREFIX + response.getUuid()));
    }

    @Test
    public void it_handles_not_finding_anything() throws Exception {
        Response result = repo.get("rubbish");
        assertThat(result, is(nullValue()));
    }

    @Test
    public void it_saves_with_no_tags() throws Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "test-header-text")
                .body("testresponsebody")
                .build();

        repo.save(response);

        Response result = gson.fromJson(jedis.get(ResponseRedisRepo.RESPONSE_KEY_PREFIX + response.getUuid().toString()), Response.class);
        List<String> tag = jedis.lrange(ResponseRedisRepo.TAG_PREFIX + "none", 0, jedis.llen(ResponseRedisRepo.TAG_PREFIX + "none"));

        assertThat(result.getBody(), is(equalTo("testresponsebody")));
        assertThat(result.getHeaders(), hasEntry("test-header", "test-header-text"));
        assertThat(result.getHeaders().size(), is(equalTo(1)));
        assertThat(result.getTags(), hasSize(0));
        assertThat(result.getUuid(), is(equalTo(response.getUuid())));

        assertThat(tag, hasSize(1));
        assertThat(tag, hasItem(ResponseRedisRepo.RESPONSE_KEY_PREFIX + response.getUuid()));
    }

    @Test
    public void it_gets_all() throws Exception {
        for (int i = 0; i < 10; i++) {
            Response response = new Response.Builder()
                    .addHeader("header" + i, "value" + i)
                    .body("body" + i)
                    .build();

            jedis.set(ResponseRedisRepo.RESPONSE_KEY_PREFIX + response.getUuid(), gson.toJson(response, Response.class));
        }

        List<Response> result = repo.getAll();

        assertThat(result, hasSize(10));

        Set<String> bodiesSeen = new HashSet<>();

        result.forEach((res) -> {
            bodiesSeen.add(res.getBody());
            String index = res.getBody().substring(4);
            assertThat(res.getHeaders(), hasEntry("header" + index, "value" + index));
        });

        assertThat(bodiesSeen, hasSize(10));
    }

    @Test
    public void it_gets_all_by_tag() throws Exception {
        String[][] uuids = new String[2][5];

        for (int i = 0; i < 10; i++) {
            Response response = new Response.Builder()
                    .addHeader("header" + i, "value" + i)
                    .tags(Arrays.asList("tag" + i % 2))
                    .body("body" + i)
                    .build();

            uuids[i%2][i/2] = response.getUuid().toString();


            jedis.set("response:" + response.getUuid(), gson.toJson(response, Response.class));
            jedis.lpush(ResponseRedisRepo.TAG_PREFIX + "tag" + i % 2, ResponseRedisRepo.RESPONSE_KEY_PREFIX + response.getUuid().toString());
        }

        List<Response> tag1Result = repo.getByTag("tag1");
        assertThat(tag1Result, hasSize(5));
        List<String> tag1Uuids = new LinkedList<>();
        tag1Result.forEach((r) -> tag1Uuids.add(r.getUuid().toString()));
        assertThat(tag1Uuids, containsInAnyOrder(uuids[1][0], uuids[1][1], uuids[1][2], uuids[1][3], uuids[1][4]));
    }

    @Test
    public void it_gets_a_list_of_buckets() throws Exception {
        jedis.lpush(ResponseRedisRepo.TAG_PREFIX + "tag1", "a");
        jedis.lpush(ResponseRedisRepo.TAG_PREFIX + "tag2", "b");
        jedis.lpush(ResponseRedisRepo.TAG_PREFIX + "tag3", "c");
        jedis.lpush(ResponseRedisRepo.TAG_PREFIX + "tag4", "d");

        List<String> tags = repo.getTags();

        assertThat(tags, hasSize(4));
        assertThat(tags, containsInAnyOrder("tag1", "tag2", "tag3", "tag4"));
    }

    @Test
    public void it_handles_there_being_no_tags() throws Exception {
        List<String> tags = repo.getTags();
        assertThat(tags, hasSize(0));
    }
}

