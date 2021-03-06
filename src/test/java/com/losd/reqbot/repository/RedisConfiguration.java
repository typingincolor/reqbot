package com.losd.reqbot.repository;

import com.losd.reqbot.config.RedisSettings;
import com.losd.reqbot.config.RequestSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
@Configuration
public class RedisConfiguration {
    @Bean
    RedisSettings redisSettings() {
        String host = System.getenv("REQBOT_REDIS_HOST");
        String password = System.getenv("REQBOT_REDIS_PASSWORD");
        String port = System.getenv("REQBOT_REDIS_PORT");

        int portInt = (port == null ? 0 : Integer.parseInt(port));

        RedisSettings settings = new RedisSettings();
        settings.setHost(host == null ? "localhost" : host);
        settings.setPort(portInt == 0 ? 6379 : portInt);
        settings.setPassword(password == null ? null : password);

        return settings;
    }

    @Bean
    RequestSettings requestSettings() {
        RequestSettings settings = new RequestSettings();
        settings.setQueueSize(3);

        return settings;
    }
}
