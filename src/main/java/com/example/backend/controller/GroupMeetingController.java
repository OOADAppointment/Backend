package com.example.backend.controller;

import com.example.backend.dto.AppointmentResponseDTO;
import com.example.backend.dto.JoinGroupMeetingDTO;
import com.example.backend.model.Appointment;
import com.example.backend.model.GroupMeeting;
import com.example.backend.model.User;
import com.example.backend.service.GroupMeetingService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/group-meetings")
public class GroupMeetingController {

    @Autowired
    private GroupMeetingService groupMeetingService;

    @Autowired
    private UserService userService;

    // Get all group meetings
    @GetMapping
    public List<JoinGroupMeetingDTO> getAllGroupMeetings() {
        List<GroupMeeting> meetings = groupMeetingService.getAllGroupMeetings();

        return meetings.stream().map(gm -> {
            Appointment appt = gm.getAppointment();

            JoinGroupMeetingDTO dto = new JoinGroupMeetingDTO();
            dto.setId(gm.getId());
            dto.setAppointmentId(appt.getId());
            dto.setTitle(appt.getTitle());
            dto.setLocation(appt.getLocation());
            dto.setStartTime(appt.getStartTime());
            dto.setEndTime(appt.getEndTime());
            dto.setUserId(appt.getUser().getId());

            return dto;
        }).toList();
    }
    @GetMapping("/{id}")
    public ResponseEntity<JoinGroupMeetingDTO> getGroupMeetingById(@PathVariable Integer id) {
        GroupMeeting gm = groupMeetingService.getGroupMeetingById(id);

        if (gm == null) {
            return ResponseEntity.notFound().build(); // Trả về 404 nếu không tìm thấy
        }

        Appointment appt = gm.getAppointment();
        JoinGroupMeetingDTO dto = new JoinGroupMeetingDTO();

        dto.setId(gm.getId());
        dto.setAppointmentId(appt.getId());
        dto.setTitle(appt.getTitle());
        dto.setLocation(appt.getLocation());
        dto.setStartTime(appt.getStartTime());
        dto.setEndTime(appt.getEndTime());
        dto.setUserId(appt.getUser().getId());

        return ResponseEntity.ok(dto);
    }

    // Create new group meeting
    @PostMapping
    public ResponseEntity<GroupMeeting> createGroupMeeting(@RequestBody GroupMeeting groupMeeting) {
        GroupMeeting saved = groupMeetingService.createGroupMeeting(groupMeeting);
        return ResponseEntity.ok(saved);
    }

    // Add participants to group meeting
    @PostMapping("/{meetingId}/participants")
    public ResponseEntity<?> addParticipants(
            @PathVariable Integer meetingId,
            @RequestBody List<Integer> userIds) {

        GroupMeeting meeting = groupMeetingService.getGroupMeetingById(meetingId);
        if (meeting == null) return ResponseEntity.notFound().build();

        for (Integer userId : userIds) {
            User user = userService.getUserById(userId);
            if (user != null) {
                meeting.getParticipants().add(user);
            }
        }

        GroupMeeting updated = groupMeetingService.saveGroupMeeting(meeting);
        return ResponseEntity.ok(updated);
    }

    // Delete group meeting
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroupMeeting(@PathVariable Integer id) {
        boolean deleted = groupMeetingService.deleteGroupMeeting(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
