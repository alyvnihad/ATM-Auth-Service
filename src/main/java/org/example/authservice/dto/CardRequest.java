package org.example.authservice.dto;

import lombok.Data;
import org.example.authservice.model.Currency;

@Data
public class CardRequest {
    private String pin;
    private Currency currency;
}
