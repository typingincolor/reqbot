package com.losd.reqbot.repository;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
public class ResponseRedisRepo implements ResponseRepo {
    public static final String RESPONSE_KEY_PREFIX = "response:";
    public static final String TAG_PREFIX = "tag:";

    @Autowired
    StringRedisTemplate template;

    Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public Response get(String uuid) {
        String response = template.opsForValue().get(RESPONSE_KEY_PREFIX + uuid);

        return gson.fromJson(response, Response.class);
    }

    @Override
    public void save(Response response) {
        String key = RESPONSE_KEY_PREFIX + response.getUuid().toString();

        template.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                if (null == response.getTags() || response.getTags().size() == 0) {
                    operations.opsForList().leftPush(TAG_PREFIX + "none", key);
                } else {
                    response.getTags().forEach((tag) -> operations.opsForList().leftPush(TAG_PREFIX + tag, key));
                }

                operations.opsForValue().set(key, gson.toJson(response, Response.class));

                return operations.exec();
            }
        });
    }

    @Override
    public List<Response> getAll() {
        Set<String> keys = template.keys(RESPONSE_KEY_PREFIX + "*");
        List<Response> result = new LinkedList<>();

        keys.forEach((key) -> result.add(get(key.substring(RESPONSE_KEY_PREFIX.length()))));
        return ImmutableList.copyOf(result);
    }

    @Override
    public List<Response> getByTag(String tag) {
        String tagKey = TAG_PREFIX + tag;
        long length = template.opsForList().size(tagKey);

        List<String> keys = template.opsForList().range(tagKey, 0, length);

        List<Response> result = new LinkedList<>();

        keys.forEach((key) -> {
            String response = template.opsForValue().get(key);
            result.add(gson.fromJson(response, Response.class));
        });

        return ImmutableList.copyOf(result);
    }

    @Override
    public List<String> getTags() {
        Set<String> tags = template.keys(TAG_PREFIX + "*");
        List<String> result = new LinkedList<>();

        tags.forEach((tag) -> result.add(tag.substring(TAG_PREFIX.length())));
        Collections.sort(result);
        return ImmutableList.copyOf(result);
    }
}