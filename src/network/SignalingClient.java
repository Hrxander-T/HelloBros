package network;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class SignalingClient {

    private static final String SERVER = "https://hellobros-signal.hellobros.workers.dev";
    private static final HttpClient http = HttpClient.newHttpClient();

    private String roomID;

    // ── Host: create room ──────────────────
    public String createRoom(int port) throws Exception {
        String body = "{\"port\":" + port + "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER + "/create"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = http.send(request, BodyHandlers.ofString());
        roomID = extractValue(response.body(), "roomID");
        return roomID;
    }

    // ── Host: update port when bore reconnects ─
    public void updatePort(int port) throws Exception {
        if (roomID == null) return;
        String body = "{\"port\":" + port + "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER + "/update/" + roomID))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(body))
            .build();

        http.send(request, BodyHandlers.ofString());
    }

    // ── Client: join room ──────────────────
    public int joinRoom(String roomID) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER + "/join/" + roomID))
            .GET()
            .build();

        HttpResponse<String> response = http.send(request, BodyHandlers.ofString());
        if (response.statusCode() == 404) throw new Exception("Room not found: " + roomID);
        return Integer.parseInt(extractValue(response.body(), "port"));
    }

    // ── Host: delete room on exit ──────────
    public void deleteRoom() {
        if (roomID == null) return;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER + "/delete/" + roomID))
                .DELETE()
                .build();
            http.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Could not delete room: " + e.getMessage());
        }
    }

    public String getRoomID() { return roomID; }

    // ── Simple JSON value extractor ────────
    // avoids needing a JSON library
    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search) + search.length();
        char first = json.charAt(start);
        if (first == '"') {
            // string value
            start++;
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } else {
            // number value
            int end = json.indexOf('}', start);
            return json.substring(start, end).trim();
        }
    }
}