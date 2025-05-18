package com.example.backend.controller;

import com.example.backend.dto.ReminderDTO;
import com.example.backend.model.Reminder;
import com.example.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor

public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<Reminder> createReminder(@RequestBody ReminderDTO dto) {
        Reminder reminder = reminderService.createReminder(dto);
        return ResponseEntity.ok(reminder);
    }
}
