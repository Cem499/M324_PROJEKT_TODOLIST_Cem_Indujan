package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true, "alles gut");
    }

    @Test
    void taskIstValidWennBeschreibungGesetzt() {
        Task task = new Task();
        task.setTaskdescription("Einkaufen");
        assertTrue(task.isValid());
    }

    @Test
    void taskIstNichtValidWennBeschreibungNull() {
        Task task = new Task();
        assertFalse(task.isValid());
    }

    @Test
    void taskHatKorrekteAnzahlZeichen() {
        Task task = new Task();
        task.setTaskdescription("Hallo");
        assertEquals(5, task.getLength());
    }

    @Test
    void taskBeschreibungWirdGrossgeschrieben() {
        Task task = new Task();
        task.setTaskdescription("hausaufgaben");
        assertEquals("HAUSAUFGABEN", task.toUpperCase());
    }
}