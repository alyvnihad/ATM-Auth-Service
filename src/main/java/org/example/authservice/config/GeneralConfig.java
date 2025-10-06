package org.example.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeneralConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new Argon2PasswordEncoder( 16, 32, 1, 65536, 3 );
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


}
