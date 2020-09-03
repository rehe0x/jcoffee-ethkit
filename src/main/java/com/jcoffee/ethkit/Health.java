package com.jcoffee.ethkit;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Health {
    @RequestMapping(
            value = {"/health"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public Object health() {
        return "ok";
    }
}
