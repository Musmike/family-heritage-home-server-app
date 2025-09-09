package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.model.Role;
import com.musmike.familyheritage.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
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
