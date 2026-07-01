package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integrationstests für die REST-Endpunkte von {@link DemoApplication}.
 *
 * Hinweis: Die Task-Liste im Backend ist ein einfacher, nicht persistenter
 * In-Memory-Speicher (siehe DemoApplication#tasks) und wird als Spring-Bean
 * über den gesamten Testkontext hinweg wiederverwendet. Deshalb arbeiten alle
 * Tests mit eindeutigen, zufälligen Task-Beschreibungen und räumen nach sich
 * selbst auf, statt sich auf eine leere Ausgangsliste zu verlassen.
 *
 * Getestet wird über MockMvc (kein echter Server nötig), sodass die Tests
 * ohne laufenden Backend-Prozess ausgeführt werden können (mvn test).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private String unique(String prefix) {
        return prefix + "-" + System.nanoTime();
    }

    private void addTask(String description, String priority, String duedate) throws Exception {
        Map<String, String> body = Map.of(
                "taskdescription", description,
                "priority", priority == null ? "" : priority,
                "duedate", duedate == null ? "" : duedate
        );
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    private void deleteTask(String description) throws Exception {
        mockMvc.perform(post("/api/v1/tasks/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("taskdescription", description))))
                .andExpect(status().isOk());
    }

    private List<Map<String, Object>> getAllTasks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readValue(result.getResponse().getContentAsString(), List.class);
    }

    // ---------------------------------------------------------------
    // Test 1: Neues Element wird beim Speichern zur Liste hinzugefügt
    // ---------------------------------------------------------------
    @Test
    void addTask_addsNewElementToList() throws Exception {
        String description = unique("Testaufgabe-Add");

        addTask(description, "MITTEL", "");

        List<Map<String, Object>> tasks = getAllTasks();
        assertTrue(
                tasks.stream().anyMatch(t -> description.equals(t.get("taskdescription"))),
                "Die neu hinzugefügte Aufgabe muss in der Liste erscheinen."
        );

        deleteTask(description); // aufräumen
    }

    // ---------------------------------------------------------------
    // Test 2: Korrekte Anzahl Elemente wird angezeigt
    // ---------------------------------------------------------------
    @Test
    void getTasks_returnsCorrectCountAfterAdding() throws Exception {
        int before = getAllTasks().size();

        String t1 = unique("Testaufgabe-Count1");
        String t2 = unique("Testaufgabe-Count2");
        addTask(t1, "HOCH", "");
        addTask(t2, "TIEF", "");

        int after = getAllTasks().size();
        assertEquals(before + 2, after, "Anzahl Elemente muss nach dem Hinzufügen um 2 steigen.");

        deleteTask(t1);
        deleteTask(t2);
    }

    // ---------------------------------------------------------------
    // Test 3: "Erledigt"-Button
    // ---------------------------------------------------------------
    @Test
    @Disabled("Nicht automatisierbar: Weder Frontend noch Backend besitzen aktuell einen "
            + "'Erledigt'-Button bzw. einen Endpunkt zum Umschalten von Task.completed. "
            + "Das Feld 'completed' existiert im Model, wird aber nirgends gesetzt oder "
            + "im Frontend angezeigt/bedient. Siehe Testbericht, Abschnitt 'Fehlende Funktionen'.")
    void completeButton_marksTaskAsDone() {
        // Absichtlich leer / deaktiviert – Funktion fehlt im Source.
    }

    // ---------------------------------------------------------------
    // Test 4: Entfernen eines Elements zeigt korrektes Verhalten
    // ---------------------------------------------------------------
    @Test
    void deleteTask_removesElementFromList() throws Exception {
        String description = unique("Testaufgabe-Delete");
        addTask(description, "MITTEL", "");
        assertTrue(getAllTasks().stream().anyMatch(t -> description.equals(t.get("taskdescription"))));

        deleteTask(description);

        assertFalse(
                getAllTasks().stream().anyMatch(t -> description.equals(t.get("taskdescription"))),
                "Gelöschte Aufgabe darf nicht mehr in der Liste erscheinen."
        );
    }

    // ---------------------------------------------------------------
    // Test 5: Fehlermeldung bei leerem Element
    // ---------------------------------------------------------------
    @Test
    @Disabled("Nicht automatisierbar: DemoApplication#addTask ruft Task#isValid() nie auf. "
            + "Ein leeres taskdescription-Feld wird aktuell klaglos akzeptiert und der Liste "
            + "hinzugefügt, es gibt keine serverseitige Validierung und keine Fehlermeldung "
            + "(weder Backend-Statuscode 4xx noch Frontend-Anzeige). Siehe Testbericht.")
    void addEmptyTask_isRejectedWithErrorMessage() {
        // Absichtlich leer / deaktiviert – Validierung fehlt im Source.
    }

    // ---------------------------------------------------------------
    // Test 6: Fehlermeldung, wenn Laden der Liste fehlschlägt
    // ---------------------------------------------------------------
    @Test
    @Disabled("Nicht automatisierbar: App.jsx#fetchTasks() hat keine .catch()-Behandlung für "
            + "fehlgeschlagene GET-Requests, es wird keinerlei Fehlerzustand im UI dargestellt. "
            + "Eine Simulation eines Ladefehlers hätte im aktuellen Source keine beobachtbare "
            + "Wirkung. Siehe Testbericht.")
    void loadTasks_showsErrorMessageOnFailure() {
        // Absichtlich leer / deaktiviert – Fehlerbehandlung fehlt im Frontend-Source.
    }

    // ---------------------------------------------------------------
    // Test 7: Aufgabenliste wird nach dem Laden korrekt angezeigt
    // ---------------------------------------------------------------
    @Test
    void getTasks_afterLoad_returnsAllFieldsCorrectly() throws Exception {
        String description = unique("Testaufgabe-Load");
        addTask(description, "HOCH", "2026-12-24");

        Map<String, Object> loaded = getAllTasks().stream()
                .filter(t -> description.equals(t.get("taskdescription")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aufgabe wurde nicht korrekt geladen."));

        assertEquals("HOCH", loaded.get("priority"));
        assertEquals("2026-12-24", loaded.get("duedate"));
        assertNotNull(loaded.get("createdAt"));

        deleteTask(description);
    }

    // ---------------------------------------------------------------
    // Test 8: Doppelter Eintrag wird abgelehnt
    // ---------------------------------------------------------------
    @Test
    void addDuplicateTask_isNotAddedTwice() throws Exception {
        String description = unique("Testaufgabe-Duplicate");
        addTask(description, "MITTEL", "");
        int countAfterFirst = (int) getAllTasks().stream()
                .filter(t -> description.equals(t.get("taskdescription"))).count();

        addTask(description, "MITTEL", ""); // gleiche Beschreibung nochmals senden
        int countAfterSecond = (int) getAllTasks().stream()
                .filter(t -> description.equals(t.get("taskdescription"))).count();

        assertEquals(1, countAfterFirst, "Aufgabe sollte nach erstem Hinzufügen genau 1x vorhanden sein.");
        assertEquals(countAfterFirst, countAfterSecond,
                "Ein doppelter Eintrag darf die Aufgabe nicht ein zweites Mal hinzufügen.");

        deleteTask(description);
    }

    // ---------------------------------------------------------------
    // Test 9: Zusätzliche User-Stories (Priorität, Fälligkeitsdatum, Suche)
    // ---------------------------------------------------------------

    @Test
    void addTask_withPriority_isStoredCorrectly() throws Exception {
        String description = unique("Testaufgabe-Prio");
        addTask(description, "HOCH", "");

        Map<String, Object> loaded = getAllTasks().stream()
                .filter(t -> description.equals(t.get("taskdescription")))
                .findFirst()
                .orElseThrow();

        assertEquals("HOCH", loaded.get("priority"));
        deleteTask(description);
    }

    @Test
    void addTask_withDueDate_isStoredCorrectly() throws Exception {
        String description = unique("Testaufgabe-Duedate");
        addTask(description, "MITTEL", "2026-08-15");

        Map<String, Object> loaded = getAllTasks().stream()
                .filter(t -> description.equals(t.get("taskdescription")))
                .findFirst()
                .orElseThrow();

        assertEquals("2026-08-15", loaded.get("duedate"));
        deleteTask(description);
    }

    @Test
    void searchTasks_returnsOnlyMatchingResults() throws Exception {
        String matching = unique("Zebrastreifen");
        String nonMatching = unique("Regenschirm");
        addTask(matching, "MITTEL", "");
        addTask(nonMatching, "MITTEL", "");

        MvcResult result = mockMvc.perform(get("/api/v1/tasks/search").param("q", "Zebrastreifen"))
                .andExpect(status().isOk())
                .andReturn();
        List<Map<String, Object>> results = mapper.readValue(result.getResponse().getContentAsString(), List.class);

        assertTrue(results.stream().anyMatch(t -> matching.equals(t.get("taskdescription"))));
        assertFalse(results.stream().anyMatch(t -> nonMatching.equals(t.get("taskdescription"))));

        deleteTask(matching);
        deleteTask(nonMatching);
    }

    @Test
    void searchTasks_isCaseInsensitive() throws Exception {
        String description = unique("GrossKleinSchreibungTest");
        addTask(description, "MITTEL", "");

        mockMvc.perform(get("/api/v1/tasks/search").param("q", "grosskleinschreibungtest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.taskdescription == '" + description + "')]", hasSize(1)));

        deleteTask(description);
    }
}
