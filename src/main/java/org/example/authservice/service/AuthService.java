package org.example.authservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.AccountResponse;
import org.example.authservice.model.Role;
import org.example.authservice.model.User;
import org.example.authservice.payload.RegisterPayload;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final SecurityFilter filter;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${card.service.url}")
    private String cardUrl;

    public void register(RegisterPayload payload){
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
        if (voidResponseEntity.getBody() !=null){
            user.setCardNumber(voidResponseEntity.getBody().getCardNumber());
        }

        userRepository.save(user);

    }
}
