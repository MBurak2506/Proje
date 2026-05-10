package model;

import java.util.List;

public interface IGamificationManager {
    int getPoints();
    void addPoints(int amount);
    int getCompletedTaskCount();
    List<String> getEarnedBadgeNames();
    void checkAndAwardBadges();
}
