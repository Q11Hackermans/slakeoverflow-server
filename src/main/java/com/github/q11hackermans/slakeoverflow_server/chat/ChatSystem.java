package com.github.q11hackermans.slakeoverflow_server.chat;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.AccountPermissionLevel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatSystem {
    private final JSONArray log;

    public ChatSystem() {
        this.log = new JSONArray();
    }

    public void sendPublicChatMessage(String from, String message) {
        this.send("[CHAT] " + from + ": " + message, false);
    }

    public void sendPrivateChatMessage(String from, ServerConnection to, String message) {
        this.send("[MSG] " + from + " --> YOU: " + message, false, to);
    }

    public void sendAdminChat(String from, String message) {
        for(ServerConnection connection : SlakeoverflowServer.getServer().getConnectionList()) {
            if(connection.getAccount() != null && connection.getAccount().getPermissionLevel() == AccountPermissionLevel.ADMIN) {
                this.send("[AC] " + from + ": " + message, false, connection);
            }
        }
    }

    public void adminBroadcast(String message, boolean title) {
        this.send("[ADMIN] " + message, title);
    }

    // BASIC MESSAGES
    public void send(String message, boolean title) {
        JSONObject msg = new JSONObject();

        msg.put("cmd", "message");
        msg.put("title", title);
        msg.put("msg", message);

        for (ServerConnection connection : SlakeoverflowServer.getServer().getConnectionList()) {
            connection.sendUTF(msg.toString());
        }
    }

    public void send(String message, boolean title, ServerConnection connection) {
        JSONObject msg = new JSONObject();

        msg.put("cmd", message);
        msg.put("title", title);
        msg.put("msg", message);

        connection.sendUTF(msg.toString());
    }

    // RECEIVING
    public void onChatEventReceived(ServerConnection connection, JSONObject message) {

    }

    // LOGGER
    private void addLogEntry(String type, String sender, String receiver, String message) {
        JSONObject logEntry = new JSONObject();

        logEntry.put("type", type);
        logEntry.put("time", this.getTimeString());
        logEntry.put("sender", sender);
        logEntry.put("receiver", receiver);
        logEntry.put("message", message);

        this.log.put(logEntry);
    }

    private String getTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
