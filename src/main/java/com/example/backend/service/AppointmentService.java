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
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

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
            groupMeeting.setParticipants(new ArrayList<>(List.of(user)));
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

    public String addAppointment(AppointmentDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return "Appointment name cannot be empty";
        }
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            return "End time cannot be before start time";
        }

        List<Appointment> overlapping = appointmentRepository
                .findByUser_IdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        dto.getUserId(), dto.getEndTime(), dto.getStartTime());

        if (!overlapping.isEmpty()) {
            return "Conflict: You already have an appointment during this time.";
        }

        // Tạo Appointment mới
        Appointment appointment = new Appointment();
        appointment.setLocation(dto.getLocation());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
        appointment.setTitle(dto.getTitle());
        appointment.setUser(userRepository.findById(dto.getUserId()).orElse(null));
        appointment.setIsGroupMeeting(dto.getIsGroupMeeting());
        appointmentRepository.save(appointment);

        // Thêm reminder nếu có
        if (dto.getReminderTimes() != null) {
            for (LocalDateTime time : dto.getReminderTimes()) {
                Reminder r = new Reminder();
                r.setAppointment(appointment);
                r.setReminderTime(time);
                r.setUser(appointment.getUser());
                reminderRepository.save(r);
            }
        }

        // Kiểm tra xem có group meeting trùng nào không
        List<GroupMeeting> existingMeetings = groupMeetingRepository.findAll();
        for (GroupMeeting gm : existingMeetings) {
            Appointment a = gm.getAppointment();
            if (a.getIsGroupMeeting()
                    && a.getStartTime().equals(dto.getStartTime())
                    && a.getEndTime().equals(dto.getEndTime())
                    && a.getLocation().equals(dto.getLocation())) {

                // Nếu đã tồn tại GroupMeeting, thêm user mới vào participants
                User user = appointment.getUser();
                if (!gm.getParticipants().contains(user)) {
                    gm.getParticipants().add(user);
                    groupMeetingRepository.save(gm);
                }

                return "You were added to an existing group meeting.";
            }
        }

        // Nếu chưa có group meeting nào trùng thì tạo mới
        if (dto.getIsGroupMeeting()) {
            GroupMeeting newGm = new GroupMeeting();
            newGm.setAppointment(appointment);
            List<User> participants = new ArrayList<>();
            participants.add(appointment.getUser());
            newGm.setParticipants(participants);
            groupMeetingRepository.save(newGm);
        }

        return "Appointment added successfully.";
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
