package com.omnicharge.apigateway;

import com.omnicharge.apigateway.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ApiGatewayApplicationTests {

    @MockBean
    private JwtUtil jwtUtil; // This prevents the real JwtUtil from trying to initialize

    @Test
    void contextLoads() {
        // The test will now pass because the Context found a 'mock' bean
    }
}