package org.example.authservice.dto;

import lombok.Data;
import org.example.authservice.model.Currency;

@Data
public class AccountResponse {
    private Currency currency;
    private Long cardNumber;
}
