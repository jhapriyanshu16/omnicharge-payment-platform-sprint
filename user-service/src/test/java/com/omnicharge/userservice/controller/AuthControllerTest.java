package com.omnicharge.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.userservice.dto.request.LoginRequest;
import com.omnicharge.userservice.dto.request.RegisterRequest;
import com.omnicharge.userservice.dto.response.LoginResponse;
import com.omnicharge.userservice.dto.response.UserResponse;
import com.omnicharge.userservice.exception.GlobalExceptionHandler;
import com.omnicharge.userservice.exception.InvalidCredentialsException;
import com.omnicharge.userservice.exception.UserAlreadyExistsException;
import com.omnicharge.userservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Priyanshu");
        request.setEmail("priyanshu@example.com");
        request.setPassword("secret");

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("Priyanshu")
                .email("priyanshu@example.com")
                .role("ROLE_USER")
                .walletBalance(100.0)
                .build();

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Registered"))
                .andExpect(jsonPath("$.data.email").value("priyanshu@example.com"));
    }

    @Test
    void shouldReturnBadRequestWhenUserAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Priyanshu");
        request.setEmail("priyanshu@example.com");
        request.setPassword("secret");

        when(authService.register(request)).thenThrow(new UserAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterPayloadIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setEmail("invalid-email");
        request.setPassword("");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("priyanshu@example.com")
                .password("secret")
                .build();

        LoginResponse response = LoginResponse.builder()
                .token("jwt-token")
                .user(UserResponse.builder().id(1L).email("priyanshu@example.com").build())
                .build();

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login Success"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("priyanshu@example.com")
                .password("wrong-password")
                .build();

        when(authService.login(request)).thenThrow(new InvalidCredentialsException("Invalid password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginEmailIsMissing() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(null)
                .password("secret")
                .build();

        when(authService.login(request)).thenThrow(new InvalidCredentialsException("Invalid email"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenLoginPasswordIsMissing() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password(null)
                .build();

        when(authService.login(request)).thenThrow(new InvalidCredentialsException("Invalid password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterEmailIsMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail(null);
        request.setPassword("secret");

        when(authService.register(request)).thenThrow(new UserAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterNameIsMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterPasswordIsMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("user@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInvalidEmailErrorWhenUserNotFound() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("secret")
                .build();

        when(authService.login(request)).thenThrow(new InvalidCredentialsException("Invalid email"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email"));
    }

    @Test
    void shouldReturnDuplicateEmailErrorWhenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("existing@example.com");
        request.setPassword("secret");

        when(authService.register(request)).thenThrow(new UserAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void shouldReturnUserDataInLoginResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("priyanshu@example.com")
                .password("secret")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(5L)
                .name("Priyanshu Kumar")
                .email("priyanshu@example.com")
                .role("ROLE_USER")
                .walletBalance(250.50)
                .build();

        LoginResponse response = LoginResponse.builder()
                .token("jwt-token-abc123")
                .user(userResponse)
                .build();

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token-abc123"))
                .andExpect(jsonPath("$.data.user.id").value(5L))
                .andExpect(jsonPath("$.data.user.name").value("Priyanshu Kumar"))
                .andExpect(jsonPath("$.data.user.walletBalance").value(250.50));
    }
}
