package com.example.backend.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ElementCollection
    private List<String> appointments = new ArrayList<>();

    public boolean addAppointment(String appointment) {
        if (!appointments.contains(appointment)) {
            appointments.add(appointment);
            return true;
        }
        return false;
    }

    public boolean checkConflict(String appointment) {
        return appointments.contains(appointment);
    }

    public boolean replaceAppointment(String oldApt, String newApt) {
        int index = appointments.indexOf(oldApt);
        if (index != -1) {
            appointments.set(index, newApt);
            return true;
        }
        return false;
    }

    public void joinGroupMeeting(String meetingId) {
        appointments.add(meetingId);
    }

    public List<String> getAppointments() {
        return appointments;
    }
}
