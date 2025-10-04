package org.example.authservice.payload;

import lombok.Data;

@Data
public class RefreshTokenPayload {
    private String refreshToken;
}
