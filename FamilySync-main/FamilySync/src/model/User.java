package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tüm kullanıcılar için ortak özellikleri barındıran soyut sınıf.
 * Parent ve Child sınıfları bu sınıftan türetilir.
 */
public abstract class User implements ITaskManager, ICalendarManager {

    // Kapsülleme: private değişkenler
    private String id;
    private String username;
    private String password;
    private String fullName;
    private List<Task> tasks;
    private List<CalendarEvent> events;

    public User(String id, String username, String password, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.tasks = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    // Polimorfizm: her alt sınıf kendi dashboard başlığını döndürür
    public abstract String getDashboardTitle();

    // Polimorfizm: görev ekleme davranışı alt sınıflarda özelleştirilebilir
    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(String taskId) {
        tasks.removeIf(t -> t.getId().equals(taskId));
    }

    public void addEvent(CalendarEvent event) {
        events.add(event);
    }

    public void removeEvent(String eventId) {
        events.removeIf(e -> e.getId().equals(eventId));
    }

    // --- Getter / Setter metotları ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public List<CalendarEvent> getEvents() { return events; }
    public void setEvents(List<CalendarEvent> events) { this.events = events; }

    @Override
    public String toString() {
        return "User{id='" + id + "', username='" + username + "', fullName='" + fullName + "'}";
    }
}
