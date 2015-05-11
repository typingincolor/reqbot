package com.losd.reqbot.model;

import com.google.common.collect.ImmutableMap;

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
public class Response {
    Map<String, String> headers;
    String body;
    UUID uuid;

    private Response(Map<String, String> headers,
                    String body
    )
    {
        this.headers = headers;
        this.body = body;
        this.uuid = UUID.randomUUID();
    }

    public Map<String, String> getHeaders() {
        return new ImmutableMap.Builder<String, String>().putAll(this.headers).build();
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public UUID getUuid() {
        return uuid;
    }

    public static class Builder {
        Map<String, String> headers = new HashMap<>();
        String body;

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

        public Response build() {
            return new Response(headers, body);
        }
    }
}
