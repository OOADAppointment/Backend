package com.example.backend.repository;

import com.example.backend.model.Appointment;
import com.example.backend.model.GroupMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupMeetingRepository extends JpaRepository<GroupMeeting, Integer> {
    Optional<GroupMeeting> findByAppointment(Appointment appointment);
    List<GroupMeeting> findByAppointmentId(Integer appointmentId);
}
