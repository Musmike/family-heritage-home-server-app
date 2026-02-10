package com.musmike.familyheritage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musmike.familyheritage.dto.LoginRequest;
import com.musmike.familyheritage.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationManager authenticationManager() {
            return Mockito.mock(AuthenticationManager.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }
    }

    @Test
    void shouldSetCookiesOnSuccessfulLogin() throws Exception {
        // GIVEN
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        UserDetails userDetails = User
                .withUsername("user")
                .password("pass")
                .authorities("ADMIN").build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,
                null, userDetails.getAuthorities());

        Mockito.when(authenticationManager.authenticate(any())).thenReturn(auth);
        Mockito.when(jwtService.generateAccessToken(any())).thenReturn("mocked.access.token");
        Mockito.when(jwtService.generateRefreshToken(any())).thenReturn("mocked.refresh.token");

        // WHEN & THEN
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFailsWithBadCredentials() throws Exception {
        // GIVEN
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("wrongpassword");

        Mockito.when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // WHEN & THEN
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("BAD_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void shouldLogoutAndClearCookies() throws Exception {
        // WHEN & THEN
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0))
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
    }


}