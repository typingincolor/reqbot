package com.losd.reqbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

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
public class ReqbotLifeCycle implements SmartLifecycle {
    Logger logger = LoggerFactory.getLogger(ReqbotLifeCycle.class);

    @Autowired
    JedisPool pool;

    public boolean isAutoStartup() {
        logger.debug("isAutoStartup()");
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        logger.debug("stop(Runnable callback)");

        if (pool != null) {
            logger.info("Destroying jedis pool");
            pool.destroy();
        }

        callback.run();
    }

    @Override
    public void start() {
        logger.debug("start()");
    }

    @Override
    public void stop() {
        logger.debug("stop()");
    }

    @Override
    public boolean isRunning() {
        logger.debug("isRunning()");
        return true;
    }

    @Override
    public int getPhase() {
        logger.debug("getPhase()");
        return 0;
    }
}
