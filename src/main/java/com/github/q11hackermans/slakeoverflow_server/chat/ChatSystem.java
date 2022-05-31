package com.github.q11hackermans.slakeoverflow_server.chat;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.config.ConfigManager;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.console.ConsoleCommands;
import com.github.q11hackermans.slakeoverflow_server.constants.AccountPermissionLevel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        if(message.has("msg")) {
            String msg = message.getString("msg");

            if(msg.startsWith("/")) {

                this.send(this.chatCommand(connection, msg.replace("/", "").split(" ")), false, connection);

            } else {

                if(this.getChatEnabledCondition(connection)) {
                    AccountData account = connection.getAccount();

                    String accountName;
                    if(account != null) {
                        accountName = account.getUsername();
                    } else {
                        try {
                            accountName = connection.getClientId().toString().split("-")[0];
                        } catch(ArrayIndexOutOfBoundsException e) {
                            accountName = "GUEST";
                        }
                    }

                    this.addLogEntry("RECEIVED", String.valueOf(connection.getAccountId()), "EVERYONE", msg, true);
                    this.sendPublicChatMessage(accountName, msg);
                } else {
                    this.addLogEntry("RECEIVED", String.valueOf(connection.getAccountId()), "EVERYONE", msg, false);
                    this.send("You don't have the permission to use the chat", false, connection);
                }

            }
        }
    }

    public String chatCommand(ServerConnection commandExecutor, String[] cmd) {
        if(cmd.length >= 1) {

            if(cmd[0].equalsIgnoreCase("admin") && SlakeoverflowServer.getServer().getConfigManager().getConfig().isEnableAdminCommand()) {
                AccountData account = commandExecutor.getAccount();

                if(account != null) {
                    if(account.getPermissionLevel() == AccountPermissionLevel.ADMIN) {

                        if(cmd.length >= 2) {
                            String[] args = new String[cmd.length-2];

                            for(int i = 2; i<cmd.length; i++) {
                                args[i-2] = cmd[i];
                            }

                            if(cmd[1].equalsIgnoreCase("config")) {
                                return ConsoleCommands.run(new String[]{})
                            } else if(cmd[1].equalsIgnoreCase("connection")) {

                            } else if(cmd[1].equalsIgnoreCase("user")) {

                            } else if(cmd[1].equalsIgnoreCase("account")) {

                            } else if(cmd[1].equalsIgnoreCase("blacklist")) {

                            } else if(cmd[1].equalsIgnoreCase("game")) {

                            } else if(cmd[1].equalsIgnoreCase("logger")) {

                            } else if(cmd[1].equalsIgnoreCase("info")) {

                            } else if(cmd[1].equalsIgnoreCase("stop")) {

                            } else if(cmd[1].equalsIgnoreCase("help")) {

                            } else {
                                return "Unknown command";
                            }
                        } else {
                            return "USAGE: /admin <ADMIN COMMAND> (Use /admin help for command list)";
                        }
                    } else {
                        return "No permission";
                    }
                } else {
                    return "No permission";
                }
            }

        } else {
            return "Unknown command";
        }
    }

    private String[] createCmdArray(String command, String[] args) {
        String [] cmd = new String[args.length+1];

        cmd[0] = command;

        for(int i = 1; i < args.length, i++) {

        }
    }

    // LOGGER
    private void addLogEntry(String type, String sender, String receiver, String message, boolean sent) {
        JSONObject logEntry = new JSONObject();

        logEntry.put("type", type);
        logEntry.put("time", this.getTimeString());
        logEntry.put("sender", sender);
        logEntry.put("receiver", receiver);
        logEntry.put("message", message);
        logEntry.put("sent", sent);

        this.log.put(logEntry);
    }

    // UTILITIES
    private String getTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private boolean getChatEnabledCondition(ServerConnection connection) {
        boolean chatEnabled = SlakeoverflowServer.getServer().getConfigManager().getConfig().isEnableChat();
        boolean guestChat = SlakeoverflowServer.getServer().getConfigManager().getConfig().isAllowGuestChat();
        AccountData account = connection.getAccount();
        boolean isAdmin = (account != null && (account.getPermissionLevel() == AccountPermissionLevel.MODERATOR || account.getPermissionLevel() == AccountPermissionLevel.ADMIN));
        boolean isUser = (account != null);

        if(isAdmin) {
            return true;
        }

        if(chatEnabled && isUser) {
            return true;
        }

        if(chatEnabled && guestChat) {
            return true;
        }

        return false;
    }
}
