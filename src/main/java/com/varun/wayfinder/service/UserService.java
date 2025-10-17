package com.varun.wayfinder.service;

import com.varun.wayfinder.model.User;
import com.varun.wayfinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean registerUser(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            return false; // username already exists
        }
        User user = new User(username, password);
        userRepository.save(user);
        return true;
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) return false;
        return user.getPassword().equals(password);
    }
}
