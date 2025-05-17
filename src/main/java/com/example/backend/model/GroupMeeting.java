package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "group_meetings")
@Getter
@Setter
public class GroupMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Một cuộc họp nhóm liên kết với 1 Appointment
    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    // Danh sách người tham gia cuộc họp nhóm
    @ManyToMany
    @JoinTable(
        name = "group_meeting_participants",
        joinColumns = @JoinColumn(name = "group_meeting_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;
}
