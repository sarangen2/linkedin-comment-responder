package com.example.linkedin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for LinkedIn Comment Responder.
 * Enables Spring Boot auto-configuration and scheduling support.
 */
@SpringBootApplication
@EnableScheduling
public class LinkedInCommentResponderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkedInCommentResponderApplication.class, args);
    }
}
