package com.example.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AppointmentResponseDTO {
    private Integer id;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isGroupMeeting;
    private Integer userId; // Chỉ trả về userId
}
