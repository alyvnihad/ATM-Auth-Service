package org.example.authservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService  extends UserDetailsService {
    UserDetails loadUserByUsername(String cardNumber);
}
