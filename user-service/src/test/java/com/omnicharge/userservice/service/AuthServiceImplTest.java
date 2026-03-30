package com.omnicharge.userservice.service;

import com.omnicharge.userservice.dto.request.LoginRequest;
import com.omnicharge.userservice.dto.request.RegisterRequest;
import com.omnicharge.userservice.dto.response.LoginResponse;
import com.omnicharge.userservice.dto.response.UserResponse;
import com.omnicharge.userservice.entity.Role;
import com.omnicharge.userservice.entity.User;
import com.omnicharge.userservice.exception.InvalidCredentialsException;
import com.omnicharge.userservice.exception.UserAlreadyExistsException;
import com.omnicharge.userservice.repository.UserRepository;
import com.omnicharge.userservice.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Priyanshu");
        request.setEmail("user@omnicharge.com");
        request.setPassword("plain-password");

        UserResponse mappedResponse = UserResponse.builder()
                .id(1L)
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.ROLE_USER.name())
                .walletBalance(100.0)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(mapper.map(any(User.class), eq(UserResponse.class))).thenReturn(mappedResponse);

        UserResponse result = authService.register(request);

        assertEquals(mappedResponse, result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(request.getName(), savedUser.getName());
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
        assertEquals(100.0, savedUser.getWalletBalance());
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@omnicharge.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .email("user@omnicharge.com")
                .password("plain-password")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Priyanshu")
                .email(request.getEmail())
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .walletBalance(100.0)
                .build();

        UserResponse mappedUser = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .walletBalance(user.getWalletBalance())
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), user.getRole().name())).thenReturn("jwt-token");
        when(mapper.map(user, UserResponse.class)).thenReturn(mappedUser);

        LoginResponse result = authService.login(request);

        assertEquals("jwt-token", result.getToken());
        assertEquals(mappedUser, result.getUser());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalidDuringLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("missing@omnicharge.com")
                .password("plain-password")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalidDuringLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("user@omnicharge.com")
                .password("wrong-password")
                .build();

        User user = User.builder()
                .email(request.getEmail())
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid password", exception.getMessage());
        verify(jwtUtil, never()).generateToken(any(), any());
        verify(mapper, never()).map(any(User.class), eq(UserResponse.class));
    }

    @Test
    void shouldRegisterWithInitialWalletBalance() {
        RegisterRequest request = new RegisterRequest();
        request.setName("TestUser");
        request.setEmail("test@omnicharge.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(mapper.map(any(User.class), eq(UserResponse.class)))
                .thenReturn(UserResponse.builder()
                        .id(1L)
                        .name(request.getName())
                        .email(request.getEmail())
                        .role("ROLE_USER")
                        .walletBalance(100.0)
                        .build());

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals(100.0, savedUser.getWalletBalance(), "New user should have 100.0 initial wallet balance");
    }

    @Test
    void shouldAssignRoleUserWhenRegistering() {
        RegisterRequest request = new RegisterRequest();
        request.setName("TestUser");
        request.setEmail("test@omnicharge.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(mapper.map(any(User.class), eq(UserResponse.class)))
                .thenReturn(UserResponse.builder().id(1L).role("ROLE_USER").build());

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    void shouldEncodedPasswordBePassedToRepository() {
        RegisterRequest request = new RegisterRequest();
        request.setName("TestUser");
        request.setEmail("test@omnicharge.com");
        request.setPassword("plaintext-password");

        String encodedPassword = "sha256$encoded$password";

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(mapper.map(any(User.class), eq(UserResponse.class)))
                .thenReturn(UserResponse.builder().id(1L).build());

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals(encodedPassword, savedUser.getPassword());
    }

    @Test
    void shouldLoginReturnCorrectTokenAndUser() {
        LoginRequest request = LoginRequest.builder()
                .email("user@omnicharge.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email(request.getEmail())
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .walletBalance(100.0)
                .build();

        UserResponse mappedUser = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email(request.getEmail())
                .role("ROLE_USER")
                .walletBalance(100.0)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), user.getRole().name())).thenReturn("jwt-token-123");
        when(mapper.map(user, UserResponse.class)).thenReturn(mappedUser);

        LoginResponse result = authService.login(request);

        assertEquals("jwt-token-123", result.getToken());
        assertEquals(mappedUser, result.getUser());
    }

    @Test
    void shouldNotCallPasswordEncoderCheckWhenUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@omnicharge.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(any(), any());
    }
}
