package com.omnicharge.userservice.controller;

import com.omnicharge.userservice.dto.request.LoginRequest;
import com.omnicharge.userservice.dto.request.RegisterRequest;
import com.omnicharge.userservice.dto.response.ApiResponse;
import com.omnicharge.userservice.dto.response.LoginResponse;
import com.omnicharge.userservice.dto.response.UserResponse;
import com.omnicharge.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User Registered")
                .data(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request){
        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login Success")
                .data(authService.login(request))
                .build();
    }
}
