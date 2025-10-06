package org.example.authservice.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityFilter {

    private final PasswordEncoder passwordEncoder;

    public String Encoder(String pin){
        return passwordEncoder.encode(pin);
    }
}
