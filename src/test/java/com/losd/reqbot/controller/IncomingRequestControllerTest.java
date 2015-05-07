package com.losd.reqbot.controller;

import com.losd.reqbot.model.Request;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
public class IncomingRequestControllerTest {
    private MockMvc mockMvc;

    @Mock
    private RequestRepo requestRepo;

    @Mock
    private ResponseRepo responseRepo;

    @InjectMocks
    private IncomingRequestController incomingRequestController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(incomingRequestController).build();
    }

    @Test
    public void it_handles_a_get() throws Exception {
        mockMvc.perform(get("/bucket/x")).andExpect(status().isOk()).andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        validate("x", Collections.EMPTY_MAP, RequestMethod.GET, null);
    }

    @Test
    public void it_handles_a_post() throws Exception {
        mockMvc.perform(post("/bucket/x").content("hello")).andExpect(status().isOk()).andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        validate("x", Collections.EMPTY_MAP, RequestMethod.POST, "hello");
    }

    @Test
    public void it_handles_a_get_with_query_parameters() throws Exception {
        mockMvc.perform(get("/bucket/x?a=1")).andExpect(status().isOk()).andExpect(content().string(HttpStatus.OK.getReasonPhrase()));
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("a", "1");

        validate("x", queryParams, RequestMethod.GET, null);
    }

    @Test
    public void it_handles_a_post_with_query_parameters() throws Exception {
        mockMvc.perform(post("/bucket/x?a=1").content("hello")).andExpect(status().isOk()).andExpect(content().string(HttpStatus.OK.getReasonPhrase()));
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("a", "1");

        validate("x", queryParams, RequestMethod.POST, "hello");
    }

    @Test
    public void it_goes_slow_when_asked() throws Exception {
        Instant start = Instant.now();
        mockMvc.perform(get("/bucket/x").header("X_REQBOT_GO_SLOW", 5000)).andExpect(status().isOk()).andExpect(content().string(HttpStatus.OK.getReasonPhrase()));

        Instant end = Instant.now();

        assertThat(Duration.between(start, end).toMillis(), is(greaterThan(5000L)));
    }

    @Test
    public void it_returns_the_requested_http_status_code() throws Exception {
        mockMvc.perform(get("/bucket/x").header("X_REQBOT_HTTP_CODE", 404)).andExpect(status().isNotFound()).andExpect(content().string(HttpStatus.NOT_FOUND.getReasonPhrase()));
    }

    @Test
    public void it_returns_the_correct_http_status_code_when_the_header_is_mixed_case() throws Exception {
        mockMvc.perform(get("/bucket/x").header("X_ReQbOt_http_CODE", 404)).andExpect(status().isNotFound()).andExpect(content().string(HttpStatus.NOT_FOUND.getReasonPhrase()));
    }

    @Test
    public void it_returns_a_http_ok_response_code_when_http_code_header_is_empty() throws Exception {
        mockMvc.perform(get("/bucket/x").header("X_ReQbOt_http_CODE", "")).andExpect(status().isOk()).andExpect(content().string(HttpStatus.OK.getReasonPhrase()));
    }

    @Test
    public void it_returns_the_requested_response_body_for_a_get() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("test-header", "testvalue");

        Response response = new Response(headers, "hello");
        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        mockMvc.perform(get("/bucket/x/"+response.getUuid())).andExpect(status().isOk()).andExpect(content().string(response.getBody()));

        validate("x", Collections.EMPTY_MAP, RequestMethod.GET, null);
    }

    @Test
    public void it_returns_the_requested_response_body_for_a_post() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("test-header", "testvalue");

        Response response = new Response(headers, "hello");
        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        mockMvc.perform(post("/bucket/x/"+response.getUuid()).content("hello")).andExpect(status().isOk()).andExpect(content().string(response.getBody()));

        validate("x", Collections.EMPTY_MAP, RequestMethod.GET, "hello");
    }

    @Test
    public void it_returns_the_requested_response_body_and_http_status_code_for_a_get() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("test-header", "testvalue");

        Response response = new Response(headers, "hello");
        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        mockMvc.perform(get("/bucket/x/"+response.getUuid()).header("X_REQBOT_HTTP_CODE", 404)).andExpect(status().isNotFound()).andExpect(content().string(response.getBody()));

        validate("x", Collections.EMPTY_MAP, RequestMethod.GET, null);
    }

    @Test
    public void it_returns_the_requested_response_body_and_http_status_code_for_a_post() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("test-header", "testvalue");

        Response response = new Response(headers, "hello");
        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        mockMvc.perform(post("/bucket/x/" + response.getUuid()).content("hello").header("X_REQBOT_HTTP_CODE", 404)).andExpect(status().isNotFound()).andExpect(content().string(response.getBody()));

        validate("x", Collections.EMPTY_MAP, RequestMethod.GET, "hello");
    }

    private void validate(String bucket, Map queryParameters, RequestMethod method, String body) {
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepo, times(1)).save(requestCaptor.capture());

        Request request = requestCaptor.getValue();
        assertThat(request.getBody(), is(equalTo(body)));
        assertThat(request.getQueryParameters(), is(equalTo(queryParameters)));
        assertThat(request.getBucket(), is(equalTo(bucket)));
        assertThat(request.getMethod(), is(equalTo(method.name())));
        assertThat(request.getUuid(), is(not(nullValue())));

        Date timestamp = Date.from(Instant.parse(request.getTimestamp()));
        assertThat(timestamp, within(2, TimeUnit.SECONDS, Moments.now()));
    }
}
