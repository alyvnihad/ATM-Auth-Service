package org.example.authservice.payload;

import lombok.Data;

@Data
public class LoginPayload {
    private Long cardNumber;
    private String pin;
}
