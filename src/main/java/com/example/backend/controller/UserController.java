package com.example.backend.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import com.example.backend.model.User;
import com.example.backend.service.UserService;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET: Lấy tất cả người dùng
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET: Lấy người dùng theo ID
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    // POST: Tạo người dùng mới
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    // PUT: Cập nhật tên người dùng
    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        return userService.getUserById(id)
            .map(user -> {
                user.setUsername(updatedUser.getUsername());
                return userService.saveUser(user);
            })
            .orElseGet(() -> {
                updatedUser.setId(id);
                return userService.saveUser(updatedUser);
            });
    }

    // DELETE: Xóa người dùng theo ID
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }
}