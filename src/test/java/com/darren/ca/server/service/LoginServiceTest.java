package com.darren.ca.server.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginServiceTest {

    private LoginService loginService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void extractUsernameTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method extractUsername
                = LoginService.class.getDeclaredMethod("extractUsername", String.class);
        extractUsername.setAccessible(true);

        loginService = new LoginService();
        String result = (String) extractUsername.invoke(loginService, "<darren><12345>");

        assertEquals("darren", result);
    }

    @Test
    public void extractPasswordTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method extractPassword
                = LoginService.class.getDeclaredMethod("extractPassword", String.class);
        extractPassword.setAccessible(true);

        loginService = new LoginService();
        String result = (String) extractPassword.invoke(loginService, "<darren><12345>");

        assertEquals("12345", result);
    }
}