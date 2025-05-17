package com.example.backend.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter

public class AppointmentDTO {
    private String name;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer userId;
    private Boolean isGroupMeeting;
    private List<LocalDateTime> reminderTimes;
}