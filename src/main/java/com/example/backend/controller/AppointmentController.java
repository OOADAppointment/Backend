package com.example.backend.controller;

import com.example.backend.dto.AppointmentDTO;
import com.example.backend.dto.JoinGroupMeetingDTO;
import com.example.backend.model.Appointment;
import com.example.backend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    @Autowired private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Integer id) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
        return appointment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/check-or-create")
    public ResponseEntity<?> checkOrCreateAppointment(@RequestBody AppointmentDTO dto) {
        return ResponseEntity.ok(appointmentService.checkOrCreateAppointment(dto));
    }

    @PostMapping("/join-group-meeting")
    public ResponseEntity<?> joinGroupMeeting(@RequestBody JoinGroupMeetingDTO dto) {
        appointmentService.joinGroupMeeting(dto.getUserId(), dto.getAppointmentId());
        return ResponseEntity.ok("Joined group meeting successfully");
    }



    @PutMapping("update/{id}")
    public ResponseEntity<String> updateAppointment(@PathVariable Integer id, @RequestBody AppointmentDTO appointmentDTO) {
        String result = appointmentService.updateAppointment(id, appointmentDTO);
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        String result = appointmentService.addAppointment(appointmentDTO);
        if (result.contains("successfully") || result.contains("added to")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Integer id) {
        String result = appointmentService.deleteAppointment(id);
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }






    
}
