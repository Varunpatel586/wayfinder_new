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
    // Profile info with safe defaults
    private String fullName      = "New User";
    private String email         = "not.provided@wayfinder.local";
    private String contactNumber = "N/A";
    private String country       = "India";
    private String zipCode       = "00000";

    @Column(length = 500)
    private String bio           = "Hey there!I’m new to WayFinder.";

    private String profileImageUrl = "https://media.istockphoto.com/id/1223671392/vector/default-profile-picture-avatar-photo-placeholder-vector-illustration.jpg?s=612x612&w=0&k=20&c=s0aTdmT5aU6b8ot7VKm11DeID6NctRCpB755rA1BIP0=";

    // ---------- Constructors ----------
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.fullName = username;
    }
}
