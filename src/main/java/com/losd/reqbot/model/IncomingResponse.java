package com.losd.reqbot.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class IncomingResponse {
    Map<String, String> headers;
    String body;
    List<String> tags;

    public IncomingResponse() {

    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public IncomingResponse(Map<String, String> headers, String body, List<String> tags) {
        this.headers = headers;
        this.body = body;
        this.tags = tags;
    }

    public Map<String, String> getHeaders() {
        if (this.headers == null) {
            return new ImmutableMap.Builder<String, String>().build();
        }
        return ImmutableMap.copyOf(this.headers);
    }

    public List<String> getTags() {
        if (this.tags == null) {
            return new ImmutableList.Builder<String>().build();
        }
        return ImmutableList.copyOf(this.tags);
    }

    public String getBody() {
        return body;
    }


    public static class Builder {
        Map<String, String> headers = new HashMap<>();
        String body;
        List<String> tags = new LinkedList<>();

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

        public Builder tags(List<String> t) {
            tags = t;
            return this;
        }

        public IncomingResponse build() {
            return new IncomingResponse(headers, body, tags);
        }
    }
}
