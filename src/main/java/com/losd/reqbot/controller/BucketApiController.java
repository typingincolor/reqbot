package com.losd.reqbot.controller;

import com.losd.reqbot.constant.ReqbotHttpHeaders;
import com.losd.reqbot.model.Request;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
public class BucketApiController {

    @Autowired
    private RequestRepo requestRepo = null;

    @Autowired
    private ResponseRepo responseRepo = null;

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}", method = RequestMethod.POST)
    ResponseEntity<String> standardPostResponse(@PathVariable String bucket,
                                                @RequestParam Map<String, String> queryParams,
                                                @RequestHeader Map<String, String> headers,
                                                @RequestBody String body)
    {
        return handleRequest(RequestMethod.POST, bucket, queryParams, headers, body);
    }

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}", method = RequestMethod.GET)
    ResponseEntity<String> standardGetResponse(@PathVariable String bucket,
                                               @RequestParam Map<String, String> queryParams,
                                               @RequestHeader Map<String, String> headers)
    {
        return handleRequest(RequestMethod.GET, bucket, queryParams, headers, null);
    }

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}/{responseKey}", method = RequestMethod.GET)
    ResponseEntity<String> programmedGetResponse(@PathVariable String bucket,
                                                 @PathVariable String responseKey,
                                                 @RequestParam Map<String, String> queryParams,
                                                 @RequestHeader Map<String, String> headers)
    {
        headers.put(ReqbotHttpHeaders.RESPONSE, responseKey);
        return handleRequest(RequestMethod.GET, bucket, queryParams, headers, null);
    }

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}/{responseKey}", method = RequestMethod.POST)
    ResponseEntity<String> programmedPostResponse(@PathVariable String bucket,
                                                  @PathVariable String responseKey,
                                                  @RequestParam Map<String, String> queryParams,
                                                  @RequestHeader Map<String, String> headers,
                                                  @RequestBody String body)
    {
        headers.put(ReqbotHttpHeaders.RESPONSE, responseKey);
        return handleRequest(RequestMethod.GET, bucket, queryParams, headers, body);
    }

    private ResponseEntity<String> handleRequest(RequestMethod method,
                                                 String bucket,
                                                 Map<String, String> queryParams,
                                                 Map<String, String> headers,
                                                 String body)
    {
        saveRequest(method, bucket, queryParams, headers, body);

        TreeMap<String, String> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveHeaders.putAll(headers);
        processGoSlowHeader(caseInsensitiveHeaders.get(ReqbotHttpHeaders.GO_SLOW));
        HttpStatus status = processHttpCodeHeader(caseInsensitiveHeaders.get(ReqbotHttpHeaders.HTTP_CODE));

        Response response = new Response(status);

        HttpHeaders resultHeaders = new HttpHeaders();

        if (!isResponseHeaderSet(caseInsensitiveHeaders)) {
            response = responseRepo.get(caseInsensitiveHeaders.get(ReqbotHttpHeaders.RESPONSE));
            response.getHeaders().forEach(resultHeaders::add);
        }

        return new ResponseEntity<>(response.getBody(), resultHeaders, status);
    }

    private boolean isResponseHeaderSet(TreeMap<String, String> caseInsensitiveHeaders) {
        return caseInsensitiveHeaders.get(ReqbotHttpHeaders.RESPONSE) == null;
    }

    private void saveRequest(RequestMethod method,
                             String bucket,
                             Map<String, String> queryParams,
                             Map<String, String> headers,
                             String body)
    {
        save(new Request(bucket, headers, body, queryParams, method.name()));
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
        }
        catch (InterruptedException e) {
            throw new RuntimeException("InterruptedException", e);
        }
    }

    private void save(Request request) {
        requestRepo.save(request);
    }
}
