package com.example.backend.repository;

import com.example.backend.model.Appointment;
import com.example.backend.model.GroupMeeting;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class CalendarRepository {

    // In-memory store: UserID -> List of Appointments
    private final Map<String, List<Appointment>> userAppointments = new ConcurrentHashMap<>();
    // In-memory store: GroupMeetingID -> GroupMeeting
    private final Map<String, GroupMeeting> groupMeetings = new ConcurrentHashMap<>();

    // --- Appointment Methods ---

    public Appointment saveAppointment(Appointment appointment) {
        if (appointment.getId() == null || appointment.getId().isEmpty()) {
            appointment.setId(UUID.randomUUID().toString());
        }
        userAppointments.computeIfAbsent(appointment.getUserId(), k -> new ArrayList<>())
                .removeIf(a -> a.getId().equals(appointment.getId())); // Remove old if updating
        userAppointments.get(appointment.getUserId()).add(appointment);
        return appointment;
    }

    public Optional<Appointment> findAppointmentById(String appointmentId) {
        return userAppointments.values().stream()
                .flatMap(List::stream)
                .filter(a -> a.getId().equals(appointmentId))
                .findFirst();
    }

    public List<Appointment> findAppointmentsByUserId(String userId) {
        return userAppointments.getOrDefault(userId, Collections.emptyList())
                               .stream()
                               .collect(Collectors.toList()); // Return a copy
    }

    public boolean deleteAppointment(String userId, String appointmentId) {
        List<Appointment> appointments = userAppointments.get(userId);
        if (appointments != null) {
            return appointments.removeIf(a -> a.getId().equals(appointmentId));
        }
        return false;
    }

    public Optional<Appointment> findConflict(String userId, LocalDateTime startTime, LocalDateTime endTime, String excludeAppointmentId) {
        List<Appointment> appointments = userAppointments.getOrDefault(userId, Collections.emptyList());
        for (Appointment appt : appointments) {
            if (excludeAppointmentId != null && appt.getId().equals(excludeAppointmentId)) {
                continue;
            }
            // Check for overlap: (StartA < EndB) and (EndA > StartB)
            if (startTime.isBefore(appt.getEndTime()) && endTime.isAfter(appt.getStartTime())) {
                return Optional.of(appt);
            }
        }
        return Optional.empty();
    }

    // --- GroupMeeting Methods ---

    public GroupMeeting saveGroupMeeting(GroupMeeting groupMeeting) {
        if (groupMeeting.getId() == null || groupMeeting.getId().isEmpty()) {
            groupMeeting.setId(UUID.randomUUID().toString());
        }
        groupMeetings.put(groupMeeting.getId(), groupMeeting);
        return groupMeeting;
    }

    public Optional<GroupMeeting> findGroupMeetingById(String groupMeetingId) {
        return Optional.ofNullable(groupMeetings.get(groupMeetingId));
    }

    public List<GroupMeeting> findAllGroupMeetings() {
        return new ArrayList<>(groupMeetings.values());
    }

    public List<GroupMeeting> findMatchingGroupMeetings(String name, LocalDateTime startTime, LocalDateTime endTime, String location) {
        return groupMeetings.values().stream()
                .filter(gm -> (name == null || gm.getName().equalsIgnoreCase(name)) &&
                               (startTime == null || gm.getStartTime().equals(startTime)) &&
                               (endTime == null || gm.getEndTime().equals(endTime)) &&
                               (location == null || gm.getLocation().equalsIgnoreCase(location)))
                .collect(Collectors.toList());
    }

    public boolean deleteGroupMeeting(String groupMeetingId) {
        return groupMeetings.remove(groupMeetingId) != null;
    }

    public boolean addParticipantToGroupMeeting(String groupMeetingId, String userId) {
        GroupMeeting meeting = groupMeetings.get(groupMeetingId);
        if (meeting != null) {
            if (!meeting.getParticipantIds().contains(userId)) {
                meeting.getParticipantIds().add(userId);
                return true;
            }
        }
        return false;
    }

    // Helper methods for testing/clearing data
    public void clearAllAppointments() {
        userAppointments.clear();
    }

    public void clearAllGroupMeetings() {
        groupMeetings.clear();
    }
}
