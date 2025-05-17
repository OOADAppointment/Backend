package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    public User getUserById(Integer id) {
        return userRepo.findById(id).orElse(null);
    }
    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }
    public User checkAccount(String username, String password) {
        return userRepo.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .orElse(null);
    }
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }


    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public void deleteUser(Integer id) {
        userRepo.deleteById(id);
    }
}
