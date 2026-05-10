package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Ebeveyn kullanıcı sınıfı.
 * Çocuklara görev atayabilir, aile takvimini ve ödül kurallarını yönetebilir.
 */
public class Parent extends User {

    private List<String>     childrenIds;
    private List<RewardRule> rewardRules;

    public Parent(String id, String username, String password, String fullName) {
        super(id, username, password, fullName);
        this.childrenIds = new ArrayList<>();
        this.rewardRules = new ArrayList<>();
    }

    @Override
    public String getDashboardTitle() {
        return "Ebeveyn Paneli - Hoş geldiniz, " + getFullName();
    }

    @Override
    public void addTask(Task task) {
        if (task.getPriority() == Task.Priority.HIGH) {
            getTasks().add(0, task);
        } else {
            super.addTask(task);
        }
    }

    public void assignTaskToChild(Task task, String childId) {
        task.setAssignedToChildId(childId);
        task.setAssignedByParentId(this.getId());
    }

    public void addChildId(String childId) {
        if (!childrenIds.contains(childId)) childrenIds.add(childId);
    }

    public void removeChildId(String childId) { childrenIds.remove(childId); }

    public void addRewardRule(RewardRule rule) { rewardRules.add(rule); }
    public void removeRewardRule(String ruleId) {
        rewardRules.removeIf(r -> r.getId().equals(ruleId));
    }

    // --- Getter / Setter ---

    public List<String>     getChildrenIds()  { return childrenIds; }
    public void setChildrenIds(List<String> ids) { this.childrenIds = ids; }

    public List<RewardRule> getRewardRules()  { return rewardRules; }
    public void setRewardRules(List<RewardRule> rules) { this.rewardRules = rules; }
}
