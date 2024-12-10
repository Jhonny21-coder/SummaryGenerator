package com.example.application.service;

import com.example.application.data.History;
import com.example.application.data.AppUser;
import com.example.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.server.VaadinSession;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository){
	this.userRepository = userRepository;
    }

    @Transactional
    public List<History> getUserHistories(Long userId) {
    	AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    	// Access histories while within the transaction
    	List<History> histories = user.getHistories();
    	histories.size(); // Forces initialization
    	return histories;
    }

    @Transactional
    public void removeUserHistory(History history) {
    	// Fetch the current user
    	AppUser user = findCurrentUser();

 	// Remove the History to the user's collection
    	user.getHistories().removeIf(h -> h.getId().equals(history.getId()));

    	// Save the user; CascadeType.ALL will save the history automatically
        userRepository.save(user);
    }

    @Transactional
    public void addHistoryToUser(String title, String dateTime) {
        // Fetch the current user
        AppUser user = findCurrentUser();

        // Create a new History instance
        History history = new History();
        history.setUser(user); // Associate the user with the history
        history.setTitle(title);
        history.setDateTime(dateTime);

        // Add the History to the user's collection
        user.getHistories().add(history);

        // Save the user; CascadeType.ALL will save the history automatically
        userRepository.save(user);
    }

    public void addGoogleUser(String firstName, String lastName, String email, String profileImage){
    	AppUser user = new AppUser();
	user.setFirstName(firstName);
	user.setLastName(lastName);
	user.setEmail(email);
	user.setPassword("NO PASSWORD");
        user.setProfileImage(profileImage);
        user.setRole("ROLE_USER");
        user.setVerification("");
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void updateUser(AppUser user){
         userRepository.save(user);
    }

    public void registerUser(String firstName, String lastName, String email, String password, String profileImage){
    	String encryptedPassword = passwordEncoder.encode(password);

    	AppUser user = new AppUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(encryptedPassword); // Store the encrypted password
        user.setProfileImage(profileImage);
        user.setRole("ROLE_USER");
        user.setVerification("");
        user.setEnabled(true);
        // Save the user entity in the database
        userRepository.save(user);
    }

    public boolean authenticate(String email, String password) {
    	// Retrieve the user from the database
    	AppUser user = userRepository.findByEmail(email);

    	if (user == null) {
            System.out.println("User not found: " + email);
            return false;
        }

    	// Check if the password matches
    	boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
    	if (!passwordMatch) {
            System.out.println("Password does not match for user: " + email);
    	}

    	return passwordMatch;
    }

    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public AppUser findCurrentUser() {
        // Retrieve the email of the current user from the session
        String userEmail = (String) VaadinSession.getCurrent().getAttribute("user");

        // Use the email to fetch the user from the database
        if (userEmail != null) {
            return userRepository.findByEmail(userEmail);
        }
        return null;
    }

    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    public AppUser getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public AppUser findUserByEmail(String userEmail){
    	return userRepository.findByEmail(userEmail);
    }

    public AppUser findUserByPassword(String userPassword){
    	AppUser user = findCurrentUser();

    	if(passwordEncoder.matches(userPassword, user.getPassword())){
    	   return userRepository.findByEmail(user.getEmail());
    	}
    	return null;
    }

    public List<AppUser> findAllUsers(String searchTerm) {
	return userRepository.search(searchTerm);
    }

    public AppUser findByFullName(String fullName){
    	return userRepository.findByFullName(fullName);
    }
}
