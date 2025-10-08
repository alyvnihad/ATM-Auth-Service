package org.example.authservice.security;

import lombok.RequiredArgsConstructor;
import org.example.authservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public AuthenticationManager authManager(HttpSecurity httpSecurity, UserService userService, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder managerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        managerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        return managerBuilder.build();
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth
                .requestMatchers("api/auth/register", "api/auth/login", "api/auth/logout").permitAll()
                .anyRequest().authenticated()
        ).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
