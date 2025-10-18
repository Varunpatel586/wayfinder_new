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

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateUser(String username, User user) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            existingUser.setFullName(user.getFullName());
            existingUser.setEmail(user.getEmail());
            existingUser.setCountry(user.getCountry());
            existingUser.setContactNumber(user.getContactNumber());
            existingUser.setZipCode(user.getZipCode());
            existingUser.setBio(user.getBio());
            userRepository.save(existingUser);
        }
    }
}
