package org.example.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.UserDTO;
import org.example.authservice.payload.LoginPayload;
import org.example.authservice.payload.RefreshTokenPayload;
import org.example.authservice.payload.RegisterPayload;
import org.example.authservice.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(@RequestBody RegisterPayload payload){
        authService.register(payload);
    }

    @PostMapping("/login")
    public UserDTO login(@RequestBody LoginPayload payload){
        return authService.login(payload);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshTokenPayload payload){
        authService.logout(payload);

    }
}
