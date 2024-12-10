package com.example.application.repository;

import com.example.application.data.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);
    AppUser findByPassword(String password);

    @Query("SELECT c FROM AppUser c " +
       "WHERE (LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<AppUser> search(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM AppUser u " +
       "WHERE (LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(:fullName))")
    AppUser findByFullName(String fullName);
}
