package org.example.authservice.payload;

import lombok.Data;
import org.example.authservice.model.Currency;
import org.example.authservice.model.Role;

@Data
public class RegisterPayload {
    private Long cardNumber;
    private String name;
    private String email;
    private String pin;
    private Currency currency;
    private Role role;
}
