package com.losd.reqbot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.config.RequestSettings;
import com.losd.reqbot.model.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;

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
@Component
public class RequestRedisRepo implements RequestRepo {
    @Autowired
    RequestSettings settings;

    @Autowired
    Jedis jedis = null;

    @Override
    public void save(Request request) {
        int queueSize = settings.getQueueSize();

        Gson gson = new GsonBuilder().serializeNulls().create();

        Transaction t = jedis.multi();
        Response<String> key = t.lindex(request.getBucket(), queueSize - 1);
        t.lpush(request.getBucket(), request.getUuid().toString());
        t.set(request.getUuid().toString(), gson.toJson(request));
        t.ltrim(request.getBucket(), 0, queueSize - 1);
        t.exec();

        if (key.get() != null)
        {
            jedis.del(key.get());
        }
    }

    @Override
    public List<Request> getBucket(String bucket) {
        int queueSize = settings.getQueueSize();

        List<Request> result = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        List<String> requests = jedis.lrange(bucket, 0, queueSize - 1);

        requests.forEach(request -> {
            String body = jedis.get(request);
            result.add(gson.fromJson(body, Request.class));
        });

        return result;
    }
}
