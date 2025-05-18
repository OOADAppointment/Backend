package com.example.backend.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReminderDTO {
    private Integer appointmentId;
    private LocalDateTime reminderTime;
    private Integer userId;
    private String message;
}
