package com.varun.wayfinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    // ---------- Primary Key ----------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------- Authentication ----------
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // ---------- Profile Info ----------
    private String fullName;
    private String email;
    private String contactNumber;
    private String country;
    private String zipCode;

    @Column(length = 500)
    private String bio;

    private String profileImageUrl = "https://via.placeholder.com/100";

    // ---------- Constructors ----------
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
