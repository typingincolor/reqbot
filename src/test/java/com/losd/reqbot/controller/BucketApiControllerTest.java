package com.losd.reqbot.controller;

import com.losd.reqbot.constant.ReqbotHttpHeaders;
import com.losd.reqbot.model.Request;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.it.modular.hamcrest.date.Moments;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.co.it.modular.hamcrest.date.IsWithin.within;

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
public class BucketApiControllerTest {
    private MockMvc mockMvc;

    @Mock
    private RequestRepo requestRepo;

    @Mock
    private ResponseRepo responseRepo;

    @InjectMocks
    private BucketApiController bucketApiController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bucketApiController).build();
    }

    @Test
    public void it_handles_a_get() throws
            Exception {
        String path = "/bucket/x";
        mockMvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        validate("x", Collections.emptyMap(), RequestMethod.GET, null, path);
    }

    @Test
    public void it_handles_a_get_with_a_path() throws
            Exception {
        String path = "/bucket/x/a/path/to/somewhere";
        mockMvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        validate("x", Collections.emptyMap(), RequestMethod.GET, null, path);
    }

    @Test
    public void it_handles_a_post() throws
            Exception {
        String path = "/bucket/x";
        mockMvc.perform(post(path).content("hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        validate("x", Collections.emptyMap(), RequestMethod.POST, "hello", path);
    }

    @Test
    public void it_handles_a_post_with_a_path() throws
            Exception {
        String path = "/bucket/x/a/path/to/somewhere";
        mockMvc.perform(post(path).content("hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        validate("x", Collections.emptyMap(), RequestMethod.POST, "hello", path);
    }

    @Test
    public void it_handles_a_get_with_query_parameters() throws
            Exception {
        String path = "/bucket/x";
        mockMvc.perform(get(path + "?a=1")).andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("a", "1");

        validate("x", queryParams, RequestMethod.GET, null, path);
    }

    @Test
    public void it_handles_a_post_with_query_parameters() throws
            Exception {
        String path = "/bucket/x";
        mockMvc.perform(post(path + "?a=1").content("hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("a", "1");

        validate("x", queryParams, RequestMethod.POST, "hello", path);
    }

    @Test
    public void it_goes_slow_when_asked() throws
            Exception {
        Instant start = Instant.now();
        mockMvc.perform(get("/bucket/x").header(ReqbotHttpHeaders.GO_SLOW, 5000))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        Instant end = Instant.now();

        assertThat(Duration.between(start, end).toMillis(), is(greaterThan(5000L)));
    }

    @Test
    public void it_returns_the_requested_http_status_code() throws
            Exception {
        mockMvc.perform(get("/bucket/x").header(ReqbotHttpHeaders.HTTP_CODE, 404))
                .andExpect(status().isNotFound())
                .andExpect(content().string(HttpStatus.NOT_FOUND.getReasonPhrase()));
    }

    @Test
    public void it_returns_the_correct_http_status_code_when_the_header_is_mixed_case() throws
            Exception {
        mockMvc.perform(get("/bucket/x").header("X-ReQbOt-http-CODE", 404))
                .andExpect(status().isNotFound())
                .andExpect(content().string(HttpStatus.NOT_FOUND.getReasonPhrase()));
    }

    @Test
    public void it_returns_a_http_ok_response_code_when_http_code_header_is_empty() throws
            Exception {
        mockMvc.perform(get("/bucket/x").header("X-ReQbOt-http-CODE", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatus.OK.getReasonPhrase()));
    }

    @Test
    public void it_returns_the_requested_response_body_for_a_get() throws
            Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "testvalue")
                .body(RandomStringUtils.randomAlphanumeric(30))
                .build();

        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        String path = "/bucket/x/response/" + response.getUuid();
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().string(response.getBody()))
                .andExpect(header().string("test-header", "testvalue"));

        validate("x", Collections.emptyMap(), RequestMethod.GET, null, path);
    }

    @Test
    public void it_returns_the_requested_response_body_for_a_post() throws
            Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "testvalue")
                .body(RandomStringUtils.randomAlphanumeric(30))
                .build();

        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        String path = "/bucket/x/response/" + response.getUuid();
        mockMvc.perform(post(path).content("hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(response.getBody()))
                .andExpect(header().string("test-header", "testvalue"));

        validate("x", Collections.emptyMap(), RequestMethod.POST, "hello", path);
    }

    @Test
    public void it_returns_the_requested_body_for_a_get_using_header() throws
            Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "testvalue")
                .body(RandomStringUtils.randomAlphanumeric(30))
                .build();

        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        String path = "/bucket/x";
        mockMvc.perform(get(path).header(ReqbotHttpHeaders.RESPONSE, response.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(response.getBody()))
                .andExpect(header().string("test-header", "testvalue"));

        validate("x", Collections.emptyMap(), RequestMethod.GET, null, path);
    }

    @Test
    public void it_returns_the_requested_body_for_a_post_using_header() throws
            Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "testvalue")
                .body(RandomStringUtils.randomAlphanumeric(30))
                .build();

        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        String path = "/bucket/x";
        mockMvc.perform(post(path).content("hello").header(ReqbotHttpHeaders.RESPONSE, response.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(response.getBody()))
                .andExpect(header().string("test-header", "testvalue"));

        validate("x", Collections.emptyMap(), RequestMethod.POST, "hello", path);
    }

    @Test
    public void it_returns_the_requested_response_body_and_http_status_code_for_a_get() throws
            Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "testvalue")
                .body("hello")
                .build();

        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        String path = "/bucket/x/response/" + response.getUuid();
        mockMvc.perform(get(path).header(ReqbotHttpHeaders.HTTP_CODE, 404))
                .andExpect(status().isNotFound())
                .andExpect(content().string(response.getBody()))
                .andExpect(header().string("test-header", "testvalue"));

        validate("x", Collections.emptyMap(), RequestMethod.GET, null, path);
    }

    @Test
    public void it_returns_the_requested_response_body_and_http_status_code_for_a_post() throws
            Exception {
        Response response = new Response.Builder()
                .addHeader("test-header", "testvalue")
                .body("hello")
                .build();

        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        String path = "/bucket/x/response/" + response.getUuid();

        mockMvc.perform(post(path).content("hello").header(ReqbotHttpHeaders.HTTP_CODE, 404))
                .andExpect(status().isNotFound())
                .andExpect(content().string(response.getBody()))
                .andExpect(header().string("test-header", "testvalue"));

        validate("x", Collections.emptyMap(), RequestMethod.POST, "hello", path);
    }

    @Test
    public void it_handles_a_bad_expected_response_header() throws Exception {
        when(responseRepo.get("aaaaa")).thenReturn(null);

        String path = "/bucket/x";
        mockMvc.perform(get(path).header(ReqbotHttpHeaders.RESPONSE, "aaaaaa"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Response Not Found"));
    }

    @Test
    public void it_handles_a_bad_expected_response_pathparameter() throws Exception {
        when(responseRepo.get("aaaaa")).thenReturn(null);

        String path = "/bucket/x/response/aaaaa";
        mockMvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Response Not Found"));
    }

    private void validate(String bucket,
                          Map<String, String> queryParameters,
                          RequestMethod method,
                          String body,
                          String path
    ) {
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepo, times(1)).save(requestCaptor.capture());

        Request request = requestCaptor.getValue();
        assertThat(request.getBody(), is(equalTo(body)));
        assertThat(request.getQueryParameters(), is(equalTo(queryParameters)));
        assertThat(request.getBucket(), is(equalTo(bucket)));
        assertThat(request.getMethod(), is(equalTo(method.name())));
        assertThat(request.getUuid(), is(not(nullValue())));
        assertThat(request.getPath(), is(equalTo(path)));

        Date timestamp = Date.from(Instant.parse(request.getTimestamp()));
        assertThat(timestamp, within(2, TimeUnit.SECONDS, Moments.now()));
    }
}
