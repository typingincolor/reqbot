package com.losd.reqbot.controller;

import com.losd.reqbot.repository.BucketRepo;
import com.losd.reqbot.repository.RequestRepo;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

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
@Controller
public class WebController {
    @Autowired
    private RequestRepo requestRepo = null;

    @Autowired
    private BucketRepo buckets = null;

    @Autowired
    AccountResolver accountResolver = null;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model, HttpServletRequest request) {
        Account account = accountResolver.getAccount(request);

        if (account != null) {
            model.addAttribute("buckets", buckets.getBucketsForUser(account.getUsername()));
        }

        return "index";
    }

    @RequestMapping(value = "/web/bucket/{bucket}/view", method = RequestMethod.GET)
    public String view(@PathVariable String bucket, Model model, HttpServletRequest request) {
        Account account = accountResolver.getAccount(request);

        if (account != null) {
            if (buckets.getBucketsForUser(account.getUsername())
                       .contains(bucket)) {
                model.addAttribute("fullname", account.getFullName());
                model.addAttribute("bucket", bucket);
                model.addAttribute("requests", requestRepo.getBucket(bucket));
                return "view";
            }
            else {
                return "redirect:/";
            }
        }
        return "redirect:/login";
    }
}
