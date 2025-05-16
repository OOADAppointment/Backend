package com.example.backend.controller;

import com.example.backend.dto.UserIdRequest;
import com.example.backend.model.Appointment;
import com.example.backend.model.GroupMeeting;
import com.example.backend.repository.CalendarRepository;
import com.example.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/calendar") // Base path for calendar related operations
public class CalendarController {

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    @Autowired
    public CalendarController(CalendarRepository calendarRepository, UserRepository userRepository) {
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;
    }

    // Endpoint for UI -> Calendar : getActiveDateTime()
    @GetMapping("/active-time")
    public ResponseEntity<LocalDateTime> getActiveDateTime() {
        return ResponseEntity.ok(LocalDateTime.now());
    }

    // Endpoint for UI -> Calendar : addAppointment()
    // This also handles validation and conflict check as per the sequence diagram.
    @PostMapping("/users/{userId}/appointments")
    public ResponseEntity<?> createAppointment(@PathVariable String userId,
                                               @RequestBody Appointment appointmentRequest,
                                               @RequestParam(required = false, defaultValue = "false") boolean replaceExisting) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
        }
        appointmentRequest.setUserId(userId); // Ensure appointment is associated with the path userId

        if (appointmentRequest.getStartTime() == null || appointmentRequest.getEndTime() == null ||
            appointmentRequest.getStartTime().isAfter(appointmentRequest.getEndTime())) {
            return ResponseEntity.badRequest().body("Invalid appointment times: start time must be before end time.");
        }

        Optional<Appointment> conflict = calendarRepository.findConflict(
                userId,
                appointmentRequest.getStartTime(),
                appointmentRequest.getEndTime(),
                null // No appointment to exclude during initial creation
        );

        if (conflict.isPresent()) {
            if (replaceExisting) {
                calendarRepository.deleteAppointment(userId, conflict.get().getId());
                // Proceed to save the new appointment after deleting the conflicting one
            } else {
                // Conflict found, and not replacing. Return conflict information.
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                     .body("Appointment conflicts with existing one: ID " + conflict.get().getId() +
                                           ", Name: '" + conflict.get().getName() + "'");
            }
        }

        Appointment savedAppointment = calendarRepository.saveAppointment(appointmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
    }

    @GetMapping("/users/{userId}/appointments")
    public ResponseEntity<?> getAppointmentsForUser(@PathVariable String userId) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
        }
        List<Appointment> appointments = calendarRepository.findAppointmentsByUserId(userId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable String appointmentId) {
        return calendarRepository.findAppointmentById(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> updateAppointment(@PathVariable String appointmentId,
                                               @RequestBody Appointment updatedAppointmentDetails) {
        Optional<Appointment> existingAppointmentOpt = calendarRepository.findAppointmentById(appointmentId);
        if (existingAppointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment existingAppointment = existingAppointmentOpt.get();
        String userId = existingAppointment.getUserId();

        // Apply updates from request to the existing appointment
        if (updatedAppointmentDetails.getName() != null) existingAppointment.setName(updatedAppointmentDetails.getName());
        if (updatedAppointmentDetails.getLocation() != null) existingAppointment.setLocation(updatedAppointmentDetails.getLocation());
        if (updatedAppointmentDetails.getStartTime() != null) existingAppointment.setStartTime(updatedAppointmentDetails.getStartTime());
        if (updatedAppointmentDetails.getEndTime() != null) existingAppointment.setEndTime(updatedAppointmentDetails.getEndTime());
        // Participant list updates would require more specific logic if allowed here

        if (existingAppointment.getStartTime().isAfter(existingAppointment.getEndTime())) {
            return ResponseEntity.badRequest().body("Invalid appointment times: start time must be before end time.");
        }

        Optional<Appointment> conflict = calendarRepository.findConflict(
                userId,
                existingAppointment.getStartTime(),
                existingAppointment.getEndTime(),
                appointmentId // Exclude self from conflict check
        );

        if (conflict.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("Updated appointment conflicts with another existing one: ID " + conflict.get().getId());
        }

        Appointment savedAppointment = calendarRepository.saveAppointment(existingAppointment);
        return ResponseEntity.ok(savedAppointment);
    }

    @DeleteMapping("/users/{userId}/appointments/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String userId, @PathVariable String appointmentId) {
         if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
        }
        boolean deleted = calendarRepository.deleteAppointment(userId, appointmentId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build(); // Appointment not found for this user
        }
    }

    // --- Group Meeting Endpoints ---

    @PostMapping("/group-meetings")
    public ResponseEntity<?> createGroupMeeting(@RequestBody GroupMeeting groupMeetingRequest) {
        if (groupMeetingRequest.getOrganizerId() == null || userRepository.findById(groupMeetingRequest.getOrganizerId()).isEmpty()) {
            return ResponseEntity.badRequest().body("Organizer ID is required and must be a valid user ID.");
        }
        if (groupMeetingRequest.getStartTime() == null || groupMeetingRequest.getEndTime() == null ||
            groupMeetingRequest.getStartTime().isAfter(groupMeetingRequest.getEndTime())) {
            return ResponseEntity.badRequest().body("Invalid meeting times: start time must be before end time.");
        }
        GroupMeeting savedGroupMeeting = calendarRepository.saveGroupMeeting(groupMeetingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGroupMeeting);
    }

    @GetMapping("/group-meetings")
    public ResponseEntity<List<GroupMeeting>> getAllGroupMeetings() {
        return ResponseEntity.ok(calendarRepository.findAllGroupMeetings());
    }

    @GetMapping("/group-meetings/{groupMeetingId}")
    public ResponseEntity<GroupMeeting> getGroupMeetingById(@PathVariable String groupMeetingId) {
        return calendarRepository.findGroupMeetingById(groupMeetingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/group-meetings/{groupMeetingId}/participants")
    public ResponseEntity<?> addParticipantToGroupMeeting(@PathVariable String groupMeetingId,
                                                          @RequestBody UserIdRequest participantRequest) {
        if (userRepository.findById(participantRequest.getUserId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Participant user not found with ID: " + participantRequest.getUserId());
        }
        Optional<GroupMeeting> groupMeetingOpt = calendarRepository.findGroupMeetingById(groupMeetingId);
        if (groupMeetingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group meeting not found with ID: " + groupMeetingId);
        }

        boolean added = calendarRepository.addParticipantToGroupMeeting(groupMeetingId, participantRequest.getUserId());
        if (added) {
            GroupMeeting gm = groupMeetingOpt.get(); // Get the meeting details
            // Create a delegate appointment in the participant's calendar
            Appointment delegateAppointment = new Appointment(
                participantRequest.getUserId(),
                gm.getName(), // Use group meeting's name
                gm.getLocation(),
                gm.getStartTime(),
                gm.getEndTime()
            );
            // Potentially check for conflict for this delegate appointment before saving
            calendarRepository.saveAppointment(delegateAppointment);
            return ResponseEntity.ok(calendarRepository.findGroupMeetingById(groupMeetingId).get()); // Return updated meeting
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add participant (possibly already a participant).");
        }
    }

    // Endpoint for "Join Group Meeting" option from conflict resolution
    @PostMapping("/appointments/join-group-meeting")
    public ResponseEntity<?> joinMatchingGroupMeeting(@RequestParam String conflictingAppointmentId,
                                                      @RequestParam String userId) { // userId of the person whose appointment is conflicting
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + userId);
        }
        Optional<Appointment> conflictingAppointmentOpt = calendarRepository.findAppointmentById(conflictingAppointmentId);
        if (conflictingAppointmentOpt.isEmpty() || !conflictingAppointmentOpt.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conflicting appointment not found for this user.");
        }
        Appointment conflictingAppointment = conflictingAppointmentOpt.get();

        List<GroupMeeting> matchingMeetings = calendarRepository.findMatchingGroupMeetings(
            conflictingAppointment.getName(), conflictingAppointment.getStartTime(),
            conflictingAppointment.getEndTime(), conflictingAppointment.getLocation()
        );

        if (matchingMeetings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matching group meeting found to join.");
        }
        GroupMeeting targetMeeting = matchingMeetings.get(0); // Join the first match

        boolean addedToGM = calendarRepository.addParticipantToGroupMeeting(targetMeeting.getId(), userId);
        if (addedToGM) {
            calendarRepository.deleteAppointment(userId, conflictingAppointmentId); // Remove original conflicting appointment
            // A delegate appointment for this user in the group meeting should be created by addParticipantToGroupMeeting logic if not already present
            // Or ensure one is created here if addParticipantToGroupMeeting doesn't cover it for the original user.
            // The current addParticipantToGroupMeeting creates a delegate, so this should be fine.
            return ResponseEntity.ok(calendarRepository.findGroupMeetingById(targetMeeting.getId()).get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to join group meeting (possibly already a participant).");
        }
    }

    // Placeholder for adding reminders - requires Reminder model and DTO
    @PostMapping("/appointments/{appointmentId}/reminders")
    public ResponseEntity<?> addReminderToAppointment(@PathVariable String appointmentId,
                                                      @RequestBody Object reminderDetails) { // Replace Object with ReminderRequestDto
        if (calendarRepository.findAppointmentById(appointmentId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found.");
        }
        // Actual logic to create and associate reminder would go here
        System.out.println("Reminder details received for appointment " + appointmentId + ": " + reminderDetails.toString());
        return ResponseEntity.ok("Reminder conceptually added to appointment " + appointmentId + ". (Full implementation requires Reminder model/logic)");
    }
}
