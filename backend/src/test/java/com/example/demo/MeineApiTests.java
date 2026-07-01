package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MeineApiTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    // -------------------------
    // Hilfsmethoden
    // -------------------------

    private int getAnzahlAufgaben() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> tasks = mapper.readValue(json, List.class);
        return tasks.size();
    }

    private void addAufgabe(String name, String priority, String duedate) throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                        + "\"taskdescription\":\"" + name + "\","
                        + "\"priority\":\"" + priority + "\","
                        + "\"duedate\":\"" + duedate + "\""
                        + "}"))
                .andExpect(status().isOk());
    }

    private void loescheAufgabe(String name) throws Exception {
        mockMvc.perform(post("/api/v1/tasks/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskdescription\":\"" + name + "\"}"))
                .andExpect(status().isOk());
    }

    private boolean existiertAufgabe(String name) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> tasks = mapper.readValue(json, List.class);

        for (Map<String, Object> task : tasks) {
            if (name.equals(task.get("taskdescription"))) {
                return true;
            }
        }
        return false;
    }

    private int zaehleVorkommen(String name) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> tasks = mapper.readValue(json, List.class);

        int anzahl = 0;
        for (Map<String, Object> task : tasks) {
            if (name.equals(task.get("taskdescription"))) {
                anzahl++;
            }
        }
        return anzahl;
    }

        // -------------------------
    // Test 1: Hinzufügen
    // -------------------------
    @Test
    void testAufgabeHinzufuegen() throws Exception {
        String name = "Testaufgabe-" + System.nanoTime();
        addAufgabe(name, "Mittel", "2026-12-31");
        assertTrue(existiertAufgabe(name));
        loescheAufgabe(name);
    }

    // -------------------------
    // Test 2: Anzahl Elemente
    // -------------------------
    @Test
    void testAnzahlElemente() throws Exception {
        int vorher = getAnzahlAufgaben();

        String name1 = "Testaufgabe1-" + System.nanoTime();
        String name2 = "Testaufgabe2-" + System.nanoTime();

        addAufgabe(name1, "Mittel", "2026-12-31");
        addAufgabe(name2, "Hoch", "2026-12-31");

        int nachher = getAnzahlAufgaben();

        assertEquals(vorher + 2, nachher);

        loescheAufgabe(name1);
        loescheAufgabe(name2);
    }

    // -------------------------
    // Test 4: Löschen
    // -------------------------
    @Test
    void testAufgabeLoeschen() throws Exception {
        String name = "Testaufgabe-" + System.nanoTime();

        addAufgabe(name, "Mittel", "2026-12-31");
        assertTrue(existiertAufgabe(name));

        loescheAufgabe(name);
        assertFalse(existiertAufgabe(name));
    }

    // -------------------------
    // Test 8: Duplikat
    // -------------------------
    @Test
    void testDuplikatWirdNichtHinzugefuegt() throws Exception {
        String name = "DuplikatTest-" + System.nanoTime();

        addAufgabe(name, "Mittel", "2026-12-31");
        assertEquals(1, zaehleVorkommen(name));

        addAufgabe(name, "Mittel", "2026-12-31");
        assertEquals(1, zaehleVorkommen(name));

        loescheAufgabe(name);
    }

    // -------------------------
    // Test: Suche
    // -------------------------
    @Test
    void testSucheFindetNurPassendeAufgabe() throws Exception {

        String zebra = "Zebra-" + System.nanoTime();
        String giraffe = "Giraffe-" + System.nanoTime();

        addAufgabe(zebra, "Mittel", "2026-12-31");
        addAufgabe(giraffe, "Hoch", "2026-12-31");

        MvcResult result = mockMvc.perform(get("/api/v1/tasks/search")
                        .param("q", "Zebra"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> tasks = mapper.readValue(json, List.class);

        boolean zebraGefunden = false;
        boolean giraffeGefunden = false;

        for (Map<String, Object> task : tasks) {
            String beschreibung = (String) task.get("taskdescription");

            if (zebra.equals(beschreibung)) {
                zebraGefunden = true;
            }

            if (giraffe.equals(beschreibung)) {
                giraffeGefunden = true;
            }
        }

        assertTrue(zebraGefunden);
        assertFalse(giraffeGefunden);

        loescheAufgabe(zebra);
        loescheAufgabe(giraffe);
    }

        // -------------------------
    // Test: Priorität
    // -------------------------
    @Test
    void testPrioritaetWirdGespeichert() throws Exception {

        String name = "PrioritaetTest-" + System.nanoTime();

        addAufgabe(name, "Hoch", "2026-12-31");

        MvcResult result = mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> tasks = mapper.readValue(json, List.class);

        Map<String, Object> gefundeneAufgabe = null;

        for (Map<String, Object> task : tasks) {
            if (name.equals(task.get("taskdescription"))) {
                gefundeneAufgabe = task;
                break;
            }
        }

        assertNotNull(gefundeneAufgabe);
        assertEquals("Hoch", gefundeneAufgabe.get("priority"));

        loescheAufgabe(name);
    }

    // -------------------------
    // Test: Fälligkeitsdatum
    // -------------------------
    @Test
    void testFaelligkeitsdatumWirdGespeichert() throws Exception {

        String name = "DatumTest-" + System.nanoTime();

        addAufgabe(name, "Hoch", "2026-12-31");

        MvcResult result = mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> tasks = mapper.readValue(json, List.class);

        Map<String, Object> gefundeneAufgabe = null;

        for (Map<String, Object> task : tasks) {
            if (name.equals(task.get("taskdescription"))) {
                gefundeneAufgabe = task;
                break;
            }
        }

        assertNotNull(gefundeneAufgabe);
        assertEquals("2026-12-31", gefundeneAufgabe.get("duedate"));

        loescheAufgabe(name);
    }

    // -------------------------
    // Disabled Tests
    // -------------------------

    @Test
    @Disabled("Kein API-Endpoint vorhanden, der completed-Status setzt oder ändert")
    void testErledigtButton() {
        // bewusst leer
    }

    @Test
    @Disabled("Keine serverseitige Validierung vorhanden (task.isValid() wird im Controller nicht aufgerufen)")
    void testLeereAufgabeWirdAbgelehnt() {
        // bewusst leer
    }

    @Test
    @Disabled("Frontend fetchTasks() in App.jsx hat kein .catch() bei Fehlern; keine UI-Fehlermeldung implementiert")
    void testFehlermeldungBeiLadefehler() {
        // bewusst leer
    }

} // Ende der Klasse

