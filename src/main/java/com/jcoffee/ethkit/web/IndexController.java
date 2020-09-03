package com.jcoffee.ethkit.web;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping({"/"})
public class IndexController {
    private static final String prefix = "/static/page/";
    private static String verstion = "";

    @ApiOperation("index")
    @RequestMapping(
            value = {"/"},
            method = {RequestMethod.GET}
    )
    public String index() {
        String key = "xxddxx";
        if (StringUtils.contains(key, "20")) {
            verstion = "2";
        }

        return "/static/page/index" + verstion + ".html";
    }
}
