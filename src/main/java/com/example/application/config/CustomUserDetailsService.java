package com.example.application.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private DataSource dataSource;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String queryUser = "SELECT email, password, enabled FROM app_user WHERE email=?";
        String queryRoles = "SELECT role FROM app_user WHERE email=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement userStatement = connection.prepareStatement(queryUser);
             PreparedStatement rolesStatement = connection.prepareStatement(queryRoles)) {

            userStatement.setString(1, email);
            ResultSet userResult = userStatement.executeQuery();

            if (userResult.next()) {
                String password = userResult.getString("password");
                boolean enabled = userResult.getBoolean("enabled");

                // Fetch roles
                rolesStatement.setString(1, email);
                ResultSet rolesResult = rolesStatement.executeQuery();

                List<GrantedAuthority> authorities = new ArrayList<>();
                while (rolesResult.next()) {
                    String role = rolesResult.getString("role");
                    authorities.add(new SimpleGrantedAuthority(role)); // Ensure roles are prefixed with "ROLE_"
                }

                return new CustomUserDetails(email, password, enabled, authorities);
            } else {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
