package com.example.application.config;
/* This class serve as a service for the application.
*/
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// Annotaion to mark this class as Spring Boot Component
@Component
public class SecurityService {
    // Context to handle authentication request
    private final AuthenticationContext authenticationContext;

    // Intialize context
    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    // Method to get the details of authenticated user
    public UserDetails getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class).get();
    }

    // Method to logout to application
    public void logout() {
        authenticationContext.logout();
    }
}
