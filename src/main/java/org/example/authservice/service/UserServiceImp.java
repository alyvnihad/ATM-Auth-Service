package org.example.authservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.AuthenticationUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService{
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String cardNumber) {
        User user = userRepository.findByCardNumber(Long.valueOf(cardNumber)).orElseThrow(() -> new RuntimeException("Not found card Number"));
        AuthenticationUser authenticationUser = new AuthenticationUser();
        authenticationUser.setId(user.getId());
        authenticationUser.setName(user.getName());
        authenticationUser.setEmail(user.getEmail());
        authenticationUser.setCardNumber(user.getCardNumber());
        authenticationUser.setPinHash(user.getPinHash());
        authenticationUser.setCurrency(Collections.singletonList(user.getCurrency().name()));
        authenticationUser.setRole(Collections.singletonList(user.getRole().name()));
        return authenticationUser;
    }
}
