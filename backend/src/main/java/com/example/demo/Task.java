package com.example.demo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	private String taskdescription;
	private String priority;
	private String duedate;
	private String createdAt;
	private boolean completed = false;

	public Task() {
		this.createdAt = LocalDateTime.now().format(FORMATTER);
	}

	public String getTaskdescription() {
		return taskdescription;
	}

	public void setTaskdescription(String taskdescription) {
		this.taskdescription = taskdescription;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getDuedate() {
		return duedate;
	}

	public void setDuedate(String duedate) {
		this.duedate = duedate;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	// neuen Methoden
	public boolean isValid() {
		return taskdescription != null && !taskdescription.trim().isEmpty();
	}

	public int getLength() {
		return taskdescription != null ? taskdescription.length() : 0;
	}

	public String toUpperCase() {
		return taskdescription != null ? taskdescription.toUpperCase() : null;
	}
}
