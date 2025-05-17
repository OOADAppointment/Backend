package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Tự sinh getter, setter, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupMeetingDTO {
    private Integer userId;
    private Integer appointmentId;
}
