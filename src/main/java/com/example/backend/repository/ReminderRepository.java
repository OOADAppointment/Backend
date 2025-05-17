package com.example.backend.repository;
import java.util.List;
import com.example.backend.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Integer> {
    List<Reminder> findByAppointmentId(Integer appointmentId);
}
