package com.losd.reqbot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.config.JedisConfiguration;
import com.losd.reqbot.config.RepoConfiguration;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.test.IntegrationTest;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    Jedis jedis;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @Before
    public void setup() {
        jedis.flushDB();
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
                .body("testresponsebody")
                .build();

        repo.save(response);

        Response result = gson.fromJson(jedis.get("response:" + response.getUuid().toString()), Response.class);

        assertThat(result.getBody(), is(equalTo("testresponsebody")));
        assertThat(result.getHeaders(), hasEntry("test-header", "test-header-text"));
        assertThat(result.getHeaders().size(), is(equalTo(1)));
        assertThat(result.getUuid(), is(equalTo(response.getUuid())));
    }
}
