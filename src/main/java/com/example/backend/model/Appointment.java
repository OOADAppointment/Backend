package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "Appointment")
@Data
@Setter
@Getter

public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "title")
    private String title;

    @Column(name = "location")
    private String location;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    private User user;


    @Column(name = "is_group_meeting")
    private Boolean isGroupMeeting;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private GroupMeeting groupMeeting;

}