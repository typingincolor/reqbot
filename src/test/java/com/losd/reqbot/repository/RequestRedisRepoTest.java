package com.losd.reqbot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.config.JedisConfiguration;
import com.losd.reqbot.config.RepoConfiguration;
import com.losd.reqbot.model.Request;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

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
public class RequestRedisRepoTest {
    @Autowired
    RequestRepo repo;

    @Autowired
    Jedis jedis;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @Before
    public void setup() {
        jedis.flushAll();
    }

    @Test
    public void testGetBucket() throws Exception {
        // generate a bucket to put requests in
        String bucket = RandomStringUtils.randomAlphabetic(10);

        // generate some requests
        Request request1 = buildRequest(bucket);
        Request request2 = buildRequest(bucket);
        Request request3 = buildRequest(bucket);

        // store the requests in the bucket
        putRequestInRedis(bucket, request1);
        putRequestInRedis(bucket, request2);
        putRequestInRedis(bucket, request3);

        // get a list of the uuids of the test requests
        List<UUID> testUuids = new ArrayList<>(Arrays.asList(request1.getUuid(), request2.getUuid(), request3.getUuid()));

        // run the test
        List<Request> result = repo.getBucket(bucket);

        // check the results
        assertThat(result, hasSize(3));

        List<UUID> resultUuids = new ArrayList<>();
        result.forEach((res) -> resultUuids.add(res.getUuid()));

        assertThat(resultUuids, hasItems(testUuids.toArray(new UUID[testUuids.size()])));
    }

    private void putRequestInRedis(String bucket, Request request) {
        // a bucket is a list to which the uuid of the request is added
        jedis.lpush(bucket, request.getUuid().toString());

        // the request is store in redis against its uuid
        jedis.set(request.getUuid().toString(), gson.toJson(request));
    }

    private Request buildRequest(String bucket) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10));

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10));

        return new Request(bucket, headers, "body/n", queryParameters, "POST");
    }
}
