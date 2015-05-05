package com.losd.reqbot.controller;

import com.losd.reqbot.model.Request;
import com.losd.reqbot.repository.RequestRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.co.it.modular.hamcrest.date.Moments;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    @InjectMocks
    private IncomingRequestController incomingRequestController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(incomingRequestController).build();
    }

    @Test
    public void handleGetTestWithoutQueryParameters() throws Exception {
        mockMvc.perform(get("/bucket/x")).andExpect(status().isOk()).andExpect(content().string("OK"));

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepo, times(1)).save(requestCaptor.capture());

        Request request = requestCaptor.getValue();

        assertThat(request.getBody(), is(nullValue()));
        assertThat(request.getQueryParameters(), is(equalTo(Collections.EMPTY_MAP)));
        assertThat(request.getBucket(), is(equalTo("x")));
        assertThat(request.getMethod(), is(equalTo(RequestMethod.GET.name())));
        assertThat(request.getUuid(), is(not(nullValue())));

        Date timestamp = Date.from(Instant.parse(request.getTimestamp()));
        assertThat(timestamp, within(2, TimeUnit.SECONDS, Moments.now()));
    }
}
