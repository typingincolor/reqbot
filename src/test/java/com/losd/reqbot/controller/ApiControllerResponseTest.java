package com.losd.reqbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.losd.reqbot.model.IncomingResponse;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.cthul.matchers.object.ContainsPattern.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class ApiControllerResponseTest {
    public static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";

    MockMvc mockMvc;
    Gson gson = new GsonBuilder().serializeNulls().create();

    @Mock
    ResponseRepo responseRepo;

    @Mock
    RequestRepo requestRepo;

    @InjectMocks
    ApiController apiController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(apiController).build();
    }

    @Test
    public void it_saves_a_response() throws
            Exception
    {
        IncomingResponse incoming = new IncomingResponse.Builder()
                .addHeader("test_header", "test_header_value")
                .body("response_body")
                .tags(Arrays.asList("tag1", "tag2"))
                .build();

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(incoming);

        MvcResult mvcResult = mockMvc.perform(post("/responses").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Response result = gson.fromJson(mvcResult.getResponse().getContentAsString(), Response.class);

        assertThat(result.getBody(), is(equalTo("response_body")));
        assertThat(result.getHeaders(), hasEntry("test_header", "test_header_value"));

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseRepo, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getBody(), is(equalTo("response_body")));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("test_header", "test_header_value"));
        assertThat(argumentCaptor.getValue().getTags(), hasSize(2));
        assertThat(argumentCaptor.getValue().getTags(), contains("tag1", "tag2"));
        assertThat(argumentCaptor.getValue().getUuid().toString(), matchesPattern(UUID_REGEX));
    }

    @Test
    public void it_gets_an_error_400_if_the_response_to_be_saved_has_no_body() throws
            Exception
    {
        mockMvc.perform(post("/responses").contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Response had an empty body"));
    }

    @Test
    public void it_can_retrieve_a_response() throws
            Exception
    {
        Response response = new Response.Builder().body("body").build();

        when(responseRepo.get(response.getUuid().toString())).thenReturn(response);

        MvcResult mvcResult = mockMvc.perform(get("/responses/" + response.getUuid()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        verify(responseRepo, times(1)).get(response.getUuid().toString());
        assertThat(mvcResult.getResponse().getContentAsString(), is(equalTo(gson.toJson(response, Response.class))));
    }

    @Test
    public void it_throws_a_404_if_there_is_no_response() throws
            Exception
    {
        when(responseRepo.get("rubbish")).thenReturn(null);

        mockMvc.perform(get("/responses/rubbish"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Not Found"))
                .andReturn();

        verify(responseRepo, times(1)).get("rubbish");
    }
}
