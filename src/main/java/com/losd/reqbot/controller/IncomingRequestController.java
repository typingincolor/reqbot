package com.losd.reqbot.controller;

import com.losd.reqbot.model.KeyValuePair;
import com.losd.reqbot.model.Request;
import com.losd.reqbot.repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
@Controller
public class IncomingRequestController {
    @Autowired
    private RequestRepo repo = null;

    @RequestMapping(value = "/{bucket}", method = RequestMethod.POST)
    @ResponseBody
    String savePost(@PathVariable String bucket, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers, @RequestBody String body) {
        return handle(RequestMethod.GET, bucket, queryParams, headers, body);
    }

    @RequestMapping(value = "/{bucket}", method = RequestMethod.GET)
    @ResponseBody
    String saveGet(@PathVariable String bucket, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers) {

        return handle(RequestMethod.GET, bucket, queryParams, headers, null);
    }

    private String handle(RequestMethod method, String bucket, Map<String, String> queryParams, Map<String, String> headers, String body) {
        List<KeyValuePair> headerList = new ArrayList<>();
        List<KeyValuePair> queryParmList = new ArrayList<>();

        headers.forEach((key, value) -> headerList.add(new KeyValuePair(key, value)));
        queryParams.forEach((key, value) -> queryParmList.add(new KeyValuePair(key, value)));

        Request request = new Request(bucket, headerList, body, queryParmList, method.name());
        save(request);

        return "OK";
    }

    private void save(Request request) {
        repo.save(request);
    }
}
