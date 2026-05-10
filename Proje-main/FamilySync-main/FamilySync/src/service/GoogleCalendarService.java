package service;

import model.CalendarEvent;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Calendar API entegrasyonu.
 * OAuth2 yetkilendirmesi ve takvim etkinliği CRUD işlemlerini yönetir.
 * Design Pattern: Singleton
 */
public class GoogleCalendarService {

    private static final String CONFIG_FILE  = "data/google_config.json";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private static final String AUTH_URL     = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URL    = "https://oauth2.googleapis.com/token";
    private static final String CALENDAR_API = "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    private static GoogleCalendarService instance;

    private String clientId      = "";
    private String clientSecret  = "";
    private String accessToken   = "";
    private String refreshToken  = "";

    public static GoogleCalendarService getInstance() {
        if (instance == null) instance = new GoogleCalendarService();
        return instance;
    }

    private GoogleCalendarService() {
        loadConfig();
    }

    // ================================================================
    // DURUM KONTROLÜ
    // ================================================================

    public boolean isConfigured() {
        return !clientId.isEmpty() && !clientSecret.isEmpty();
    }

    public boolean isAuthenticated() {
        return isConfigured() && !refreshToken.isEmpty();
    }

    public void setCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        saveConfig();
    }

    public void disconnect() {
        this.accessToken = "";
        this.refreshToken = "";
        saveConfig();
    }

    // ================================================================
    // OAuth2 AKIŞI
    // ================================================================

    public void authenticate() throws Exception {
        String authUrl = AUTH_URL + "?"
            + "client_id=" + URLEncoder.encode(clientId, "UTF-8")
            + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
            + "&response_type=code"
            + "&scope=" + URLEncoder.encode("https://www.googleapis.com/auth/calendar", "UTF-8")
            + "&access_type=offline"
            + "&prompt=consent";

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            throw new Exception("Tarayıcı açılamıyor. Lütfen şu URL'yi manuel olarak açın:\n" + authUrl);
        }

        String code = listenForAuthCode();
        exchangeCodeForTokens(code);
    }

    private String listenForAuthCode() throws Exception {
        try (ServerSocket server = new ServerSocket(8888)) {
            server.setSoTimeout(120_000);
            try (Socket socket = server.accept()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String requestLine = reader.readLine();

                String html = "<html><body style='font-family:sans-serif;text-align:center;padding:40px'>"
                    + "<h2>&#9989; FamilySync: Yetkilendirme başarılı!</h2>"
                    + "<p>Bu pencereyi kapatabilirsiniz.</p></body></html>";

                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.print("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: "
                    + html.length() + "\r\n\r\n" + html);
                out.flush();

                if (requestLine == null || !requestLine.contains("code=")) {
                    throw new Exception("Yetkilendirme kodu alınamadı.");
                }
                int codeStart = requestLine.indexOf("code=") + 5;
                int codeEnd   = requestLine.indexOf("&", codeStart);
                if (codeEnd < 0) codeEnd = requestLine.indexOf(" ", codeStart);
                return requestLine.substring(codeStart, codeEnd);
            }
        }
    }

    private void exchangeCodeForTokens(String code) throws Exception {
        String body = "code=" + URLEncoder.encode(code, "UTF-8")
            + "&client_id=" + URLEncoder.encode(clientId, "UTF-8")
            + "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8")
            + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
            + "&grant_type=authorization_code";

        String response = postForm(TOKEN_URL, body);
        accessToken  = extractJsonValue(response, "access_token");
        refreshToken = extractJsonValue(response, "refresh_token");

        if (accessToken.isEmpty()) {
            throw new Exception("Token alınamadı. Lütfen Client ID ve Secret'i kontrol edin.");
        }
        saveConfig();
    }

    private void refreshAccessToken() throws Exception {
        String body = "client_id=" + URLEncoder.encode(clientId, "UTF-8")
            + "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8")
            + "&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8")
            + "&grant_type=refresh_token";

        String response = postForm(TOKEN_URL, body);
        String newToken = extractJsonValue(response, "access_token");
        if (newToken.isEmpty()) throw new Exception("Access token yenilenemedi.");
        accessToken = newToken;
        saveConfig();
    }

    // ================================================================
    // TAKVİM ETKİNLİK İŞLEMLERİ
    // ================================================================

    /**
     * Atanan görev için Google Takvim etkinliği oluşturur.
     * @return Google etkinlik ID'si (hata durumunda null)
     */
    public String createTaskEvent(String taskTitle, String taskDescription,
                                   LocalDate dueDate, String assignedTo) {
        if (!isAuthenticated()) return null;
        try {
            String summary  = "[FamilySync] " + taskTitle + " → " + assignedTo;
            String eventJson = "{\n"
                + "  \"summary\": \"" + esc(summary) + "\",\n"
                + "  \"description\": \"" + esc(taskDescription) + "\",\n"
                + "  \"start\": {\"date\": \"" + dueDate.toString() + "\"},\n"
                + "  \"end\": {\"date\": \"" + dueDate.plusDays(1).toString() + "\"},\n"
                + "  \"colorId\": \"6\",\n"
                + "  \"reminders\": {\n"
                + "    \"useDefault\": false,\n"
                + "    \"overrides\": [\n"
                + "      {\"method\": \"email\", \"minutes\": 1440},\n"
                + "      {\"method\": \"popup\", \"minutes\": 120}\n"
                + "    ]\n"
                + "  }\n"
                + "}";

            String response = apiPost(CALENDAR_API, eventJson);
            String eventId  = extractJsonValue(response, "id");
            return eventId.isEmpty() ? null : eventId;
        } catch (Exception e) {
            System.err.println("Google Takvim etkinlik oluşturma hatası: " + e.getMessage());
            return null;
        }
    }

    /** Görevi tamamlandı olarak işaretler (renk + başlık değişir). */
    public void markEventCompleted(String eventId, String taskTitle) {
        if (!isAuthenticated() || eventId == null || eventId.isEmpty()) return;
        try {
            String patchJson = "{\"summary\": \"" + esc("✅ [FamilySync] " + taskTitle) + "\", \"colorId\": \"2\"}";
            String encodedId = URLEncoder.encode(eventId, "UTF-8");
            apiPatch(CALENDAR_API + "/" + encodedId, patchJson);
        } catch (Exception e) {
            System.err.println("Google Takvim güncelleme hatası: " + e.getMessage());
        }
    }

    /** Takvim etkinliğini siler. */
    public void deleteEvent(String eventId) {
        if (!isAuthenticated() || eventId == null || eventId.isEmpty()) return;
        try {
            String encodedId = URLEncoder.encode(eventId, "UTF-8");
            apiDelete(CALENDAR_API + "/" + encodedId);
        } catch (Exception e) {
            System.err.println("Google Takvim silme hatası: " + e.getMessage());
        }
    }

    /**
     * Tarih/saat bilgisi olan takvim etkinliği oluşturur (CalendarGridPanel için).
     * @return Google etkinlik ID'si (hata durumunda null)
     */
    public String createCalendarEvent(String title, String description,
                                       LocalDateTime start, LocalDateTime end) {
        if (!isAuthenticated()) return null;
        try {
            String startStr = start.toString();
            String endStr   = end.toString();
            if (startStr.length() == 16) startStr += ":00";
            if (endStr.length()   == 16) endStr   += ":00";

            String eventJson = "{\n"
                + "  \"summary\": \""     + esc(title)       + "\",\n"
                + "  \"description\": \"" + esc(description) + "\",\n"
                + "  \"start\": {\"dateTime\": \"" + startStr + "\", \"timeZone\": \"Europe/Istanbul\"},\n"
                + "  \"end\":   {\"dateTime\": \"" + endStr   + "\", \"timeZone\": \"Europe/Istanbul\"}\n"
                + "}";

            String response = apiPost(CALENDAR_API, eventJson);
            String eventId  = extractJsonValue(response, "id");
            return eventId.isEmpty() ? null : eventId;
        } catch (Exception e) {
            System.err.println("Google Takvim etkinlik oluşturma hatası: " + e.getMessage());
            return null;
        }
    }

    /**
     * Google Takvim'den gelecek 3 ay + geçmiş 1 aylık etkinlikleri çeker.
     * @return CalendarEvent listesi (boş liste hata durumunda)
     */
    public List<CalendarEvent> fetchGoogleEvents() {
        if (!isAuthenticated()) return new ArrayList<>();
        try {
            String timeMin = LocalDateTime.now().minusMonths(1).toString();
            String timeMax = LocalDateTime.now().plusMonths(3).toString();
            if (timeMin.length() == 16) timeMin += ":00";
            if (timeMax.length() == 16) timeMax += ":00";

            String url = CALENDAR_API
                + "?timeMin=" + URLEncoder.encode(timeMin + "Z", "UTF-8")
                + "&timeMax=" + URLEncoder.encode(timeMax + "Z", "UTF-8")
                + "&singleEvents=true&orderBy=startTime&maxResults=200";

            String response = apiGet(url);
            return parseGoogleEvents(response);
        } catch (Exception e) {
            System.err.println("Google Takvim çekme hatası: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<CalendarEvent> parseGoogleEvents(String json) {
        List<CalendarEvent> list = new ArrayList<>();
        String itemsRaw = extractArrayContent(json, "\"items\"");
        if (itemsRaw == null) return list;

        for (String obj : extractJsonObjects(itemsRaw)) {
            try {
                String id      = extractJsonValue(obj, "id");
                String summary = extractJsonValue(obj, "summary");
                String desc    = extractJsonValue(obj, "description");

                String startStr = extractNestedValue(obj, "start", "dateTime");
                String endStr   = extractNestedValue(obj, "end",   "dateTime");

                if (startStr.isEmpty()) {
                    startStr = extractNestedValue(obj, "start", "date") + "T00:00:00";
                    endStr   = extractNestedValue(obj, "end",   "date") + "T00:00:00";
                }
                // Timezone offset varsa at ("+03:00" gibi)
                if (startStr.length() > 19) startStr = startStr.substring(0, 19);
                if (endStr.length()   > 19) endStr   = endStr.substring(0, 19);

                LocalDateTime start = LocalDateTime.parse(startStr);
                LocalDateTime end   = LocalDateTime.parse(endStr);

                CalendarEvent ev = new CalendarEvent(id, summary, desc, start, end,
                    CalendarEvent.EventType.OTHER);
                ev.setGoogleEventId(id);
                list.add(ev);
            } catch (Exception e) {
                System.err.println("Google event parse hatası: " + e.getMessage());
            }
        }
        return list;
    }

    private String extractNestedValue(String json, String outerKey, String innerKey) {
        String search = "\"" + outerKey + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int brace = json.indexOf("{", idx + search.length());
        if (brace < 0) return "";
        int depth = 0, end = brace;
        for (int i = brace; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') { depth--; if (depth == 0) { end = i; break; } }
        }
        return extractJsonValue(json.substring(brace, end + 1), innerKey);
    }

    private String extractArrayContent(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int bracket = json.indexOf("[", idx + key.length());
        if (bracket < 0) return null;
        int depth = 0, end = bracket;
        for (int i = bracket; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) { end = i; break; } }
        }
        return json.substring(bracket + 1, end);
    }

    private List<String> extractJsonObjects(String content) {
        List<String> list = new ArrayList<>();
        if (content == null) return list;
        int i = 0;
        while (i < content.length()) {
            if (content.charAt(i) == '{') {
                int depth = 0, start = i;
                for (int j = i; j < content.length(); j++) {
                    char c = content.charAt(j);
                    if (c == '{') depth++;
                    else if (c == '}') { depth--; if (depth == 0) { list.add(content.substring(start, j + 1)); i = j + 1; break; } }
                }
            } else i++;
        }
        return list;
    }

    // ================================================================
    // HTTP YARDIMCI METOTlari
    // ================================================================

    private String apiGet(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 401) { refreshAccessToken(); return apiGet(url); }
        return res.body();
    }

    private String postForm(String url, String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String apiPost(String url, String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 401) { refreshAccessToken(); return apiPost(url, json); }
        return res.body();
    }

    private void apiDelete(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .DELETE()
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 401) { refreshAccessToken(); apiDelete(url); }
    }

    private void apiPatch(String url, String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 401) { refreshAccessToken(); apiPatch(url, json); }
    }

    // ================================================================
    // KONFİGÜRASYON
    // ================================================================

    private void loadConfig() {
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return;
            String content = new String(Files.readAllBytes(f.toPath()));
            clientId     = extractJsonValue(content, "clientId");
            clientSecret = extractJsonValue(content, "clientSecret");
            accessToken  = extractJsonValue(content, "accessToken");
            refreshToken = extractJsonValue(content, "refreshToken");
        } catch (Exception e) {
            System.err.println("Google config yükleme hatası: " + e.getMessage());
        }
    }

    private void saveConfig() {
        try {
            Files.createDirectories(Paths.get("data"));
            String json = "{\n"
                + "  \"clientId\": \""     + esc(clientId)     + "\",\n"
                + "  \"clientSecret\": \"" + esc(clientSecret) + "\",\n"
                + "  \"accessToken\": \""  + esc(accessToken)  + "\",\n"
                + "  \"refreshToken\": \"" + esc(refreshToken) + "\"\n"
                + "}";
            Files.writeString(Paths.get(CONFIG_FILE), json);
        } catch (Exception e) {
            System.err.println("Google config kaydetme hatası: " + e.getMessage());
        }
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int colon = json.indexOf(":", idx + search.length());
        if (colon < 0) return "";
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length() || json.charAt(start) != '"') return "";
        int end = start + 1;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
            end++;
        }
        return json.substring(start + 1, end);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
