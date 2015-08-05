package com.losd.reqbot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.HashMap;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {
    private String bucket;
    private Map<String, String> headers;
    private String body;
    private Map<String, String> queryParameters;
    private String method;
    private String timestamp;
    private UUID uuid;
    private String path;

    private Request(String bucket,
                   Map<String, String> headers,
                   String body,
                   Map<String, String> queryParameters,
                   String method,
                   String path
    )
    {
        this.bucket = bucket;
        this.headers = headers;
        this.body = body;
        this.queryParameters = queryParameters;
        this.method = method;
        this.timestamp = Instant.now().toString();
        this.uuid = UUID.randomUUID();
        this.path = path;

    }

    public static class Builder {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        String body;
        String method;
        String path;
        String bucket;

        public Builder bucket(String b) {
            bucket = b;
            return this;
        }

        public Builder path(String p) {
            path = p;
            return this;
        }

        public Builder method(String m) {
            method = m;
            return this;
        }

        public Builder addQueryParameters(String qp, String value) {
            queryParameters.put(qp, value);
            return this;
        }

        public Builder queryParameters(Map<String, String> qp) {
            queryParameters.putAll(qp);
            return this;
        }

        public Builder addHeader(String header, String value) {
            headers.put(header, value);
            return this;
        }

        public Builder headers(Map<String, String> h) {
            headers.putAll(h);
            return this;
        }

        public Builder body(String b) {
            body = b;
            return this;
        }

        public Request build() {
            return new Request(bucket, headers, body, queryParameters, method, path);
        }
    }

    public String getBucket() {
        return bucket;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, String> getHeaders() {
        return ImmutableMap.copyOf(this.headers);
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQueryParameters() {
        return ImmutableMap.copyOf(this.queryParameters);
    }

    public String getMethod() {
        return method;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}
