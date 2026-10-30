package com.gamehub.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class TestController {
    @GetMapping("")
    public String protectedEndpoint(){
        return "Success";
    }
}
