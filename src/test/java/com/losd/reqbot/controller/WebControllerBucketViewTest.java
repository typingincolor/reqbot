package com.losd.reqbot.controller;

import com.losd.reqbot.model.Request;
import com.losd.reqbot.repository.BucketRepo;
import com.losd.reqbot.repository.RequestRepo;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
public class WebControllerBucketViewTest {
    private MockMvc mockMvc;

    @Mock
    private AccountResolver accountResolver;

    @Mock
    private Account account;

    @Mock
    private BucketRepo bucketRepo;

    @Mock
    private RequestRepo requestRepo;

    @InjectMocks
    private WebController webController;

    final Set<String> bucketList = new HashSet<String>(Arrays.asList("a", "b"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("blah");
        mockMvc = MockMvcBuilders.standaloneSetup(webController).setViewResolvers(viewResolver).build();
    }

    @Test
    public void loggedInAndUserCanSeeBucket() throws Exception {
        List<Request> requestList = new ArrayList<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("header1", "value1");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("param1", "value1");

        requestList.add(new Request("a", headers, "body", queryParams, "GET"));

        when(account.getUsername()).thenReturn("testuser@example.com");
        when(account.getFullName()).thenReturn("Homer Simpson");
        when(accountResolver.getAccount(any(HttpServletRequest.class))).thenReturn(account);
        when(bucketRepo.getBucketsForUser("testuser@example.com")).thenReturn(bucketList);
        when(requestRepo.getBucket("a")).thenReturn(requestList);

        mockMvc.perform(get("/bucket/a/view"))
                .andExpect(status().isOk())
                .andExpect(view().name(is("view")))
                .andExpect(model().attribute("fullname", is("Homer Simpson")))
                .andExpect(model().attribute("bucket", is("a")))
                .andExpect(model().attribute("requests", hasSize(1)))
                .andExpect(model().attribute("requests", is(requestList)));
    }

    @Test
    public void loggedInAndUserCannotSeeBucket() throws Exception {
        when(account.getUsername()).thenReturn("testuser@example.com");
        when(accountResolver.getAccount(any(HttpServletRequest.class))).thenReturn(account);

        mockMvc.perform(get("/bucket/x/view"))
                .andExpect(status().isFound())
                .andExpect(model().size(0))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void notLoggedIn() throws Exception {
        when(accountResolver.getAccount(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(get("/bucket/x/view"))
                .andExpect(status().isFound())
                .andExpect(model().size(0))
                .andExpect(redirectedUrl("/login"));
    }
}
