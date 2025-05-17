package com.example.backend.service;

import com.example.backend.dto.AppointmentDTO;
import com.example.backend.model.Appointment;
import com.example.backend.model.Calendar;
import com.example.backend.model.GroupMeeting;
import com.example.backend.model.Reminder;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private GroupMeetingRepository groupMeetingRepository;
    @Autowired private ReminderRepository reminderRepository;
    @Autowired private UserRepository userRepository;

    public String addAppointment(AppointmentDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return "Appointment name cannot be empty";
        }
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            return "End time cannot be before start time";
        }

        List<Appointment> overlapping = appointmentRepository
                .findByUserIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        dto.getUserId(), dto.getEndTime(), dto.getStartTime());

        if (!overlapping.isEmpty()) {
            return "Conflict: You already have an appointment during this time.";
        }

        Appointment appointment = new Appointment();
        appointment.setLocation(dto.getLocation());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
        appointment.setUser(userRepository.findById(dto.getUserId()).orElse(null));
        appointment.setIsGroupMeeting(dto.getIsGroupMeeting());
        appointmentRepository.save(appointment);

        if (dto.getReminderTimes() != null) {
            for (LocalDateTime time : dto.getReminderTimes()) {
                Reminder r = new Reminder();
                r.setAppointment(appointment);
                r.setReminderTime(time);
                r.setMessenger("System Notification");
                reminderRepository.save(r);
            }
        }

        List<Appointment> groupMeetings = appointmentRepository.findAll();
        for (Appointment a : groupMeetings) {
            if (a.getIsGroupMeeting() && a.getStartTime().equals(dto.getStartTime()) &&
                    a.getEndTime().equals(dto.getEndTime()) &&
                    a.getLocation().equals(dto.getLocation())) {
                GroupMeeting gm = new GroupMeeting();
                gm.setAppointment(a);
                gm.setUser(appointment.getUser());
                groupMeetingRepository.save(gm);
                return "You were added to an existing group meeting.";
            }
        }

        return "Appointment added successfully.";
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Integer id) {
        return appointmentRepository.findById(id);
    }

    public String updateAppointment(Integer id, AppointmentDTO dto) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (!optionalAppointment.isPresent()) {
            return "Appointment not found.";
        }

        Appointment appointment = optionalAppointment.get();
        appointment.setLocation(dto.getLocation());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
        appointment.setIsGroupMeeting(dto.getIsGroupMeeting());
        appointmentRepository.save(appointment);
        return "Appointment updated successfully.";
    }

    public String deleteAppointment(Integer id) {
        if (!appointmentRepository.existsById(id)) {
            return "Appointment not found.";
        }
        appointmentRepository.deleteById(id);
        return "Appointment deleted successfully.";
    }
}
