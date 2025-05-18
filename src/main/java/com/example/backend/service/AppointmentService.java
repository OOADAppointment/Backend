package com.example.backend.service;
import com.example.backend.model.User;
import com.example.backend.dto.AppointmentDTO;
import com.example.backend.model.Appointment;
import com.example.backend.model.GroupMeeting;
import com.example.backend.model.Reminder;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;


@Service
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private GroupMeetingRepository groupMeetingRepository;
    @Autowired private ReminderRepository reminderRepository;
    @Autowired private UserRepository userRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Integer id) {
        return appointmentRepository.findById(id);
    }

    public Object checkOrCreateAppointment(AppointmentDTO dto) {
        // Kiểm tra xem có group meeting trùng không
        Optional<Appointment> existingGroup = appointmentRepository
                .findMatchingGroupMeeting(dto.getTitle(), dto.getStartTime(), dto.getEndTime());

        if (existingGroup.isPresent()) {
            Appointment groupApp = existingGroup.get();
            Optional<GroupMeeting> optionalGroupMeeting = groupMeetingRepository.findByAppointment(groupApp);
            GroupMeeting groupMeeting = optionalGroupMeeting.orElseThrow(() -> new RuntimeException("Group meeting not found"));
            List<String> participantNames = groupMeeting.getParticipants()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

            // Nếu có trùng -> trả về thông tin appointment để FE xử lý chọn
            return Map.of(
                "status", "GROUP_MEETING_EXISTS",
                "appointmentId", groupApp.getId(),
                "title", groupApp.getTitle(),
                "location", groupApp.getLocation(),
                "startTime", groupApp.getStartTime(),
                "endTime", groupApp.getEndTime(),
                "participantCount", participantNames.size(),
                "participants", participantNames
            );
        }

        // Nếu không có -> tạo mới appointment
        User user = userRepository.findByUsername(dto.getOwner())
            .orElseThrow(() -> new RuntimeException("Owner user not found"));

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setTitle(dto.getTitle());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
        appointment.setLocation(dto.getLocation());
        appointment.setIsGroupMeeting(dto.getIsGroupMeeting());

        Appointment saved = appointmentRepository.save(appointment);

        // Nếu là group meeting, tạo GroupMeeting record và thêm user là participant
        if (dto.getIsGroupMeeting() != null && dto.getIsGroupMeeting()) {
            GroupMeeting groupMeeting = new GroupMeeting();
            groupMeeting.setAppointment(saved);

            List<User> participants = new ArrayList<>();
            // Thêm owner
            participants.add(user);

            // Thêm members khác nếu có
            if (dto.getMembers() != null && !dto.getMembers().isEmpty()) {
                List<User> members = userRepository.findAllByUsernameIn(dto.getMembers());
                participants.addAll(members);
            }

            groupMeeting.setParticipants(participants);
            groupMeetingRepository.save(groupMeeting);
        }


        return Map.of("status", "CREATED", "appointmentId", saved.getId());
    }

    public void joinGroupMeeting(Integer userId, Integer appointmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        GroupMeeting groupMeeting = groupMeetingRepository.findByAppointment(appointment)
                .orElseThrow(() -> new RuntimeException("GroupMeeting not found"));

        if (!groupMeeting.getParticipants().contains(user)) {
            groupMeeting.getParticipants().add(user);
            groupMeetingRepository.save(groupMeeting);
        }
    }

    public List<Appointment> getConflictingAppointments(Integer userId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentRepository.findConflictingAppointments(userId, startTime, endTime);
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

        // Xóa các reminder liên quan trước
        List<Reminder> reminders = reminderRepository.findByAppointmentId(id);
        reminderRepository.deleteAll(reminders);

        // Xóa các group meeting liên quan trước
        List<GroupMeeting> groupMeetings = groupMeetingRepository.findByAppointmentId(id);
        groupMeetingRepository.deleteAll(groupMeetings);

        // Sau đó mới xóa appointment
        appointmentRepository.deleteById(id);

        return "Appointment deleted successfully.";
    }
    

}
