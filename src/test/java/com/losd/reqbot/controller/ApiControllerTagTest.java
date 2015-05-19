package com.losd.reqbot.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.losd.reqbot.config.GsonHttpMessageConverterConfiguration;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.ResponseRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GsonHttpMessageConverterConfiguration.class})
public class ApiControllerTagTest {
    @Autowired
    GsonHttpMessageConverter gsonHttpMessageConverter;

    MockMvc mockMvc;
    Gson gson = new GsonBuilder().serializeNulls().create();

    @Mock
    ResponseRepo responseRepo;

    @InjectMocks
    ApiController apiController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(apiController).setMessageConverters(gsonHttpMessageConverter).build();
    }

    @Test
    public void it_can_get_a_list_of_tags() throws
            Exception
    {
        when(responseRepo.getTags()).thenReturn(Arrays.asList("a", "b", "c", "d"));

        MvcResult result = mockMvc.perform(get("/tags")).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString(), is(equalTo("[\"a\",\"b\",\"c\",\"d\"]")));
        verify(responseRepo, times(1)).getTags();
    }

    @Test
    public void it_returns_a_404_if_there_are_no_tags() throws
            Exception
    {
        when(responseRepo.getTags()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tags")).andExpect(status().isNotFound()).andExpect(status().reason("Not Found"));
        verify(responseRepo, times(1)).getTags();
    }

    @Test
    public void it_can_get_a_list_of_responses_for_a_tag() throws
            Exception
    {
        List<Response> list = new LinkedList<>();
        list.add(new Response.Builder().addHeader("h1", "v1").tags(Arrays.asList("a", "b")).body("body1").build());
        list.add(new Response.Builder().addHeader("h2", "v2").tags(Arrays.asList("a", "b")).body("body2").build());

        Type listType = new TypeToken<LinkedList<Response>>() {
        }.getType();
        String listAsJson = gson.toJson(list, listType);

        when(responseRepo.getByTag("a")).thenReturn(list);

        MvcResult result = mockMvc.perform(get("/tags/a")).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString(), is(equalTo(listAsJson)));
        verify(responseRepo, times(1)).getByTag("a");
    }

    @Test
    public void it_returns_a_404_if_there_are_no_responses_for_a_tag() throws
            Exception
    {
        when(responseRepo.getByTag("a")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tags/a")).andExpect(status().isNotFound()).andExpect(status().reason("Not Found"));
        verify(responseRepo, times(1)).getByTag("a");
    }
}
