package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "group_meetings")
@Getter
@Setter
public class GroupMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    private Appointment appointment;

    // Danh sách người tham gia cuộc họp nhóm
    @ManyToMany

    @JoinTable(
        name = "group_meeting_participants",
        joinColumns = @JoinColumn(name = "group_meeting_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    
    @JsonIgnore
    private List<User> participants;
    
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }
    
}
