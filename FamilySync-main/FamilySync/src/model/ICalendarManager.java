package model;

import java.util.List;

/**
 * Takvim etkinliği yönetimi için interface.
 */
public interface ICalendarManager {

    /** Yeni etkinlik ekle */
    void addEvent(CalendarEvent event);

    /** ID'ye göre etkinlik sil */
    void removeEvent(String eventId);

    /** Tüm etkinlikleri döndür */
    List<CalendarEvent> getEvents();

    /** Bugünkü etkinlikleri döndür */
    default List<CalendarEvent> getTodayEvents() {
        return getEvents().stream()
            .filter(CalendarEvent::isToday)
            .toList();
    }
}
