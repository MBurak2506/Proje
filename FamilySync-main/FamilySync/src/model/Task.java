package model;

import java.time.LocalDate;

/**
 * Görev modelini temsil eden sınıf.
 * Hem ebeveyn hem de çocuk görevleri bu sınıfla temsil edilir.
 */
public class Task {

    // Öncelik seviyesi için enum
    public enum Priority {
        LOW("Düşük"),
        MEDIUM("Orta"),
        HIGH("Yüksek");

        private final String label;
        Priority(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private String id;
    private String title;
    private String description;
    private boolean completed;
    private Priority priority;
    private LocalDate dueDate;

    // Görev bir çocuğa atandıysa dolu olur
    private String assignedToChildId;
    private String assignedByParentId;

    // Google Takvim ile senkronize edilmişse etkinlik ID'si
    private String googleEventId;

    public Task(String id, String title, String description, Priority priority, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = false;
        this.assignedToChildId = null;
        this.assignedByParentId = null;
    }

    /**
     * Görevin ebeveyn tarafından atanıp atanmadığını kontrol eder
     */
    public boolean isAssignedByParent() {
        return assignedByParentId != null && !assignedByParentId.isEmpty();
    }

    /**
     * Görev durumunu metin olarak döndürür
     */
    public String getStatusText() {
        return completed ? "✅ Tamamlandı" : "⏳ Bekliyor";
    }

    // --- Getter / Setter ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getAssignedToChildId() { return assignedToChildId; }
    public void setAssignedToChildId(String assignedToChildId) { this.assignedToChildId = assignedToChildId; }

    public String getAssignedByParentId() { return assignedByParentId; }
    public void setAssignedByParentId(String assignedByParentId) { this.assignedByParentId = assignedByParentId; }

    public String getGoogleEventId() { return googleEventId; }
    public void setGoogleEventId(String googleEventId) { this.googleEventId = googleEventId; }

    @Override
    public String toString() {
        return "[" + priority.getLabel() + "] " + title + " - " + getStatusText();
    }
}
