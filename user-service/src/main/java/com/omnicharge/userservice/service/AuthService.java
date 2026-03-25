package com.omnicharge.userservice.service;

import com.omnicharge.userservice.dto.request.LoginRequest;
import com.omnicharge.userservice.dto.request.RegisterRequest;
import com.omnicharge.userservice.dto.response.LoginResponse;
import com.omnicharge.userservice.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}