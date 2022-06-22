package com.github.q11hackermans.slakeoverflow_server.chat;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.console.ConsoleCommands;
import com.github.q11hackermans.slakeoverflow_server.constants.AccountPermissionLevel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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

        String type;
        if(title) {
            type = "MESSAGE_TITLE";
        } else {
            type = "MESSAGE";
        }

        this.addLogEntry(type, "@everyone", message);
        for (ServerConnection connection : SlakeoverflowServer.getServer().getConnectionList()) {
            connection.sendUTF(msg.toString());
        }
    }

    public void send(String message, boolean title, ServerConnection connection) {
        JSONObject msg = new JSONObject();

        msg.put("cmd", message);
        msg.put("title", title);
        msg.put("msg", message);

        String type;
        if(title) {
            type = "MESSAGE_TITLE";
        } else {
            type = "MESSAGE";
        }

        AccountData account = connection.getAccount();
        String receiver;
        if(account != null) {
            receiver = String.valueOf(account.getId());
        } else {
            receiver = String.valueOf(connection.getClientId());
        }

        this.addLogEntry(type, receiver, message);
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

                    this.sendPublicChatMessage(accountName, msg);
                } else {
                    this.send("You don't have the permission to use the chat", false, connection);
                }

            }
        }
    }

    public String chatCommand(ServerConnection commandExecutor, String[] cmd) {
        AccountData account = commandExecutor.getAccount();
        String accountString;

        if(account != null) {
            accountString = String.valueOf(account.getId());
        } else {
            accountString = String.valueOf(commandExecutor.getClientId());
        }

        this.addLogEntry("COMMAND", accountString, Arrays.toString(cmd));

        if(cmd.length >= 1) {

            if(cmd[0].equalsIgnoreCase("admin") && SlakeoverflowServer.getServer().getConfigManager().getConfig().isEnableAdminCommand()) {
                if(account != null) {
                    if(account.getPermissionLevel() == AccountPermissionLevel.ADMIN) {

                        if(cmd.length >= 2) {
                            String[] args = new String[cmd.length-2];

                            for(int i = 2; i<cmd.length; i++) {
                                args[i-2] = cmd[i];
                            }

                            if(cmd[1].equalsIgnoreCase("config")) {
                                return ConsoleCommands.run(this.createCmdArray("config", args));
                            } else if(cmd[1].equalsIgnoreCase("connection")) {
                                return ConsoleCommands.run(this.createCmdArray("connection", args));
                            } else if(cmd[1].equalsIgnoreCase("user")) {
                                return ConsoleCommands.run(this.createCmdArray("user", args));
                            } else if(cmd[1].equalsIgnoreCase("account")) {
                                return ConsoleCommands.run(this.createCmdArray("account", args));
                            } else if(cmd[1].equalsIgnoreCase("blacklist")) {
                                return ConsoleCommands.run(this.createCmdArray("blacklist", args));
                            } else if(cmd[1].equalsIgnoreCase("game")) {
                                return ConsoleCommands.run(this.createCmdArray("game", args));
                            } else if(cmd[1].equalsIgnoreCase("logger")) {
                                return ConsoleCommands.run(this.createCmdArray("logger", args));
                            } else if(cmd[1].equalsIgnoreCase("info")) {
                                return ConsoleCommands.run(this.createCmdArray("info", args));
                            } else if(cmd[1].equalsIgnoreCase("stop")) {
                                return ConsoleCommands.run(this.createCmdArray("stop", args));
                            } else if(cmd[1].equalsIgnoreCase("help")) {
                                return ConsoleCommands.run(this.createCmdArray("help", args));
                            } else if(cmd[1].equalsIgnoreCase("shop")) {
                                return ConsoleCommands.run(this.createCmdArray("shop", args));
                            } else {
                                return "Use /admin help for a list of commands";
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
            } else if(cmd[0].equalsIgnoreCase("msg")) {
                if(account != null) {
                    if(this.getChatEnabledCondition(commandExecutor)) {
                        if(cmd.length >= 3) {
                            String messageString = "";

                            for(int i = 2; i < cmd.length; i++) {
                                messageString = messageString + " " + cmd[i];
                            }

                            AccountData receiver = SlakeoverflowServer.getServer().getAccountSystem().getAccount(cmd[1]);
                            ServerConnection receiverConnection = SlakeoverflowServer.getServer().getConnectionByAccountId(receiver.getId());

                            this.sendPrivateChatMessage(account.getUsername(), receiverConnection, messageString);
                            return "YOU --> " + account.getUsername() + ":" + messageString;
                        } else {
                            return "Usage: /msg <TO> <MESSAGE>";
                        }
                    } else {
                        return "You don't have the permission to send private messages";
                    }
                } else {
                    return "You need to be logged in to send private messages";
                }
            } else if (cmd[0].equalsIgnoreCase("login")) {
                if(account == null) {
                    if(cmd.length == 3) {
                        // LOGIN
                    } else {
                        return "USAGE: /login <username> <password>";
                    }
                } else {
                    return "You are already logged in";
                }
            } else {
                return "Unknown command";
            }

        } else {
            return "Unknown command";
        }
    }

    private String[] createCmdArray(String command, String[] args) {
        String [] cmd = new String[args.length+1];

        cmd[0] = command;

        for(int i = 1; i < cmd.length; i++) {
            cmd[i] = args[i-1];
        }

        return cmd;
    }

    // LOGGER
    private void addLogEntry(String type, String user, String message) {
        JSONObject logEntry = new JSONObject();

        logEntry.put("time", this.getTimeString());
        logEntry.put("type", type);
        logEntry.put("user", user);
        logEntry.put("message", message);

        this.log.put(logEntry);
    }

    // UTILITIES
    private String getTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private boolean getChatEnabledCondition(ServerConnection connection) {
        boolean isPunished = connection.isBanned() || connection.isMuted();
        boolean chatEnabled = SlakeoverflowServer.getServer().getConfigManager().getConfig().isEnableChat();
        boolean guestChat = SlakeoverflowServer.getServer().getConfigManager().getConfig().isAllowGuestChat();
        AccountData account = connection.getAccount();
        boolean isAdmin = (account != null && (account.getPermissionLevel() == AccountPermissionLevel.MODERATOR || account.getPermissionLevel() == AccountPermissionLevel.ADMIN));
        boolean isUser = (account != null);

        if(isAdmin) {
            return true;
        }

        if(isPunished) {
            return false;
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
