package com.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void testSaluerNomNormal() {
        assertEquals("Bonjour, Alice !", App.saluer("Alice"));
    }

    @Test
    void testSaluerNomVide() {
        assertThrows(IllegalArgumentException.class,
            () -> App.saluer(""));
    }

    @Test
    void testSaluerNull() {
        assertThrows(IllegalArgumentException.class,
            () -> App.saluer(null));
    }
}