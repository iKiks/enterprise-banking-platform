package com.bank.app.auth.service;

import com.bank.app.auth.dto.LoginRequest;
import com.bank.app.auth.dto.RegisterRequest;
import com.bank.app.auth.dto.TokenResponse;
import com.bank.app.auth.entity.Role;
import com.bank.app.auth.entity.User;
import com.bank.app.auth.repository.RefreshTokenRepository;
import com.bank.app.auth.repository.RoleRepository;
import com.bank.app.auth.repository.UserRepository;
import com.bank.app.exception.UnauthorizedException;
import com.bank.app.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private com.bank.app.auth.service.AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);

        authService = new com.bank.app.auth.service.AuthService(userRepository, roleRepository, refreshTokenRepository,
                passwordEncoder, authenticationManager, jwtTokenProvider);
    }

    @Test
    void register_and_login() {
        RegisterRequest reg = new RegisterRequest("test@example.com", "password", "Test User");

        when(userRepository.existsByEmail(reg.email())).thenReturn(false);
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        when(passwordEncoder.encode(reg.password())).thenReturn("hashed");

        authService.register(reg);

        verify(userRepository).save(any(User.class));

        // login path: user exists
        User user = new User();
        user.setEmail(reg.email());
        user.setPasswordHash("hashed");
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        when(userRepository.findByEmailAndDeletedFalse(reg.email())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh");

        TokenResponse resp = authService.login(new LoginRequest(reg.email(), reg.password()));
        assertThat(resp).isNotNull();
        assertThat(resp.accessToken()).isEqualTo("access");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_invalid_credentials() {
        when(userRepository.findByEmailAndDeletedFalse(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "pw")))
                .isInstanceOf(UnauthorizedException.class);
    }
}
