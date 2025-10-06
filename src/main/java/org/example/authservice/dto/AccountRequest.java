package org.example.authservice.dto;

import lombok.Data;

@Data
public class AccountRequest {
    private Long cardNumber;
    private String pin;
}
