package org.example.authservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.*;
import org.example.authservice.exception.NotFoundException;
import org.example.authservice.exception.UnAuthorizedException;
import org.example.authservice.exception.UsernameAlreadyExistsException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User can register, log in and log out
 * email is verified when registering, pin is checked when logging in
 **/

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SecurityFilter filter;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Value("${card.service.url}")
    private String cardUrl;
    @Value("${notification.service.url}")
    private String notificationUrl;
    @Value("${report.service.url}")
    private String reportUrl;

    // Register new customer and send account info as PDF via email
    @Transactional
    public void register(RegisterPayload payload) {
        // Check if email already exists
        userRepository.findByEmail(payload.getEmail()).ifPresent(user -> {
            throw new UsernameAlreadyExistsException("user email already exists");
        });

        // Create new user
        User user = new User();
        user.setName(payload.getName());
        user.setEmail(payload.getEmail());
        user.setPinHash(filter.Encoder(payload.getPin()));
        user.setCurrency(payload.getCurrency());
        user.setRole(Role.CUSTOMER);

        // Prepare request for Card microservice
        CardRequest cardRequest = new CardRequest();
        cardRequest.setPin(payload.getPin());
        cardRequest.setCurrency(payload.getCurrency());
        cardRequest.setEmail(user.getEmail());

        // Call Card service to create card
        ResponseEntity<CardResponse> cardResponse = restTemplate.postForEntity(cardUrl + "/register", cardRequest, CardResponse.class);
        if (cardResponse.getBody() != null) {
            user.setCardNumber(cardResponse.getBody().getCardNumber());
            userRepository.save(user);
        }

        // Prepare data for Report service (PDF generation)
        ReportRequest reportRequest = new ReportRequest();

        if (cardResponse.getBody() != null) {
            reportRequest.setAccountNumber(cardResponse.getBody().getAccountNumber());
            reportRequest.setName(user.getName());
            reportRequest.setEmail(user.getEmail());
            reportRequest.setCardNumber(user.getCardNumber());
            reportRequest.setExpiryDate(LocalDate.now().plusYears(3));
            reportRequest.setCurrency(user.getCurrency().toString());
            reportRequest.setPaymentNetwork(cardResponse.getBody().getPaymentNetwork());
        }

        // Generate PDF report
        ResponseEntity<String> reportResponse = restTemplate.postForEntity(reportUrl + "/pdf", reportRequest, String.class);
        String filePath = reportResponse.getBody();

        // Prepare notification request
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTo(user.getEmail());
        notificationRequest.setBody(user.getName());
        notificationRequest.setFilePath(filePath);

        AuthenticationUser userDetails = (AuthenticationUser) userService.loadUserByUsername("register - "+ user.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtUtil.generatedToken(userDetails));
        refreshTokenRepository.save(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(refreshToken.getToken());
        HttpEntity<NotificationRequest> entity = new HttpEntity<>(notificationRequest,headers);

        //  Notification service call. Send email with PDF file
        restTemplate.postForEntity(notificationUrl + "/register-send", entity, NotificationRequest.class);

        userRepository.save(user);
    }

    // Login with card number and PIN
    public UserDTO login(LoginPayload payload) {


        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCardNumber(payload.getCardNumber());
        accountRequest.setPin(payload.getPin());

        // Check card number and PIN from Card service
        ResponseEntity<Boolean> posted = restTemplate.postForEntity(cardUrl + "/pin-check", accountRequest, Boolean.class);

        Boolean isCheck = posted.getBody();
        if (!Boolean.TRUE.equals(isCheck)) {
            throw new UnAuthorizedException("Card Number or pin invalid");
        }

        AuthenticationUser user = (AuthenticationUser) userService.loadUserByUsername(String.valueOf(payload.getCardNumber()));

        // Disable old refresh token if exists
        refreshTokenRepository.findByCardNumberAndExpiredFalse(accountRequest.getCardNumber()).ifPresent(
                refreshToken -> {
                    refreshToken.setExpired(true);
                    refreshTokenRepository.save(refreshToken);
                });

        // Create new refresh token
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

    // Logout and disable refresh token
    public void logout(RefreshTokenPayload payload) {
        long cardNumber;
        try {
            cardNumber = Long.parseLong(jwtUtil.extractUsername(payload.getRefreshToken()));
        } catch (Exception e) {
            throw new NotFoundException("Error not found token");
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
