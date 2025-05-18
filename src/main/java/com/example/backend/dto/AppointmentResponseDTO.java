package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class AppointmentResponseDTO {
    private Integer id;
    private String owner; // là tên người dùng tạo cuộc hẹn, ví dụ: "alice"
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isGroupMeeting;
    private List<String> members; // danh sách tên thành viên, ví dụ: ["ly", "anh"]
}
