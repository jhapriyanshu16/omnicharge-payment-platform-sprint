package com.omnicharge.userservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/secure")
    public String secure(@RequestHeader("X-User-Email") String email){
        return "Hello " + email;
    }
}