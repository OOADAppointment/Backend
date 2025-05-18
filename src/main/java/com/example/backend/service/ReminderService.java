package com.example.backend.service;

import com.example.backend.dto.ReminderDTO;
import com.example.backend.model.Appointment;
import com.example.backend.model.Reminder;
import com.example.backend.model.User;
import com.example.backend.repository.AppointmentRepository;
import com.example.backend.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final AppointmentRepository appointmentRepository;

    public Reminder createReminder(ReminderDTO dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        User user = appointment.getUser();

        Reminder reminder = new Reminder();
        reminder.setAppointment(appointment);
        reminder.setReminderTime(dto.getReminderTime());
        reminder.setUser(user);
        reminder.setMessage(dto.getMessage());

        return reminderRepository.save(reminder);
    }
    @Scheduled(cron = "0 0 * * * *")
    public void checkRemindersToNotify() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusDays(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime to = from.plusHours(1);

        List<Reminder> reminders = reminderRepository.findByReminderTimeBetween(from, to);
        for (Reminder reminder : reminders) {
            sendReminder(reminder);
        }
    }

    private void sendReminder(Reminder reminder) {
        System.out.println("📢 Reminder: " + reminder.getMessage());
    }
}
