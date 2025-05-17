package com.example.backend.repository;


import com.example.backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByUserIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Integer userId, LocalDateTime end, LocalDateTime start);
    Optional<Appointment> findByUserIdAndStartTimeAndEndTimeAndLocation(Integer userId, LocalDateTime start, LocalDateTime end, String location);
}

