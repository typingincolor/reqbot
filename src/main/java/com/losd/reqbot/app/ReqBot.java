package com.losd.reqbot.app;

import com.losd.reqbot.controller.ReqBotController;
import com.losd.reqbot.model.Request;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

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
public class ReqBot {
    public static void main(String[] args) {
        Route route = (req, res) -> {
            String bucket = req.params(":bucket");
            Map<String, String> headers = new HashMap<>();
            Map<String, String> queryParams = new HashMap<>();

            req.headers()
               .forEach(header ->
                                headers.put(header, req.headers(header))
               );

            req.queryParams()
               .forEach(queryParam ->
                                queryParams.put(queryParam, req.queryParams(queryParam))
               );

            Request request = new Request(bucket, headers, req.body(), queryParams, req.pathInfo(), req.requestMethod());

            ReqBotController controller = new ReqBotController();
            controller.handle(request);

            return "Hello World " + bucket;
        };

        get("/:bucket", route);
        post("/:bucket", route);
    }
}
