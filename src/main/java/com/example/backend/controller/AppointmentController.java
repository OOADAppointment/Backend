package com.example.backend.controller;

import com.example.backend.dto.AppointmentDTO;
import com.example.backend.dto.AppointmentResponseDTO;
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
    public List<AppointmentResponseDTO> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return appointments.stream().map(a -> {
            AppointmentResponseDTO dto = new AppointmentResponseDTO();
            dto.setId(a.getId());
            dto.setTitle(a.getTitle());
            dto.setLocation(a.getLocation());
            dto.setStartTime(a.getStartTime());
            dto.setEndTime(a.getEndTime());
            dto.setIsGroupMeeting(a.getIsGroupMeeting());
            dto.setUserId(a.getUser().getId()); // Lấy userId duy nhất
            return dto;
        }).toList();
    }


    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable Integer id) {
        Optional<Appointment> optional = appointmentService.getAppointmentById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appointment = optional.get();

        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setTitle(appointment.getTitle());
        dto.setLocation(appointment.getLocation());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setIsGroupMeeting(appointment.getIsGroupMeeting());
        dto.setUserId(appointment.getUser().getId());

        return ResponseEntity.ok(dto);
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
