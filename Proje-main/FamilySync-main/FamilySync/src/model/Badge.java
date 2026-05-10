package model;

public enum Badge {
    FIRST_TASK("🌟 İlk Adım", "İlk görevini tamamladın!", 1, 0),
    FIVE_TASKS("⭐ Azimli", "5 görev tamamladın!", 5, 0),
    TEN_TASKS("🏆 Süper Kahraman", "10 görev tamamladın!", 10, 0),
    TWENTY_TASKS("👑 Şampiyon", "20 görev tamamladın!", 20, 0),
    FIFTY_POINTS("💰 Puan Avcısı", "50 puan kazandın!", 0, 50),
    HUNDRED_POINTS("💎 Altın Ödül", "100 puan kazandın!", 0, 100),
    TWO_HUNDRED_POINTS("🎯 Mükemmeliyetçi", "200 puan kazandın!", 0, 200);

    private final String label;
    private final String description;
    private final int taskThreshold;
    private final int pointThreshold;

    Badge(String label, String description, int taskThreshold, int pointThreshold) {
        this.label = label;
        this.description = description;
        this.taskThreshold = taskThreshold;
        this.pointThreshold = pointThreshold;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public int getTaskThreshold() { return taskThreshold; }
    public int getPointThreshold() { return pointThreshold; }

    public boolean isEarned(int completedTaskCount, int points) {
        if (taskThreshold > 0) return completedTaskCount >= taskThreshold;
        if (pointThreshold > 0) return points >= pointThreshold;
        return false;
    }
}
