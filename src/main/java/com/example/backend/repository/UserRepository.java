package com.example.backend.repository;


import com.example.backend.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findById(Integer id);
    List<User> findAllByUsernameIn(List<String> usernames);

}