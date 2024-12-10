package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@Push
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}) // Annotation to declare the class as a Spring Boot application and exclude Spring Security auto-configuration
@EnableScheduling
@Theme(value = "managementflow", variant = Lumo.DARK) // Annotation to define the Vaadin theme for the application
@PWA(
     name = "Student Management System", // Name of the Progressive Web Application
     shortName = "SMS", // Short name of the application
     offlinePath="offline.html", // Path to the HTML file to be displayed when the application is offline
     offlineResources = { "images/offline.png" } // Resources to be cached and used when the application is offline
)
public class Application extends SpringBootServletInitializer implements AppShellConfigurator { // Main class of the application implementing AppShellConfigurator interface

    public static void main(String[] args) { // Main method
        SpringApplication.run(Application.class, args); // Start the Spring Boot application
    }
}
