package com.example.backend.controller;

import com.example.backend.model.GroupMeeting;
import com.example.backend.model.User;
import com.example.backend.service.GroupMeetingService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-meetings")
public class GroupMeetingController {

    @Autowired
    private GroupMeetingService groupMeetingService;

    @Autowired
    private UserService userService;

    // Get all group meetings
    @GetMapping
    public List<GroupMeeting> getAllGroupMeetings() {
        return groupMeetingService.getAllGroupMeetings();
    }

    // Get group meeting by ID
    @GetMapping("/{id}")
    public ResponseEntity<GroupMeeting> getGroupMeetingById(@PathVariable Integer id) {
        GroupMeeting groupMeeting = groupMeetingService.getGroupMeetingById(id);
        return groupMeeting != null ? ResponseEntity.ok(groupMeeting) : ResponseEntity.notFound().build();
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
