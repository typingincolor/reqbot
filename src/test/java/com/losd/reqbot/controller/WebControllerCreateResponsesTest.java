package com.losd.reqbot.controller;

import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.ResponseRepo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.cthul.matchers.CthulMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
public class WebControllerCreateResponsesTest {
    public static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";

    private MockMvc mockMvc;

    @Mock
    private ResponseRepo responseRepo;

    @InjectMocks
    private WebController webController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("blah");
        mockMvc = MockMvcBuilders.standaloneSetup(webController).setViewResolvers(viewResolver).build();
    }

    @Test
    public void it_creates_a_response() throws Exception {
        mockMvc.perform(post("/web/response/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("tags", "a,b,c,d")
                .param("headers", "header1:value1\r\nheader2:value2")
                .param("body", "this\r\nis\r\na\r\nbody"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("response", isA(Response.class)))
                .andExpect(view().name(matchesPattern("redirect:/web/responses/" + "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")));

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseRepo, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getBody(), is(equalTo("this\r\nis\r\na\r\nbody")));

        assertThat(argumentCaptor.getValue().getHeaders().size(), is(equalTo(2)));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("header1", "value1"));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("header2", "value2"));

        assertThat(argumentCaptor.getValue().getTags(), hasSize(4));
        assertThat(argumentCaptor.getValue().getTags(), contains("a", "b", "c", "d"));
        assertThat(argumentCaptor.getValue().getUuid().toString(), matchesPattern(UUID_REGEX));
    }

    @Test
         public void it_handles_headers_with_dashes() throws Exception {
        mockMvc.perform(post("/web/response/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("tags", "a,b,c,d")
                .param("headers", "Content-Type:value1\r\nheader2:value2")
                .param("body", "this\r\nis\r\na\r\nbody"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("response", isA(Response.class)))
                .andExpect(view().name(matchesPattern("redirect:/web/responses/" + "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")));

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseRepo, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getHeaders().size(), is(equalTo(2)));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("Content-Type", "value1"));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("header2", "value2"));

        assertThat(argumentCaptor.getValue().getTags(), hasSize(4));
        assertThat(argumentCaptor.getValue().getTags(), contains("a", "b", "c", "d"));
        assertThat(argumentCaptor.getValue().getUuid().toString(), matchesPattern(UUID_REGEX));
    }

    @Test
    public void it_handles_headers_with_dots() throws Exception {
        mockMvc.perform(post("/web/response/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("headers", "Content.Type:value1\r\nheader2:value2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("response", isA(Response.class)))
                .andExpect(view().name(matchesPattern("redirect:/web/responses/" + "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")));

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseRepo, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getHeaders().size(), is(equalTo(2)));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("Content.Type", "value1"));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("header2", "value2"));

        assertThat(argumentCaptor.getValue().getUuid().toString(), matchesPattern(UUID_REGEX));
    }

    @Test
    public void it_handles_headers_with_spaces() throws Exception {
        mockMvc.perform(post("/web/response/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("headers", "header1    : value1\r\nheader2: value2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("response", isA(Response.class)))
                .andExpect(view().name(matchesPattern("redirect:/web/responses/" + "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")));

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseRepo, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getHeaders().size(), is(equalTo(2)));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("header1", "value1"));
        assertThat(argumentCaptor.getValue().getHeaders(), hasEntry("header2", "value2"));

        assertThat(argumentCaptor.getValue().getUuid().toString(), matchesPattern(UUID_REGEX));
    }

    @Test
    public void it_handles_tags_with_spaces() throws Exception {
        mockMvc.perform(post("/web/response/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("tags", "a , b ,c , d"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("response", isA(Response.class)))
                .andExpect(view().name(matchesPattern("redirect:/web/responses/" + "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")));

        ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);

        verify(responseRepo, times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getTags(), hasSize(4));
        assertThat(argumentCaptor.getValue().getTags(), contains("a", "b", "c", "d"));
        assertThat(argumentCaptor.getValue().getUuid().toString(), matchesPattern(UUID_REGEX));
    }
}
