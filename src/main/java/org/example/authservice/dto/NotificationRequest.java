package org.example.authservice.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String to;
    private String body;
    private String filePath;
}
