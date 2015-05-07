package com.losd.reqbot.controller;

import com.losd.reqbot.model.Request;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.TreeMap;

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

    @Autowired
    private ResponseRepo responseRepo = null;

    @RequestMapping(value = "/bucket/{bucket}", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unused")
    ResponseEntity<String> savePost(@PathVariable String bucket, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers, @RequestBody String body) {
        return handle(RequestMethod.POST, bucket, queryParams, headers, body);
    }

    @RequestMapping(value = "/bucket/{bucket}", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unused")
    ResponseEntity<String> saveGet(@PathVariable String bucket, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers) {
        return handle(RequestMethod.GET, bucket, queryParams, headers, null);
    }

    @RequestMapping(value = "/bucket/{bucket}/{responseKey}", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unused")
    ResponseEntity<String> getProgrammedResponse(@PathVariable String bucket, @PathVariable String responseKey, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers) {
        ResponseEntity<String> result = handle(RequestMethod.GET, bucket, queryParams, headers, null);

        Response response = responseRepo.get(responseKey);

        return new ResponseEntity<>(response.getBody(), result.getStatusCode());
    }

    @RequestMapping(value = "/bucket/{bucket}/{responseKey}", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unused")
    ResponseEntity<String> postProgrammedResponse(@PathVariable String bucket, @PathVariable String responseKey, @RequestParam Map<String, String> queryParams, @RequestHeader Map<String, String> headers, @RequestBody String body) {
        ResponseEntity<String> result = handle(RequestMethod.GET, bucket, queryParams, headers, body);

        Response response = responseRepo.get(responseKey);

        return new ResponseEntity<>(response.getBody(), result.getStatusCode());
    }

    private ResponseEntity<String> handle(RequestMethod method, String bucket, Map<String, String> queryParams, Map<String, String> headers, String body) {
        TreeMap<String, String> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveHeaders.putAll(headers);
        processGoSlowHeader(caseInsensitiveHeaders.get("X_REQBOT_GO_SLOW"));
        HttpStatus status = processHttpCodeHeader(caseInsensitiveHeaders.get("X_REQBOT_HTTP_CODE"));
        Request request = new Request(bucket, headers, body, queryParams, method.name());
        save(request);
        return new ResponseEntity<>(status.getReasonPhrase(), status);
    }

    private HttpStatus processHttpCodeHeader(String x_reqbot_http_code) {
        if (x_reqbot_http_code == null || x_reqbot_http_code.isEmpty()) {
            return HttpStatus.OK;
        }

        return HttpStatus.valueOf(Integer.parseInt(x_reqbot_http_code));
    }

    private void processGoSlowHeader(String x_reqbot_go_slow) {
        if (x_reqbot_go_slow == null || x_reqbot_go_slow.isEmpty()) {
            return;
        }

        try {
            Thread.sleep(Integer.parseInt(x_reqbot_go_slow));
        } catch (InterruptedException e) {
            throw new RuntimeException("InterruptedException", e);
        }
    }

    private void save(Request request) {
        requestRepo.save(request);
    }
}
