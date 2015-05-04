package com.losd.reqbot.controller;

import com.losd.reqbot.repository.BucketRepo;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
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
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes={WebControllerTestConfiguration.class}, loader = SpringApplicationContextLoader.class)
public class WebControllerIndexTest {
    private MockMvc mockMvc;

    @Mock
    private AccountResolver accountResolver;

    @Mock
    private Account account;

    @Mock
    private BucketRepo bucketRepo;

    @InjectMocks
    private WebController webController;

    final Set<String> bucketList = new HashSet<String>(Arrays.asList("a", "b"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    public void loggedIn() throws Exception {
        when(account.getUsername()).thenReturn("testuser@example.com");
        when(accountResolver.getAccount(any(HttpServletRequest.class))).thenReturn(account);
        when(bucketRepo.getBucketsForUser("testuser@example.com")).thenReturn(bucketList);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name(is("index")))
                .andExpect(model().attribute("buckets", hasSize(2)))
                .andExpect(model().attribute("buckets", hasItems("a", "b")));
    }

    @Test
    public void notLoggedIn() throws Exception {
        when(accountResolver.getAccount(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name(is("index")))
                .andExpect(model().attributeDoesNotExist("buckets"));
    }
}

