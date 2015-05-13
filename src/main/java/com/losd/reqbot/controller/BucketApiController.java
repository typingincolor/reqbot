package com.losd.reqbot.controller;

import com.losd.reqbot.constant.ReqbotHttpHeaders;
import com.losd.reqbot.model.Request;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
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
    Logger logger = LoggerFactory.getLogger(BucketApiController.class);

    @Autowired
    private RequestRepo requestRepo = null;

    @Autowired
    private ResponseRepo responseRepo = null;

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}/response/{responseKey}", method = RequestMethod.GET)
    ResponseEntity<String> programmedGetResponse(@PathVariable String bucket,
                                                 @PathVariable String responseKey,
                                                 @RequestParam Map<String, String> queryParams,
                                                 @RequestHeader Map<String, String> headers,
                                                 HttpServletRequest request) {
        logger.info("GET /bucket/{}/response/{}", bucket, responseKey);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        headers.put(ReqbotHttpHeaders.RESPONSE, responseKey);
        return handleRequest(request.getMethod(), bucket, queryParams, headers, null, path);
    }

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}/response/{responseKey}", method = RequestMethod.POST)
    ResponseEntity<String> programmedPostResponse(@PathVariable String bucket,
                                                  @PathVariable String responseKey,
                                                  @RequestParam Map<String, String> queryParams,
                                                  @RequestHeader Map<String, String> headers,
                                                  @RequestBody String body,
                                                  HttpServletRequest request) {
        logger.info("POST /bucket/{}/response/{}", bucket, responseKey);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        headers.put(ReqbotHttpHeaders.RESPONSE, responseKey);
        return handleRequest(request.getMethod(), bucket, queryParams, headers, body, path);
    }

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}/**", method = RequestMethod.POST)
    ResponseEntity<String> standardPostResponse(@PathVariable String bucket,
                                                @RequestParam Map<String, String> queryParams,
                                                @RequestHeader Map<String, String> headers,
                                                @RequestBody String body,
                                                HttpServletRequest request) {
        logger.info("POST /bucket/{}", bucket);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        return handleRequest(request.getMethod(), bucket, queryParams, headers, body, path);
    }

    @ResponseBody
    @RequestMapping(value = "/bucket/{bucket}/**", method = RequestMethod.GET)
    ResponseEntity<String> standardGetResponse(@PathVariable String bucket,
                                               @RequestParam Map<String, String> queryParams,
                                               @RequestHeader Map<String, String> headers,
                                               HttpServletRequest request) {
        logger.info("GET /bucket/{}", bucket);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        return handleRequest(request.getMethod(), bucket, queryParams, headers, null, path);
    }

    private ResponseEntity<String> handleRequest(String method,
                                                 String bucket,
                                                 Map<String, String> queryParams,
                                                 Map<String, String> headers,
                                                 String body,
                                                 String path) {
        saveRequest(method, bucket, queryParams, headers, body, path);

        TreeMap<String, String> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveHeaders.putAll(headers);
        processGoSlowHeader(caseInsensitiveHeaders.get(ReqbotHttpHeaders.GO_SLOW));
        HttpStatus status = processHttpCodeHeader(caseInsensitiveHeaders.get(ReqbotHttpHeaders.HTTP_CODE));

        Response response = new Response.Builder().body(status.getReasonPhrase()).build();

        HttpHeaders resultHeaders = new HttpHeaders();

        if (!isResponseHeaderSet(caseInsensitiveHeaders)) {
            response = responseRepo.get(caseInsensitiveHeaders.get(ReqbotHttpHeaders.RESPONSE));

            if (response == null) {
                throw new ResponseNotFoundException();
            }
            response.getHeaders().forEach(resultHeaders::add);
        }

        return new ResponseEntity<>(response.getBody(), resultHeaders, status);
    }

    private boolean isResponseHeaderSet(TreeMap<String, String> caseInsensitiveHeaders) {
        return caseInsensitiveHeaders.get(ReqbotHttpHeaders.RESPONSE) == null;
    }

    private void saveRequest(String method,
                             String bucket,
                             Map<String, String> queryParams,
                             Map<String, String> headers,
                             String body,
                             String path)
    {
        Request request = new Request.Builder()
                .bucket(bucket)
                .headers(headers)
                .body(body)
                .queryParameters(queryParams)
                .method(method)
                .path(path)
                .build();

        save(request);
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

    @ExceptionHandler(ResponseNotFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleResponseNotFound(ResponseNotFoundException e) {
        return "Response Not Found";
    }


    static class ResponseNotFoundException extends RuntimeException {

    }
}
