package com.losd.reqbot.repository;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.LinkedHashSet;
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
public class BucketRedisRepo implements BucketRepo {
    private final static String BUCKET_KEY_PREFIX = "bucket:";

    @Autowired
    Jedis jedis = null;

    static String getBucketKey(String bucket) {
        return BUCKET_KEY_PREFIX + bucket;
    }

    @Override
    public Set<String> getBuckets() {
        Set<String> keys = jedis.keys(BUCKET_KEY_PREFIX + "*");
        Set<String> result = new LinkedHashSet<>(keys.size());

        keys.forEach((key) -> result.add(key.substring(BUCKET_KEY_PREFIX.length())));
        return result;
    }
}
