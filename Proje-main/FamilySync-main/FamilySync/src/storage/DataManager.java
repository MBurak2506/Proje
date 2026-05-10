package storage;

import model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON tabanlı veri yönetimi — saf Java ile yazılmıştır.
 * Design Pattern: Singleton
 */
public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    private static final String DATA_DIR  = "data";
    private static final String DATA_FILE = "data/users.json";

    private List<Parent> parents;
    private List<Child>  children;

    private DataManager() {
        this.parents  = new ArrayList<>();
        this.children = new ArrayList<>();
        try { Files.createDirectories(Paths.get(DATA_DIR)); }
        catch (IOException e) { System.err.println("Klasör hatası: " + e.getMessage()); }
        loadData();
    }

    // ================================================================
    // KAYDET
    // ================================================================

    public void saveData() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"parents\": [\n");
        for (int i = 0; i < parents.size(); i++) {
            sb.append(parentToJson(parents.get(i)));
            if (i < parents.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"children\": [\n");
        for (int i = 0; i < children.size(); i++) {
            sb.append(childToJson(children.get(i)));
            if (i < children.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");

        try (Writer w = new FileWriter(DATA_FILE)) {
            w.write(sb.toString());
        } catch (IOException e) {
            System.err.println("Kayıt hatası: " + e.getMessage());
        }
    }

    // ================================================================
    // YÜKLE
    // ================================================================

    public void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) { seedDefaultData(); saveData(); return; }
        try {
            String content = new String(Files.readAllBytes(Paths.get(DATA_FILE)));
            parents  = parseParents(content);
            children = parseChildren(content);
            System.out.println("Yüklendi: " + parents.size() + " ebeveyn, " + children.size() + " çocuk.");
        } catch (Exception e) {
            System.err.println("Yükleme hatası: " + e.getMessage());
            seedDefaultData();
        }
    }

    // ================================================================
    // JSON YAZICI
    // ================================================================

    private String parentToJson(Parent p) {
        return "    {\n"
            + "      \"type\": \"parent\",\n"
            + "      \"id\": \""          + esc(p.getId())       + "\",\n"
            + "      \"username\": \""    + esc(p.getUsername()) + "\",\n"
            + "      \"password\": \""    + esc(p.getPassword()) + "\",\n"
            + "      \"fullName\": \""    + esc(p.getFullName()) + "\",\n"
            + "      \"childrenIds\": "   + stringListToJson(p.getChildrenIds()) + ",\n"
            + "      \"tasks\": "         + tasksToJson(p.getTasks())   + ",\n"
            + "      \"events\": "        + eventsToJson(p.getEvents()) + ",\n"
            + "      \"rewardRules\": "   + rewardRulesToJson(p.getRewardRules()) + "\n"
            + "    }";
    }

    private String childToJson(Child c) {
        return "    {\n"
            + "      \"type\": \"child\",\n"
            + "      \"id\": \""               + esc(c.getId())       + "\",\n"
            + "      \"username\": \""          + esc(c.getUsername()) + "\",\n"
            + "      \"password\": \""          + esc(c.getPassword()) + "\",\n"
            + "      \"fullName\": \""          + esc(c.getFullName()) + "\",\n"
            + "      \"age\": "                 + c.getAge()           + ",\n"
            + "      \"parentId\": \""          + esc(c.getParentId()) + "\",\n"
            + "      \"points\": "              + c.getPoints()        + ",\n"
            + "      \"completedTaskCount\": "  + c.getCompletedTaskCount() + ",\n"
            + "      \"earnedBadges\": "        + stringListToJson(c.getEarnedBadgeNames()) + ",\n"
            + "      \"tasks\": "               + tasksToJson(c.getTasks())          + ",\n"
            + "      \"assignedTasks\": "       + tasksToJson(c.getAssignedTasks())  + ",\n"
            + "      \"events\": "              + eventsToJson(c.getEvents())        + "\n"
            + "    }";
    }

    private String tasksToJson(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("        {\n")
              .append("          \"id\": \"").append(esc(t.getId())).append("\",\n")
              .append("          \"title\": \"").append(esc(t.getTitle())).append("\",\n")
              .append("          \"description\": \"").append(esc(t.getDescription())).append("\",\n")
              .append("          \"completed\": ").append(t.isCompleted()).append(",\n")
              .append("          \"priority\": \"").append(t.getPriority().name()).append("\",\n")
              .append("          \"dueDate\": \"").append(t.getDueDate() != null ? t.getDueDate().toString() : "").append("\",\n")
              .append("          \"assignedToChildId\": \"").append(esc(t.getAssignedToChildId())).append("\",\n")
              .append("          \"assignedByParentId\": \"").append(esc(t.getAssignedByParentId())).append("\",\n")
              .append("          \"googleEventId\": \"").append(esc(t.getGoogleEventId())).append("\"\n")
              .append("        }");
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        return sb.append("      ]").toString();
    }

    private String eventsToJson(List<CalendarEvent> events) {
        if (events == null || events.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < events.size(); i++) {
            CalendarEvent e = events.get(i);
            sb.append("        {\n")
              .append("          \"id\": \"").append(esc(e.getId())).append("\",\n")
              .append("          \"title\": \"").append(esc(e.getTitle())).append("\",\n")
              .append("          \"description\": \"").append(esc(e.getDescription())).append("\",\n")
              .append("          \"startTime\": \"").append(e.getStartTime()).append("\",\n")
              .append("          \"endTime\": \"").append(e.getEndTime()).append("\",\n")
              .append("          \"eventType\": \"").append(e.getEventType().name()).append("\",\n")
              .append("          \"sharedWithFamily\": ").append(e.isSharedWithFamily()).append(",\n")
              .append("          \"googleEventId\": \"").append(esc(e.getGoogleEventId())).append("\"\n")
              .append("        }");
            if (i < events.size() - 1) sb.append(",");
            sb.append("\n");
        }
        return sb.append("      ]").toString();
    }

    private String rewardRulesToJson(List<RewardRule> rules) {
        if (rules == null || rules.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < rules.size(); i++) {
            RewardRule r = rules.get(i);
            sb.append("        {\n")
              .append("          \"id\": \"").append(esc(r.getId())).append("\",\n")
              .append("          \"description\": \"").append(esc(r.getDescription())).append("\",\n")
              .append("          \"pointThreshold\": ").append(r.getPointThreshold()).append("\n")
              .append("        }");
            if (i < rules.size() - 1) sb.append(",");
            sb.append("\n");
        }
        return sb.append("      ]").toString();
    }

    private String stringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(esc(list.get(i))).append("\"");
            if (i < list.size() - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ================================================================
    // JSON OKUYUCU
    // ================================================================

    private List<Parent> parseParents(String json) {
        List<Parent> list = new ArrayList<>();
        String block = extractBlock(json, "\"parents\"");
        if (block == null) return list;
        for (String obj : extractObjects(block)) {
            try {
                Parent p = new Parent(
                    extractValue(obj, "id"),
                    extractValue(obj, "username"),
                    extractValue(obj, "password"),
                    extractValue(obj, "fullName")
                );
                String idsRaw = extractArrayRaw(obj, "childrenIds");
                if (idsRaw != null)
                    for (String id : parseStringArray(idsRaw)) p.addChildId(id);
                String tasksRaw = extractArrayRaw(obj, "tasks");
                if (tasksRaw != null) p.setTasks(parseTasks(tasksRaw));
                String eventsRaw = extractArrayRaw(obj, "events");
                if (eventsRaw != null) p.setEvents(parseEvents(eventsRaw));
                String rulesRaw = extractArrayRaw(obj, "rewardRules");
                if (rulesRaw != null) p.setRewardRules(parseRewardRules(rulesRaw));
                list.add(p);
            } catch (Exception e) { System.err.println("Parent parse: " + e.getMessage()); }
        }
        return list;
    }

    private List<Child> parseChildren(String json) {
        List<Child> list = new ArrayList<>();
        String block = extractBlock(json, "\"children\"");
        if (block == null) return list;
        for (String obj : extractObjects(block)) {
            try {
                int age = 0;
                try { age = Integer.parseInt(extractValue(obj, "age")); } catch (Exception ignored) {}
                Child c = new Child(
                    extractValue(obj, "id"),
                    extractValue(obj, "username"),
                    extractValue(obj, "password"),
                    extractValue(obj, "fullName"),
                    age,
                    extractValue(obj, "parentId")
                );
                String pointsStr = extractValue(obj, "points");
                if (!pointsStr.isEmpty()) { try { c.setPoints(Integer.parseInt(pointsStr)); } catch (Exception ignored) {} }
                String ctcStr = extractValue(obj, "completedTaskCount");
                if (!ctcStr.isEmpty()) { try { c.setCompletedTaskCount(Integer.parseInt(ctcStr)); } catch (Exception ignored) {} }
                String badgesRaw = extractArrayRaw(obj, "earnedBadges");
                if (badgesRaw != null) c.setEarnedBadgeNames(parseStringArray(badgesRaw));
                String tasksRaw = extractArrayRaw(obj, "tasks");
                if (tasksRaw != null) c.setTasks(parseTasks(tasksRaw));
                String assignedRaw = extractArrayRaw(obj, "assignedTasks");
                if (assignedRaw != null) c.setAssignedTasks(parseTasks(assignedRaw));
                String eventsRaw = extractArrayRaw(obj, "events");
                if (eventsRaw != null) c.setEvents(parseEvents(eventsRaw));
                list.add(c);
            } catch (Exception e) { System.err.println("Child parse: " + e.getMessage()); }
        }
        return list;
    }

    private List<Task> parseTasks(String arrayContent) {
        List<Task> list = new ArrayList<>();
        for (String obj : extractObjects(arrayContent)) {
            try {
                Task.Priority priority;
                try { priority = Task.Priority.valueOf(extractValue(obj, "priority")); }
                catch (Exception e) { priority = Task.Priority.MEDIUM; }

                LocalDate dueDate = null;
                String dateStr = extractValue(obj, "dueDate");
                if (dateStr != null && !dateStr.isEmpty()) {
                    try { dueDate = LocalDate.parse(dateStr); } catch (Exception ignored) {}
                }

                Task t = new Task(
                    extractValue(obj, "id"),
                    extractValue(obj, "title"),
                    extractValue(obj, "description"),
                    priority, dueDate
                );
                t.setCompleted(Boolean.parseBoolean(extractValue(obj, "completed")));
                String toChild  = extractValue(obj, "assignedToChildId");
                String byParent = extractValue(obj, "assignedByParentId");
                String gEventId = extractValue(obj, "googleEventId");
                if (!toChild.isEmpty())  t.setAssignedToChildId(toChild);
                if (!byParent.isEmpty()) t.setAssignedByParentId(byParent);
                if (!gEventId.isEmpty()) t.setGoogleEventId(gEventId);
                list.add(t);
            } catch (Exception e) { System.err.println("Task parse: " + e.getMessage()); }
        }
        return list;
    }

    private List<CalendarEvent> parseEvents(String arrayContent) {
        List<CalendarEvent> list = new ArrayList<>();
        for (String obj : extractObjects(arrayContent)) {
            try {
                CalendarEvent.EventType type;
                try { type = CalendarEvent.EventType.valueOf(extractValue(obj, "eventType")); }
                catch (Exception e) { type = CalendarEvent.EventType.OTHER; }

                CalendarEvent ev = new CalendarEvent(
                    extractValue(obj, "id"),
                    extractValue(obj, "title"),
                    extractValue(obj, "description"),
                    LocalDateTime.parse(extractValue(obj, "startTime")),
                    LocalDateTime.parse(extractValue(obj, "endTime")),
                    type
                );
                ev.setSharedWithFamily(Boolean.parseBoolean(extractValue(obj, "sharedWithFamily")));
                String gid = extractValue(obj, "googleEventId");
                if (!gid.isEmpty()) ev.setGoogleEventId(gid);
                list.add(ev);
            } catch (Exception e) { System.err.println("Event parse: " + e.getMessage()); }
        }
        return list;
    }

    private List<RewardRule> parseRewardRules(String arrayContent) {
        List<RewardRule> list = new ArrayList<>();
        for (String obj : extractObjects(arrayContent)) {
            try {
                int threshold = 0;
                try { threshold = Integer.parseInt(extractValue(obj, "pointThreshold")); } catch (Exception ignored) {}
                list.add(new RewardRule(
                    extractValue(obj, "id"),
                    extractValue(obj, "description"),
                    threshold
                ));
            } catch (Exception e) { System.err.println("RewardRule parse: " + e.getMessage()); }
        }
        return list;
    }

    // ================================================================
    // PARSE YARDIMCI
    // ================================================================

    private String extractValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int colon = json.indexOf(":", idx + search.length());
        if (colon < 0) return "";
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return "";
        char first = json.charAt(start);
        if (first == '"') {
            int end = start + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && ",}\n\r".indexOf(json.charAt(end)) < 0) end++;
            return json.substring(start, end).trim();
        }
    }

    private String extractArrayRaw(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int bracket = json.indexOf("[", idx + search.length());
        if (bracket < 0) return null;
        int depth = 0, end = bracket;
        for (int i = bracket; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) { end = i; break; } }
        }
        return json.substring(bracket + 1, end);
    }

    private String extractBlock(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int bracket = json.indexOf("[", idx);
        if (bracket < 0) return null;
        int depth = 0, end = bracket;
        for (int i = bracket; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) { end = i; break; } }
        }
        return json.substring(bracket + 1, end);
    }

    private List<String> extractObjects(String arrayContent) {
        List<String> list = new ArrayList<>();
        if (arrayContent == null) return list;
        int i = 0;
        while (i < arrayContent.length()) {
            if (arrayContent.charAt(i) == '{') {
                int depth = 0, start = i;
                for (int j = i; j < arrayContent.length(); j++) {
                    char c = arrayContent.charAt(j);
                    if (c == '{') depth++;
                    else if (c == '}') { depth--; if (depth == 0) { list.add(arrayContent.substring(start, j + 1)); i = j + 1; break; } }
                }
            } else i++;
        }
        return list;
    }

    private List<String> parseStringArray(String content) {
        List<String> list = new ArrayList<>();
        if (content == null || content.isBlank()) return list;
        int i = 0;
        while (i < content.length()) {
            if (content.charAt(i) == '"') {
                int end = content.indexOf('"', i + 1);
                if (end < 0) break;
                list.add(content.substring(i + 1, end));
                i = end + 1;
            } else i++;
        }
        return list;
    }

    // ================================================================
    // KULLANICI İŞLEMLERİ
    // ================================================================

    public User login(String username, String password) {
        for (Parent p : parents)
            if (p.getUsername().equals(username) && p.getPassword().equals(password)) return p;
        for (Child c : children)
            if (c.getUsername().equals(username) && c.getPassword().equals(password)) return c;
        return null;
    }

    public boolean isUsernameTaken(String username) {
        return parents.stream().anyMatch(p -> p.getUsername().equals(username))
            || children.stream().anyMatch(c -> c.getUsername().equals(username));
    }

    public void addParent(Parent p)  { parents.add(p);   saveData(); }
    public void addChild(Child c)    { children.add(c);  saveData(); }

    public void updateParent(Parent updated) {
        parents.replaceAll(p -> p.getId().equals(updated.getId()) ? updated : p);
        saveData();
    }
    public void updateChild(Child updated) {
        children.replaceAll(c -> c.getId().equals(updated.getId()) ? updated : c);
        saveData();
    }

    public Parent getParentById(String id) {
        return parents.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }
    public Child getChildById(String id) {
        return children.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Child> getChildrenByParentId(String parentId) {
        List<Child> result = new ArrayList<>();
        for (Child c : children) if (parentId.equals(c.getParentId())) result.add(c);
        return result;
    }

    public List<Parent> getAllParents()  { return parents; }
    public List<Child>  getAllChildren() { return children; }

    public static String generateId() {
        return System.currentTimeMillis() + "" + (int)(Math.random() * 1000);
    }

    private void seedDefaultData() {
        String pid = generateId(), cid = generateId();
        Parent p = new Parent(pid, "ebeveyn", "1234", "Ayşe Yılmaz");
        Child  c = new Child(cid, "cocuk", "1234", "Ali Yılmaz", 12, pid);
        p.addChildId(cid);
        parents.add(p);
        children.add(c);
        System.out.println("Demo → ebeveyn/1234 ve cocuk/1234");
    }
}
