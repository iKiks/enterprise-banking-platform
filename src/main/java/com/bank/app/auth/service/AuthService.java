package com.bank.app.auth.service;

import com.bank.app.auth.dto.*;
import com.bank.app.auth.entity.RefreshToken;
import com.bank.app.auth.entity.Role;
import com.bank.app.auth.entity.User;
import com.bank.app.auth.repository.RefreshTokenRepository;
import com.bank.app.auth.repository.RoleRepository;
import com.bank.app.auth.repository.UserRepository;
import com.bank.app.common.enums.RoleName;
import com.bank.app.exception.BusinessException;
import com.bank.app.exception.UnauthorizedException;
import com.bank.app.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already exists");
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.CUSTOMER);
                    return roleRepository.save(role);
                });

        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(Set.of(customerRole));
        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isAccountNonLocked()) {
            throw new UnauthorizedException("Account is locked");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountNonLocked(false);
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, refreshTokenValue, "Bearer", jwtTokenProvider.accessTokenExpirationSeconds());
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalid"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        RefreshToken freshToken = new RefreshToken();
        freshToken.setUser(user);
        freshToken.setToken(newRefreshToken);
        freshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(freshToken);

        return new TokenResponse(newAccessToken, newRefreshToken, "Bearer", jwtTokenProvider.accessTokenExpirationSeconds());
    }

    @Transactional
    public void revoke(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
