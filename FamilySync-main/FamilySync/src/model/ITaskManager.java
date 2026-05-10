package model;

import java.util.List;

/**
 * Görev yönetimi için temel interface.
 * Tüm kullanıcı türleri bu interface'i implemente eder.
 * 
 * Interface kullanımı OOP gereksinimi olarak zorunludur.
 */
public interface ITaskManager {

    /** Yeni görev ekle */
    void addTask(Task task);

    /** ID'ye göre görev sil */
    void removeTask(String taskId);

    /** Tüm görevleri döndür */
    List<Task> getTasks();

    /** Tamamlanmamış görev sayısını döndür */
    default int getPendingTaskCount() {
        return (int) getTasks().stream().filter(t -> !t.isCompleted()).count();
    }

    /** Tüm görevleri tamamlandı olarak işaretle */
    default void completeAllTasks() {
        getTasks().forEach(t -> t.setCompleted(true));
    }
}
