package com.example.backend.dto;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    @NotNull
    private String owner;
    @NotBlank
    private String title;

    private String location;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    private Boolean isGroupMeeting;
    private List<String> members;
    private List<LocalDateTime> reminderTimes;
}
