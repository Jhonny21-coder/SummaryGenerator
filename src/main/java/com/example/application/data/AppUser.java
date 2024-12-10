package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String profileImage;
    private String verification;
    private boolean enabled;
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<History> histories;

    public String getFullName(){
	return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if it's the same instance
        if (o == null || getClass() != o.getClass()) return false; // Check class compatibility
        AppUser user = (AppUser) o;
        return Objects.equals(id, user.id); // Check equality based on 'id'
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Generate consistent hash code based on 'id'
    }
}
