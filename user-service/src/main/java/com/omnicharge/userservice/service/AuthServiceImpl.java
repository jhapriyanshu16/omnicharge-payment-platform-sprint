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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;



    @Override
    public UserResponse register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail()))
            throw new UserAlreadyExistsException("Email already registered");

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .walletBalance(100.0)
                .build();

        userRepository.save(user);
        log.info("Registering user with email {}", request.getEmail());
        return mapper.map(user, UserResponse.class);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new InvalidCredentialsException("Invalid password");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        log.info("Login attempt for {}", request.getEmail());

        return LoginResponse.builder()
                .token(token)
                .user(mapper.map(user, UserResponse.class))
                .build();
    }
}
