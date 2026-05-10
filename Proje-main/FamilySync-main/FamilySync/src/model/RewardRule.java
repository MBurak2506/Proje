package model;

public class RewardRule {
    private String id;
    private String description;
    private int pointThreshold;

    public RewardRule(String id, String description, int pointThreshold) {
        this.id = id;
        this.description = description;
        this.pointThreshold = pointThreshold;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPointThreshold() { return pointThreshold; }
    public void setPointThreshold(int pointThreshold) { this.pointThreshold = pointThreshold; }

    @Override
    public String toString() {
        return description + " (" + pointThreshold + " puan)";
    }
}
