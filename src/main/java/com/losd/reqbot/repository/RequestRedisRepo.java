package com.losd.reqbot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.model.Request;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

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
    @Override
    public void save(Request request) {
        Gson gson = new GsonBuilder().create();
        Jedis jedis = new Jedis("localhost");
        jedis.lpush(request.getBucket(), gson.toJson(request));
        jedis.ltrim(request.getBucket(), 0, 5);
    }

    @Override
    public List<Request> getBucket(String bucket) {
        List<Request> result = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        Jedis jedis = new Jedis("localhost");
        List<String> requests = jedis.lrange(bucket, 0, 5);

        requests.forEach(request -> result.add(gson.fromJson(request, Request.class)));
        return result;
    }
}