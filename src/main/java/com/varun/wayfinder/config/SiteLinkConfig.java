package com.varun.wayfinder.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SiteLinkConfig implements CommandLineRunner {

    @Override
    public void run(String... args){
        System.out.println("=============================================================");
        System.out.println("Server Started at http://localhost:8080");
        System.out.println("=============================================================");
    }
}