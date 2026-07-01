# Testbericht – ToDo-Liste (M324)

**Projekt:** M324_PROJEKT_TODOLIST_Cem_Indujan
**Datum:** 01.07.2026
**Getestete Komponente:** Backend (Spring Boot, `DemoApplication` / `Task`), ergänzend Frontend (React `App.jsx`)
**Testwerkzeug:** JUnit 5 + Spring `MockMvc` (`spring-boot-starter-test`, bereits in `pom.xml` vorhanden)
**Neue Datei:** `backend/src/test/java/com/example/demo/TaskApiIntegrationTests.java`

---

## 1. Wichtiger Hinweis zur Ausführung

Die neuen Tests wurden **nach bestem Wissen anhand des vorhandenen Source-Codes geschrieben und folgen exakt dem Stil der bestehenden `DemoApplicationTests.java`** (gleiche Annotationen, gleiche Spring-Boot-Version, keine neuen Abhängigkeiten). In der Umgebung, in der dieser Bericht erstellt wurde, bestand jedoch **kein Zugriff auf Maven Central**, weshalb die Tests hier **nicht kompiliert/ausgeführt** werden konnten. Bitte vor dem Push lokal verifizieren:

```bash
cd backend
./mvnw test
# oder, falls mvn global installiert ist:
mvn test
```

Sollte etwas nicht kompilieren (z. B. leicht abweichende Spring-Boot-Version), sind die wahrscheinlichsten Stellen die `jsonPath`-Matcher-Syntax oder die `Map.of(...)`-Aufrufe – beides Standard-APIs, die mit den in `pom.xml` vorhandenen Versionen (Spring Boot 3.4.5, Java 17) kompatibel sein sollten.

---

## 2. Testübersicht (Vorgabe-Tests 1–8)

| # | Test | Ziel | Status | Testmethode |
|---|------|------|--------|-------------|
| 1 | Neues Element wird hinzugefügt | POST `/tasks` mit neuem Eintrag → erscheint in GET `/tasks` | ✅ automatisiert | `addTask_addsNewElementToList` |
| 2 | Korrekte Anzahl Elemente | Anzahl Elemente steigt nach Hinzufügen um genau die erwartete Zahl | ✅ automatisiert | `getTasks_returnsCorrectCountAfterAdding` |
| 3 | "Erledigt"-Button funktioniert | Klick auf "Erledigt" markiert Task als erledigt | ⛔ **nicht automatisierbar** | `completeButton_marksTaskAsDone` (`@Disabled`) |
| 4 | Entfernen eines Elements | POST `/tasks/delete` entfernt Element korrekt aus der Liste | ✅ automatisiert | `deleteTask_removesElementFromList` |
| 5 | Fehlermeldung bei leerem Element | Leere Eingabe wird abgelehnt, Fehlermeldung erscheint | ⛔ **nicht automatisierbar** | `addEmptyTask_isRejectedWithErrorMessage` (`@Disabled`) |
| 6 | Fehlermeldung bei Ladefehler | Fehlschlag beim Laden zeigt Fehlermeldung | ⛔ **nicht automatisierbar** | `loadTasks_showsErrorMessageOnFailure` (`@Disabled`) |
| 7 | Liste wird nach Laden korrekt angezeigt | GET `/tasks` liefert alle Felder korrekt (Priorität, Fälligkeitsdatum, Erstellungszeit) | ✅ automatisiert | `getTasks_afterLoad_returnsAllFieldsCorrectly` |
| 8 | Doppelter Eintrag wird abgelehnt | Zweites Senden derselben Beschreibung erzeugt keinen zweiten Eintrag | ✅ automatisiert | `addDuplicateTask_isNotAddedTwice` |

**Ergebnis: 5 von 8 Vorgabe-Tests sind automatisiert, 3 sind aktuell nicht automatisierbar**, weil die dafür nötige Funktionalität im Source (noch) fehlt (siehe Abschnitt 4). Statt sie wegzulassen, wurden sie als `@Disabled`-Testmethoden mit präziser Begründung im Code belassen – so bleiben sie als offene Punkte sichtbar und können nach einer Fehlerbehebung direkt reaktiviert werden.

---

## 3. Zusätzliche Tests für die umgesetzten User-Stories (Punkt 9)

Neben der Kernfunktionalität (Task-CRUD) wurden von Cem/Indujan drei zusätzliche User-Stories umgesetzt: **Priorität**, **Fälligkeitsdatum** und **serverseitige Suche**. Dafür wurden folgende Tests ergänzt:

| Test | Ziel | Status |
|------|------|--------|
| `addTask_withPriority_isStoredCorrectly` | Beim Anlegen gesetzte Priorität (`HOCH`/`MITTEL`/`TIEF`) wird korrekt gespeichert und ausgegeben | ✅ automatisiert |
| `addTask_withDueDate_isStoredCorrectly` | Fälligkeitsdatum wird korrekt gespeichert und ausgegeben | ✅ automatisiert |
| `searchTasks_returnsOnlyMatchingResults` | `/tasks/search?q=...` liefert nur passende Treffer, keine falschen Positiven | ✅ automatisiert |
| `searchTasks_isCaseInsensitive` | Suche funktioniert unabhängig von Gross-/Kleinschreibung | ✅ automatisiert |

Nicht automatisiert (weil nicht im Code vorhanden bzw. nicht sinnvoll testbar über die API):
- Client-seitige Priorität-Farblabels (`priority-label priority-hoch` etc.) – reines CSS/Rendering, würde einen Frontend-Test (z. B. mit Vitest + React Testing Library) benötigen, was im Frontend-Projekt aktuell nicht eingerichtet ist.
- Verhalten bei leerem Suchbegriff (`q=""`) – aktuell nicht spezifiziert, sollte mit dem Auftraggeber/Lehrperson geklärt werden, bevor ein Test dafür geschrieben wird.

---

## 4. Ausbesserungspunkte im Source (Haupt-Ziel dieser Analyse)

### Backend (`DemoApplication.java`, `Task.java`)

1. **Keine Validierung leerer Aufgaben (→ Test 5):** `Task.isValid()` ist implementiert, wird aber in `addTask()` nie aufgerufen. Eine leere `taskdescription` wird kommentarlos gespeichert.
2. **Kein "Erledigt"-Mechanismus (→ Test 3):** Das Feld `Task.completed` existiert, es gibt aber weder einen Endpunkt (`PUT/POST /tasks/complete`) noch eine Frontend-Bedienung dafür.
3. **Irreführende Rückgabewerte:** `addTask()` und `delTask()` geben immer den literalen Text `"redirect:/api/v1/tasks"` mit HTTP-Status 200 zurück – auch bei Duplikaten oder ungültigem JSON. Da die Klasse `@RestController` ist, wird dieser String nicht als echter Redirect interpretiert, sondern 1:1 als Body gesendet. Der Client kann Erfolg und Fehler nicht unterscheiden.
4. **Stille Fehlerbehandlung:** `JsonProcessingException` wird nur mit `e.printStackTrace()` geloggt; die Methode antwortet trotzdem mit vermeintlichem Erfolg.
5. **Keine eindeutige ID:** Tasks werden ausschliesslich über `taskdescription` verglichen/identifiziert (Duplikatprüfung, Löschen). Das verhindert inhaltlich gleiche, aber eigentlich unterschiedliche Einträge und macht Umbenennungen unmöglich, ohne die "Identität" der Aufgabe zu verlieren.
6. **Tote Abhängigkeit / fehlende Persistenz:** `mysql-connector-j` ist in `pom.xml` eingebunden, wird aber nirgends verwendet – Tasks leben nur in einer In-Memory-`ArrayList` und gehen bei jedem Neustart verloren.
7. **`@CrossOrigin` redundant pro Methode** statt einmal auf Klassenebene gesetzt – funktional kein Bug, aber unnötige Wiederholung.
8. **`/tasks/search` ohne Fehlerbehandlung:** Fehlt der Query-Parameter `q`, wirft Spring eine `MissingServletRequestParameterException` ohne definierte, saubere Fehlerantwort.

### Frontend (`App.jsx`)

9. **Keine Fehlerbehandlung beim Laden (→ Test 6):** `fetchTasks()` hat kein `.catch()` bei der GET-Anfrage; bei Serverausfall bleibt die Liste stumm leer, ohne jede Rückmeldung an die Nutzer:innen.
10. **Keine clientseitige Leer-Prüfung (→ Test 5):** `handleSubmit()` prüft `taskdescription` nicht auf einen leeren/whitespace-only String, bevor der Request abgesendet wird.
11. **Serverantwort wird nicht ausgewertet:** Weder bei `handleSubmit` noch bei `handleDelete` wird `response.ok` geprüft – Duplikate, Validierungsfehler oder Serverfehler bleiben für Nutzer:innen unsichtbar (→ Test 8: kein visuelles Feedback bei Duplikat, obwohl das Backend sie korrekt verwirft).
12. **Verwirrende Button-Semantik:** Der einzige Button pro Aufgabe zeigt ein Häkchen (✔), führt aber `handleDelete` aus statt eine "Erledigt"-Funktion – das Symbol suggeriert etwas anderes als die tatsächliche Aktion.
13. **React-`key` basiert auf `taskdescription`** statt auf einer stabilen ID, was bei zukünftigen Duplikaten/Umbenennungen zu Rendering-Problemen führen kann.
14. **Hart codierte `API_BASE`** (`http://localhost:8080`) statt Umgebungsvariable – funktioniert nicht automatisch in Docker-Compose- oder Produktionsumgebungen ohne Build-Anpassung.

---

## 5. Empfehlung für die Priorisierung (falls Zeit bleibt)

Gemäss Aufgabenstellung ist das Festhalten der Punkte das primäre Ziel; sollte dennoch Zeit für Korrekturen bleiben, empfiehlt sich folgende Reihenfolge (Aufwand/Nutzen):

1. Punkt 1/10 (leere Aufgaben validieren) – kleiner Aufwand, behebt Test 5 direkt.
2. Punkt 11 (Serverantwort auswerten + Fehleranzeige im Frontend) – behebt Test 6 und macht Duplikat-Feedback (Test 8) sichtbar.
3. Punkt 2 ("Erledigt"-Funktion) – grösserer Aufwand (neuer Endpoint + UI), behebt Test 3.

Alle drei Änderungen sind additiv und würden bestehende, funktionierende Abläufe nicht brechen.

---

## 6. Git

Die neuen Tests wurden lokal committet:

```
ab7cea4 test: MockMvc-Integrationstests für ToDo-REST-API
10e7746 chore: baseline import (Ausgangsstand vor Testerweiterung)
```

**Zum Push auf euer eigentliches Repository** (GitHub/GitLab) fehlen mir in dieser Umgebung Zugriff/Credentials auf euer Remote. Bitte lokal:

```bash
git remote add origin <eure-repo-url>
git push -u origin <branch-name>
```

oder die Datei `TaskApiIntegrationTests.java` direkt in euer bestehendes lokales Repo kopieren, `mvn test` laufen lassen und wie gewohnt committen/pushen.
