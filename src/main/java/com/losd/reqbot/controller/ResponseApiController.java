package com.losd.reqbot.controller;

import com.losd.reqbot.model.IncomingResponse;
import com.losd.reqbot.model.Response;
import com.losd.reqbot.repository.ResponseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
@RestController
public class ResponseApiController {
    @Autowired
    ResponseRepo repo;

    Logger logger = LoggerFactory.getLogger(ResponseApiController.class);

    @RequestMapping(value = "/response", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response save(@RequestBody IncomingResponse incoming) throws IncomingEmptyBodyException {
        logger.info("POST /response");
        if (incoming.getBody() == null || incoming.getBody().isEmpty()) {
            throw new IncomingEmptyBodyException();
        }

        Response response = new Response.Builder().headers(incoming.getHeaders()).tags(incoming.getTags()).body(incoming.getBody()).build();
        repo.save(response);

        return response;
    }

    @ExceptionHandler(IncomingEmptyBodyException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleEmptyBody(IncomingEmptyBodyException e) {
        return "Empty Body";
    }

    static class IncomingEmptyBodyException extends Exception {

    }
}
