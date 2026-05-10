package model;

import java.time.LocalDateTime;

/**
 * Takvim etkinliğini temsil eden sınıf.
 * Toplantı, randevu, sınav gibi etkinlikler bu sınıfla temsil edilir.
 */
public class CalendarEvent {

    // Etkinlik türü için enum
    public enum EventType {
        MEETING("📅 Toplantı"),
        APPOINTMENT("🏥 Randevu"),
        EXAM("📝 Sınav"),
        REMINDER("🔔 Hatırlatıcı"),
        OTHER("📌 Diğer");

        private final String label;
        EventType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EventType eventType;
    private boolean isSharedWithFamily; // Aile genelinde görünür mü?
    private String googleEventId;

    public CalendarEvent(String id, String title, String description,
                         LocalDateTime startTime, LocalDateTime endTime,
                         EventType eventType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventType = eventType;
        this.isSharedWithFamily = false;
    }

    /**
     * Etkinliğin bugün olup olmadığını kontrol eder
     */
    public boolean isToday() {
        return startTime.toLocalDate().equals(java.time.LocalDate.now());
    }

    /**
     * Etkinlik süresini dakika cinsinden döndürür
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    // --- Getter / Setter ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public boolean isSharedWithFamily() { return isSharedWithFamily; }
    public void setSharedWithFamily(boolean sharedWithFamily) { isSharedWithFamily = sharedWithFamily; }

    public String getGoogleEventId() { return googleEventId; }
    public void setGoogleEventId(String googleEventId) { this.googleEventId = googleEventId; }

    @Override
    public String toString() {
        return eventType.getLabel() + " | " + title + " | " + startTime.toString();
    }
}
