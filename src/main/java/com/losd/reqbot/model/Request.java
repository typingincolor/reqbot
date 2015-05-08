package com.losd.reqbot.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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
@SuppressWarnings("all")
public class Request {
    private String bucket;
    private Map<String, String> headers;
    private String body;
    private Map<String, String> queryParameters;
    private String method;
    private String timestamp;
    private UUID uuid;


    public Request(String bucket,
                   Map<String, String> headers,
                   String body,
                   Map<String, String> queryParameters,
                   String method
    )
    {
        this.bucket = bucket;
        this.headers = headers;
        this.body = body;
        this.queryParameters = queryParameters;
        this.method = method;
        this.timestamp = Instant.now().toString();
        this.uuid = UUID.randomUUID();
    }

    public String getBucket() {
        return bucket;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getMethod() {
        return method;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
