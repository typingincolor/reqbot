package com.losd.reqbot.controller;

import com.losd.reqbot.model.Request;
import com.losd.reqbot.repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RestController
public class IncomingRequestController {
    @Autowired
    private RequestRepo requestRepo = null;

    @RequestMapping(value = "/bucket/{bucket}", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> savePost(@PathVariable String bucket, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers, @RequestBody String body) {
        handle(RequestMethod.POST, bucket, queryParams, headers, body);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/bucket/{bucket}", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<String> saveGet(@PathVariable String bucket, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers) {
        handle(RequestMethod.GET, bucket, queryParams, headers, null);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private void handle(RequestMethod method, String bucket, Map<String, String> queryParams, Map<String, String> headers, String body) {
        Request request = new Request(bucket, headers, body, queryParams, method.name());
        save(request);
    }

    private void save(Request request) {
        requestRepo.save(request);
    }
}
