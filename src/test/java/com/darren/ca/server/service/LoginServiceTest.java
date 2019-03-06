package com.darren.ca.server.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void extractUsernameTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method extractUsername
                = AuthService.class.getDeclaredMethod("extractUsername", String.class);
        extractUsername.setAccessible(true);

        authService = new LoginService();
        String result = (String) extractUsername.invoke(authService, "<darren><12345>");

        assertEquals("darren", result);
    }

    @Test
    void extractPasswordTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method extractPassword
                = AuthService.class.getDeclaredMethod("extractPassword", String.class);
        extractPassword.setAccessible(true);

        authService = new LoginService();
        String result = (String) extractPassword.invoke(authService, "<darren><12345>");

        assertEquals("12345", result);
    }
}