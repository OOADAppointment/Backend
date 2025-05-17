package com.example.backend.repository;

import com.example.backend.model.Appointment;
import com.example.backend.model.GroupMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMeetingRepository extends JpaRepository<GroupMeeting, Integer> {
    List<GroupMeeting> findByAppointment(Appointment appointment);
}
