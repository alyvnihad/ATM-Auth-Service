package org.example.authservice.dto;

import lombok.Data;
import org.example.authservice.model.Currency;

@Data
public class CardResponse {
    private Long accountNumber;
    private Currency currency;
    private Long cardNumber;
    private String paymentNetwork;
}
