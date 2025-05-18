package com.example.backend.controller;

import com.example.backend.dto.AppointmentDTO;
import com.example.backend.dto.AppointmentResponseDTO;
import com.example.backend.dto.JoinGroupMeetingDTO;
import com.example.backend.model.Appointment;
import com.example.backend.model.User;
import com.example.backend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            dto.setOwner(a.getUser().getUsername()); // hoặc getName()

            if (a.getIsGroupMeeting() && a.getGroupMeeting() != null) {
                List<String> memberNames = a.getGroupMeeting().getParticipants()
                    .stream()
                    .map(User::getUsername) // hoặc getName nếu bạn có
                    .collect(Collectors.toList());
                dto.setMembers(memberNames);
            } else {
                dto.setMembers(List.of());
            }


            return dto;
        }).toList();
    }


    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable Integer id) {
        Optional<Appointment> optional = appointmentService.getAppointmentById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment a = optional.get();

        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setLocation(a.getLocation());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setIsGroupMeeting(a.getIsGroupMeeting());
        dto.setOwner(a.getUser().getUsername());

        if (a.getIsGroupMeeting() && a.getGroupMeeting() != null) {
            List<String> memberNames = a.getGroupMeeting().getParticipants()
                .stream()
                .map(User::getUsername) // hoặc getName nếu bạn có
                .collect(Collectors.toList());
            dto.setMembers(memberNames);
        } else {
            dto.setMembers(List.of());
        }


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
