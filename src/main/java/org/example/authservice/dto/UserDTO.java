package org.example.authservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private Long cardNumber;
    private String email;
    private String name;
    private List<String> currency;
    private String role;
    private String accessToken;
    private String refreshToken;

}
