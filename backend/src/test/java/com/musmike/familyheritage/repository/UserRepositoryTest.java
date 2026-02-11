package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.AbstractIntegrationTest; // Import klasy bazowej
import com.musmike.familyheritage.model.Role;
import com.musmike.familyheritage.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByUsernameWhenUserExists() {
        // GIVEN
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.GUEST);
        userRepository.save(user);

        // WHEN
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // THEN
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        // WHEN
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // THEN
        assertFalse(foundUser.isPresent());
    }
}