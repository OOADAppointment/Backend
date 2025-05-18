package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JoinGroupMeetingDTO {
    private Integer id;
    private Integer appointmentId;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer userId;
}
