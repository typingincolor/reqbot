package com.losd.reqbot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.config.JedisConfiguration;
import com.losd.reqbot.config.RedisSettings;
import com.losd.reqbot.config.RepoConfiguration;
import com.losd.reqbot.model.Request;
import com.losd.reqbot.test.IntegrationTest;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
@ContextConfiguration(classes = {RedisConfiguration.class, RepoConfiguration.class, JedisConfiguration.class, RedisSettings.class})
@Category(IntegrationTest.class)
public class RequestRedisRepoTest {
    @Autowired
    RequestRepo repo;

    @Autowired
    StringRedisTemplate template;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    private String bucket;

    @Before
    public void setup() {
        Set<String> keys = template.keys("*");
        template.delete(keys);

        // generate a bucket to put requests in
        bucket = RandomStringUtils.randomAlphabetic(10);
    }

    @Test
    public void testSave() throws
            Exception {
        Request request = buildRequest(bucket);
        repo.save(request);

        // check it worked
        String storedRequest = getRequest(request);
        Request result = gson.fromJson(storedRequest, Request.class);

        assertThat(result.getBody(), is(equalTo(request.getBody())));
        assertThat(result.getUuid(), is(equalTo(request.getUuid())));
        assertThat(result.getBucket(), is(equalTo(request.getBucket())));

        assertThat(getBucketLength(bucket), is(equalTo(1L)));
    }

    private Long getBucketLength(String bucket) {
        return template.opsForList().size(RequestRedisRepo.getBucketKey(bucket));
    }

    @Test
    public void testSaveStopsBucketGettingTooBig() {
        for (long i = 1; i < 10; i++) {
            Request request = buildRequest(bucket);
            repo.save(request);

            if (i < 3)
                assertThat(getBucketLength(bucket), is(equalTo(i)));
            else
                assertThat(getBucketLength(bucket), is(equalTo(3L)));

            Request savedRequest = gson.fromJson(getRequest(request), Request.class);

            assertThat(savedRequest.getUuid(), is(equalTo(request.getUuid())));
        }

        // expecting 4 keys, 3 for the request and 1 for the bucket
        assertThat(template.keys("*").size(), is(equalTo(4)));
    }

    private String getRequest(Request request) {
        return template.opsForValue().get(RequestRedisRepo.getRequestKey(request));
    }

    @Test
    public void testGetBucket() throws
            Exception {
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
        List<Request> result = repo.getByBucket(bucket);

        // check the results
        assertThat(result, hasSize(3));

        List<UUID> resultUuids = new ArrayList<>();
        result.forEach((res) -> resultUuids.add(res.getUuid()));

        assertThat(resultUuids, hasItems(testUuids.toArray(new UUID[testUuids.size()])));
    }

    @Test
    public void it_can_get_a_set_containing_all_of_the_buckets() {
        template.opsForList().leftPush(RequestRedisRepo.getBucketKey("a"), "element");
        template.opsForList().leftPush(RequestRedisRepo.getBucketKey("b"), "element");
        template.opsForList().leftPush(RequestRedisRepo.getBucketKey("c"), "element");
        template.opsForList().leftPush(RequestRedisRepo.getBucketKey("d"), "element");

        List<String> buckets = repo.getBuckets();
        assertThat(buckets, Matchers.hasSize(4));
        assertThat(buckets, hasItems("a", "b", "c", "d"));
    }

    private void putRequestInRedis(String bucket,
                                   Request request) {
        // a bucket is a list to which the uuid of the request is added
        template.opsForList().leftPush(RequestRedisRepo.getBucketKey(bucket), RequestRedisRepo.getRequestKey(request));

        // the request is store in redis against its uuid
        template.opsForValue().set(RequestRedisRepo.getRequestKey(request), gson.toJson(request));
    }

    private Request buildRequest(String bucket) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10));

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10));

        return new Request.Builder().bucket(bucket)
                .headers(headers)
                .body("body/n")
                .queryParameters(queryParameters)
                .method("POST")
                .path("/blah")
                .build();
    }
}
