package com.losd.reqbot.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.config.GsonHttpMessageConverterConfiguration;
import com.losd.reqbot.model.IncomingResponse;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.ResponseRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GsonHttpMessageConverterConfiguration.class})
public class ResponseApiControllerTest {
    public static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    @Autowired
    GsonHttpMessageConverter gsonHttpMessageConverter;
    private MockMvc mockMvc;
    private Gson gson = new GsonBuilder().serializeNulls().create();
    @Mock
    private ResponseRepo repo;
    @InjectMocks
    private ResponseApiController responseApiController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(responseApiController).setMessageConverters(gsonHttpMessageConverter).build();
    }

    @Test
    public void it_saves_a_response() throws
            Exception {
        IncomingResponse incoming = new IncomingResponse.Builder()
                .addHeader("test_header", "test_header_value")
                .body("response_body")
                .tags(Arrays.asList("tag1", "tag2"))
                .build();

        String json = gson.toJson(incoming, IncomingResponse.class);

        MvcResult mvcResult = mockMvc.perform(post("/response").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Response result = gson.fromJson(mvcResult.getResponse().getContentAsString(), Response.class);

        assertThat(result.getBody(), is(equalTo("response_body")));
        assertThat(result.getHeaders(), hasEntry("test_header", "test_header_value"));

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);

        verify(repo, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getBody(), is(equalTo("response_body")));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("test_header", "test_header_value"));
        assertThat(argumentCaptor.getValue().getTags(), hasSize(2));
        assertThat(argumentCaptor.getValue().getTags(), contains("tag1", "tag2"));
        assertThat(argumentCaptor.getValue().getUuid().toString().matches(UUID_REGEX), is(true));
    }

    @Test
    public void it_gets_an_error_400_if_the_response_to_be_saved_has_no_body() throws
            Exception {
        mockMvc.perform(post("/response").contentType(MediaType.APPLICATION_JSON).content("{\"stuff\": \"random\"}"))
                .andExpect(status().isBadRequest());
    }
}
