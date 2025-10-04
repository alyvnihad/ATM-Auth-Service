package org.example.authservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.AccountResponse;
import org.example.authservice.dto.UserDTO;
import org.example.authservice.model.RefreshToken;
import org.example.authservice.model.Role;
import org.example.authservice.model.User;
import org.example.authservice.payload.LoginPayload;
import org.example.authservice.payload.RefreshTokenPayload;
import org.example.authservice.payload.RegisterPayload;
import org.example.authservice.repository.RefreshTokenRepository;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.AuthenticationUser;
import org.example.authservice.security.JwtUtil;
import org.example.authservice.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final SecurityFilter filter;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${card.service.url}")
    private String cardUrl;

    public void register(RegisterPayload payload) {
        userRepository.findByEmail(payload.getEmail()).ifPresent(user -> {
            throw new RuntimeException("user email already exists");
        });

        User user = new User();
        user.setName(payload.getName());
        user.setEmail(payload.getEmail());
        user.setPinHash(filter.passwordEncoder(payload.getPin()));
        user.setCurrency(payload.getCurrency());
        user.setRole(Role.CUSTOMER);

        RegisterPayload registerPayload = new RegisterPayload();
        registerPayload.setPin(payload.getPin());
        registerPayload.setCurrency(payload.getCurrency());

        ResponseEntity<AccountResponse> voidResponseEntity = restTemplate.postForEntity(cardUrl + "/register", registerPayload, AccountResponse.class);
        if (voidResponseEntity.getBody() != null) {
            user.setCardNumber(voidResponseEntity.getBody().getCardNumber());
        }

        userRepository.save(user);
    }

    public UserDTO login(LoginPayload payload) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(payload.getCardNumber(), payload.getPin()));
        AuthenticationUser user = (AuthenticationUser) authentication.getPrincipal();

        refreshTokenRepository.findByCardNumberAndExpiredFalse(user.getCardNumber()).ifPresent(
                refreshToken -> {
                    refreshToken.setExpired(true);
                    refreshTokenRepository.save(refreshToken);
                });

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setCardNumber(payload.getCardNumber());
        refreshToken.setToken(jwtUtil.generatedToken(user));
        refreshToken.setExpiresAt(LocalDateTime.now().plusHours(7));
        refreshToken.setExpired(false);
        refreshTokenRepository.save(refreshToken);

        String token = jwtUtil.generatedToken(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setCardNumber(user.getCardNumber());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setCurrency(user.getCurrency());
        userDTO.setRole(user.getAuthorities().stream().findFirst().get().getAuthority().replace("ROLE_", ""));
        userDTO.setAccessToken(token);
        userDTO.setRefreshToken(refreshToken.getToken());

        return userDTO;
    }


    public void logout(RefreshTokenPayload payload){
        long cardNumber;
        try {
            cardNumber = Long.parseLong(jwtUtil.extractUsername(payload.getRefreshToken()));
        }catch (Exception e){
            throw new RuntimeException("Error not found token");
        }

        refreshTokenRepository.findByCardNumberAndToken(cardNumber, payload.getRefreshToken()).ifPresent(
                refreshToken -> {
                    refreshToken.setExpired(true);
                    refreshTokenRepository.save(refreshToken);
                }
        );
        SecurityContextHolder.clearContext();
    }
}
