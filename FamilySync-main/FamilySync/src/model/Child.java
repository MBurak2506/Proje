package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Çocuk kullanıcı sınıfı.
 * Ebeveyn tarafından atanan görevleri görüntüler, tamamlar ve puan/rozet kazanır.
 */
public class Child extends User implements IGamificationManager {

    private int age;
    private String parentId;
    private List<Task> assignedTasks;

    // Oyunlaştırma
    private int points;
    private int completedTaskCount;
    private List<String> earnedBadgeNames;

    public Child(String id, String username, String password, String fullName, int age, String parentId) {
        super(id, username, password, fullName);
        this.age = age;
        this.parentId = parentId;
        this.assignedTasks   = new ArrayList<>();
        this.points          = 0;
        this.completedTaskCount = 0;
        this.earnedBadgeNames = new ArrayList<>();
    }

    @Override
    public String getDashboardTitle() {
        return "Merhaba, " + getFullName() + "! 👋";
    }

    public void receiveAssignedTask(Task task) {
        assignedTasks.add(task);
    }

    /**
     * Atanan görevi tamamlar; puan ve rozet kontrolü yapar.
     * @return kazanılan puan (görev bulunamazsa veya zaten tamamlandıysa -1)
     */
    public int completeAssignedTask(String taskId) {
        for (Task task : assignedTasks) {
            if (task.getId().equals(taskId) && !task.isCompleted()) {
                task.setCompleted(true);
                int earned = pointsForPriority(task.getPriority());
                points += earned;
                completedTaskCount++;
                checkAndAwardBadges();
                return earned;
            }
        }
        return -1;
    }

    /**
     * Kendi görevini tamamlar; puan ve rozet kontrolü yapar.
     * @return kazanılan puan (görev bulunamazsa veya zaten tamamlandıysa -1)
     */
    public int completeOwnTask(String taskId) {
        for (Task task : getTasks()) {
            if (task.getId().equals(taskId) && !task.isCompleted()) {
                task.setCompleted(true);
                int earned = pointsForPriority(task.getPriority());
                points += earned;
                completedTaskCount++;
                checkAndAwardBadges();
                return earned;
            }
        }
        return -1;
    }

    private int pointsForPriority(Task.Priority priority) {
        return switch (priority) {
            case HIGH   -> 20;
            case MEDIUM -> 10;
            case LOW    -> 5;
        };
    }

    public int getPendingAssignedTaskCount() {
        return (int) assignedTasks.stream().filter(t -> !t.isCompleted()).count();
    }

    // --- IGamificationManager ---

    @Override
    public int getPoints() { return points; }

    @Override
    public void addPoints(int amount) {
        points += amount;
        checkAndAwardBadges();
    }

    @Override
    public int getCompletedTaskCount() { return completedTaskCount; }

    @Override
    public List<String> getEarnedBadgeNames() { return earnedBadgeNames; }

    @Override
    public void checkAndAwardBadges() {
        for (Badge badge : Badge.values()) {
            if (!earnedBadgeNames.contains(badge.name()) && badge.isEarned(completedTaskCount, points)) {
                earnedBadgeNames.add(badge.name());
            }
        }
    }

    /** Badge enum listesi olarak döndürür. */
    public List<Badge> getEarnedBadges() {
        List<Badge> badges = new ArrayList<>();
        for (String name : earnedBadgeNames) {
            try { badges.add(Badge.valueOf(name)); } catch (Exception ignored) {}
        }
        return badges;
    }

    // --- Getter / Setter ---

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public List<Task> getAssignedTasks() { return assignedTasks; }
    public void setAssignedTasks(List<Task> assignedTasks) { this.assignedTasks = assignedTasks; }

    public void setPoints(int points) { this.points = points; }
    public void setCompletedTaskCount(int count) { this.completedTaskCount = count; }
    public void setEarnedBadgeNames(List<String> names) { this.earnedBadgeNames = names; }
}
