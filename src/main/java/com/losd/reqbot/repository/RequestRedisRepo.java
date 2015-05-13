package com.losd.reqbot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.config.RequestSettings;
import com.losd.reqbot.model.Request;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.*;

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
public class RequestRedisRepo implements RequestRepo {
    private static final String BUCKET_KEY_PREFIX = "bucket:";
    public static final String REQUEST_KEY_PREFIX = "request:";

    @Autowired
    RequestSettings settings;

    @Autowired
    Jedis jedis = null;

    Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public void save(Request request) {
        int queueSize = settings.getQueueSize();

        Transaction t = jedis.multi();
        Response<String> key = t.lindex(getBucketKey(request.getBucket()), queueSize - 1);
        t.lpush(getBucketKey(request.getBucket()), getRequestKey(request));
        t.set(getRequestKey(request), gson.toJson(request));
        t.ltrim(getBucketKey(request.getBucket()), 0, queueSize - 1);
        t.exec();

        if (key.get() != null) {
            jedis.del(key.get());
        }
    }

    @Override
    public List<Request> getByBucket(String bucket) {
        int queueSize = settings.getQueueSize();

        List<Request> result = new ArrayList<>();

        List<String> requests = jedis.lrange(getBucketKey(bucket), 0, queueSize - 1);

        requests.forEach(request -> {
            String body = jedis.get(request);
            result.add(gson.fromJson(body, Request.class));
        });

        return result;
    }

    @Override
    public List<String> getBuckets() {
        Set<String> keys = jedis.keys(BUCKET_KEY_PREFIX + "*");
        List<String> result = new LinkedList<String>();

        keys.forEach((key) -> result.add(key.substring(BUCKET_KEY_PREFIX.length())));

        Collections.sort(result);
        return result;
    }

    static String getRequestKey(Request request) {
        return REQUEST_KEY_PREFIX + request.getUuid();
    }

    static String getBucketKey(String bucket) {
        return BUCKET_KEY_PREFIX + bucket;
    }
}
