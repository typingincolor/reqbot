package com.losd.reqbot.controller;

import com.google.common.base.Strings;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.model.WebResponse;
import com.losd.reqbot.repository.RequestRepo;
import com.losd.reqbot.repository.ResponseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.regex.MatchResult;

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
    private RequestRepo requests = null;

    @Autowired
    private ResponseRepo responses = null;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        List<String> buckets = requests.getBuckets();

        if (buckets.size() > 0) {
            return "redirect:/web/bucket/" + buckets.get(0);
        }

        model.addAttribute("mode", "request");

        return "index";
    }

    @RequestMapping(value = "/web/bucket/{bucket}", method = RequestMethod.GET)
    public String viewBucket(@PathVariable String bucket, Model model) {
        model.addAttribute("mode", "request");
        model.addAttribute("bucket", bucket);
        model.addAttribute("buckets", requests.getBuckets());
        model.addAttribute("requests", requests.getByBucket(bucket));
        return "bucket-view";
    }

    @RequestMapping(value = "/web/tag/{tag}", method = RequestMethod.GET)
    public String viewTag(@PathVariable String tag, Model model) {
        model.addAttribute("mode", "response");
        model.addAttribute("tag", tag);
        model.addAttribute("tags", responses.getTags());
        model.addAttribute("responses", responses.getByTag(tag));

        return "tag-view";
    }

    @RequestMapping(value = "/web/responses", method = RequestMethod.GET)
    public String responses(Model model) {
        List<String> tags = responses.getTags();

        if (tags.size() > 0) {
            return "redirect:/web/tag/" + tags.get(0);
        }

        model.addAttribute("mode", "response");

        return "index";
    }

    @RequestMapping(value = "/web/responses/{response}", method = RequestMethod.GET)
    public String response(@PathVariable String response, Model model) {
        Response result = responses.get(response);
        model.addAttribute("response", result);
        return "response";
    }

    @RequestMapping(value = "/web/response/create", method = RequestMethod.GET)
    public String renderCreateReponse(Model model) {
        model.addAttribute("webResponse", new WebResponse());
        return "create-response";
    }

    @RequestMapping(value = "/web/response/create", method = RequestMethod.POST)
    public String handleCreateReponseForm(@ModelAttribute WebResponse webResponse,Model model) {
        Map<String, String> headers = getHeadersFromFormData(webResponse.getHeaders());
        List<String> tags = getTagsFromFormData(webResponse.getTags());

        Response newResponse = new Response.Builder().headers(headers).tags(tags).body(webResponse.getBody()).build();

        responses.save(newResponse);

        model.addAttribute("response", newResponse);
        return "redirect:/web/responses/" + newResponse.getUuid();
    }

    private Map<String, String> getHeadersFromFormData(String formHeaders) {
        if (Strings.isNullOrEmpty(formHeaders)) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new HashMap<>();
        Scanner headerScanner = new Scanner(formHeaders);

        while (headerScanner.hasNextLine()) {
            Scanner lineScanner = new Scanner(headerScanner.nextLine());
            lineScanner.findInLine("([a-z0-9A-Z-\\./]+)\\s*:\\s*([a-z0-9A-Z-\\./]+)");
            MatchResult res = lineScanner.match();
            headers.put(res.group(1), res.group(2));
        }

        return headers;
    }

    private List<String> getTagsFromFormData(String formTags) {
        if (Strings.isNullOrEmpty(formTags)) {
            return Collections.emptyList();
        }

        List<String> tags = new LinkedList<>();
        Scanner tagScanner = new Scanner(formTags).useDelimiter("\\s*,\\s*");
        tagScanner.forEachRemaining((tag) -> tags.add(tag));

        return tags;
    }
}
