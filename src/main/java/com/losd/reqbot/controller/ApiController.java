package com.losd.reqbot.controller;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.losd.reqbot.constant.ReqbotHttpHeaders;
import com.losd.reqbot.model.IncomingResponse;
import com.losd.reqbot.model.Request;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
public class ApiController {
    Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private RequestRepo requestRepo = null;

    @Autowired
    private ResponseRepo responseRepo = null;

    @ResponseBody
    @RequestMapping(value = "/buckets/{bucket}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    List<Request> getRequestsByBucket(@PathVariable String bucket) {
        logger.info("GET /buckets/{}", bucket);
        return requestRepo.getByBucket(bucket);
    }

    @ResponseBody
    @RequestMapping(value = "/buckets", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    List<String> getBuckets() {
        logger.info("GET /buckets");
        return requestRepo.getBuckets();
    }

    @ResponseBody
    @RequestMapping(value = "/tags", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    List<String> getTags() {
        logger.info("GET /tags");
        List<String> tags = responseRepo.getTags();

        if (tags.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        return tags;
    }

    @ResponseBody
    @RequestMapping(value = "/tags/{tag}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    List<Response> getResponsesByTag(@PathVariable String tag) {
        logger.info("GET /tags/{}", tag);
        return responseRepo.getByTag(tag);
    }

    @ResponseBody
    @RequestMapping(value = "/responses/{responseKey}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    Response getResponse(@PathVariable String responseKey) {
        logger.info("GET /responses/{}", responseKey);
        Optional<Response> result = Optional.fromNullable(responseRepo.get(responseKey));

        if (!result.isPresent()) {
            throw new ResourceNotFoundException();
        }

        return result.get();
    }

    @ResponseBody
    @RequestMapping(value = "/responses", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response saveResponse(@RequestBody IncomingResponse incoming) {
        logger.info("POST /responses");
        if (Strings.isNullOrEmpty(incoming.getBody())) {
            throw new IncomingEmptyBodyException();
        }

        Map<String, String> headers = incoming.getHeaders();
        List<String> tags = incoming.getTags();
        String body = incoming.getBody();
        Response response = new Response.Builder().headers(headers).tags(tags).body(body).build();
        responseRepo.save(response);

        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/{bucket}/response/{responseKey}", method = RequestMethod.GET)
    ResponseEntity<String> requestWithResponse(@PathVariable String bucket,
                                                 @PathVariable String responseKey,
                                                 @RequestParam Map<String, String> queryParams,
                                                 @RequestHeader Map<String, String> headers,
                                                 HttpServletRequest request) {
        logger.info("GET /{}/response/{}", bucket, responseKey);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        headers.put(ReqbotHttpHeaders.RESPONSE, responseKey);
        return handleRequest(request.getMethod(), bucket, queryParams, headers, null, path);
    }

    @ResponseBody
    @RequestMapping(value = "/{bucket}/response/{responseKey}", method = RequestMethod.POST)
    ResponseEntity<String> requestWithResponse(@PathVariable String bucket,
                                                  @PathVariable String responseKey,
                                                  @RequestParam Map<String, String> queryParams,
                                                  @RequestHeader Map<String, String> headers,
                                                  @RequestBody String body,
                                                  HttpServletRequest request) {
        logger.info("POST /{}/response/{}", bucket, responseKey);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        headers.put(ReqbotHttpHeaders.RESPONSE, responseKey);
        return handleRequest(request.getMethod(), bucket, queryParams, headers, body, path);
    }

    @ResponseBody
    @RequestMapping(value = "/{bucket}/**", method = RequestMethod.POST)
    ResponseEntity<String> request(@PathVariable String bucket,
                                                @RequestParam Map<String, String> queryParams,
                                                @RequestHeader Map<String, String> headers,
                                                @RequestBody String body,
                                                HttpServletRequest request) {
        logger.info("POST /{}", bucket);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        return handleRequest(request.getMethod(), bucket, queryParams, headers, body, path);
    }

    @ResponseBody
    @RequestMapping(value = "/{bucket}/**", method = RequestMethod.GET)
    ResponseEntity<String> request(@PathVariable String bucket,
                                               @RequestParam Map<String, String> queryParams,
                                               @RequestHeader Map<String, String> headers,
                                               HttpServletRequest request) {
        logger.info("GET /{}", bucket);
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        return handleRequest(request.getMethod(), bucket, queryParams, headers, null, path);
    }

    private ResponseEntity<String> handleRequest(String method,
                                                String bucket,
                                                Map<String, String> queryParams,
                                                Map<String, String> headers,
                                                String body,
                                                String path)
    {
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
                throw new UnableToReturnRequestedResponse();
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
        }
        catch (InterruptedException e) {
            throw new RuntimeException("InterruptedException", e);
        }
    }

    private void save(Request request) {
        requestRepo.save(request);
    }
}
