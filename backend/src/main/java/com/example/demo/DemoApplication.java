package com.example.demo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@SpringBootApplication
@RequestMapping("/api/v1")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private List<Task> tasks = new ArrayList<>();

    @CrossOrigin
    @GetMapping("/tasks")
    public List<Task> getTasks() {
        System.out.println("API v1 - GET /api/v1/tasks – Aufgabenliste hat " + tasks.size() + " Einträge.");
        return tasks;
    }

    @CrossOrigin
    @PostMapping("/tasks")
    public String addTask(@RequestBody String taskdescription) {
        System.out.println("API v1 - POST /api/v1/tasks – Empfangen: '" + taskdescription + "'");
        ObjectMapper mapper = new ObjectMapper();
        try {
            Task task = mapper.readValue(taskdescription, Task.class);
            for (Task t : tasks) {
                if (t.getTaskdescription().equals(task.getTaskdescription())) {
                    System.out.println(">>> Aufgabe '" + task.getTaskdescription() + "' existiert bereits!");
                    return "redirect:/api/v1/tasks";
                }
            }
            System.out.println("... Aufgabe wird hinzugefügt: '" + task.getTaskdescription() + "'");
            tasks.add(task);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "redirect:/api/v1/tasks";
    }

    @CrossOrigin
    @GetMapping("/tasks/search")
    public List<Task> searchTasks(@RequestParam String q) {
        String query = q.toLowerCase();
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTaskdescription().toLowerCase().contains(query)) {
                result.add(task);
            }
        }
        System.out.println("API v1 - GET /api/v1/tasks/search?q=" + q + " – " + result.size() + " Treffer gefunden.");
        return result;
    }

    @CrossOrigin
    @PostMapping("/tasks/delete")
    public String delTask(@RequestBody String taskdescription) {
        System.out.println("API v1 - POST /api/v1/tasks/delete – Empfangen: '" + taskdescription + "'");
        ObjectMapper mapper = new ObjectMapper();
        try {
            Task task = mapper.readValue(taskdescription, Task.class);
            Iterator<Task> it = tasks.iterator();
            while (it.hasNext()) {
                Task t = it.next();
                if (t.getTaskdescription().equals(task.getTaskdescription())) {
                    System.out.println("... Aufgabe wird gelöscht: '" + task.getTaskdescription() + "'");
                    it.remove();
                    return "redirect:/api/v1/tasks";
                }
            }
            System.out.println(">>> Aufgabe '" + task.getTaskdescription() + "' nicht gefunden!");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "redirect:/api/v1/tasks";
    }
}