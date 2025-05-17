package com.example.backend.repository;


import com.example.backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByUser_IdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        Integer userId, LocalDateTime end, LocalDateTime start);

    Optional<Appointment> findByUser_IdAndStartTimeAndEndTimeAndLocation(
        Integer userId, LocalDateTime start, LocalDateTime end, String location);

    @Query("SELECT a FROM Appointment a WHERE a.title = :title AND a.isGroupMeeting = true " +
           "AND a.startTime = :start AND a.endTime = :end")
    Optional<Appointment> findMatchingGroupMeeting(@Param("title") String title,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId AND " +
           "((:startTime < a.endTime) AND (:endTime > a.startTime))")
    List<Appointment> findConflictingAppointments(@Param("userId") Integer userId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);
}

