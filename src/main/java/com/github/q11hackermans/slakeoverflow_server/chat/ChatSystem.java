package com.github.q11hackermans.slakeoverflow_server.chat;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.console.ConsoleCommands;
import com.github.q11hackermans.slakeoverflow_server.constants.AccountPermissionLevel;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import com.github.q11hackermans.slakeoverflow_server.game.Snake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatSystem {

    private final SlakeoverflowServer server;
    private final JSONArray log;

    public ChatSystem(SlakeoverflowServer server) {
        this.server = server;
        this.log = new JSONArray();
    }

    public void sendPublicChatMessage(String from, String message) {
        this.send("[CHAT] " + from + ": " + message, false);
    }

    public void sendPrivateChatMessage(String from, ServerConnection to, String message) {
        String username;
        if(to.getAccount() != null) {
            username = to.getAccount().getUsername();
        } else {
            username = String.valueOf(to.getClientId());
        }

        this.sendSocialSpy(from, username, message);
        this.send("[MSG] " + from + " --> YOU: " + message, false, to);
    }

    public void adminMessage(String message, boolean title) {
        this.send("[ADMIN] " + message, title);
    }

    public void adminMessage(String message, boolean title, ServerConnection to) {
        this.send("[ADMIN] " + message, title, to);
    }

    public void sendAdminChat(String from, String message) {
        for(ServerConnection connection : this.server.getConnectionList()) {
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
        for (ServerConnection connection : this.server.getConnectionList()) {
            connection.sendUTF(msg.toString());
        }
    }

    public void send(String message, boolean title, ServerConnection connection) {
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
        if(this.server.getConfigManager().getConfig().isVerboseChatLogs()) {
            this.server.getLogger().info("CHAT", "[VERBOSE] " + connection.getClientId() + ": " + message.toString());
        }

        if(message.has("msg")) {
            String msg = message.getString("msg");

            if(msg.startsWith("/")) {

                String[] command = msg.replace("/", "").split(" ");

                if(this.server.getConfigManager().getConfig().isPrintChatCommandsToConsole()) {
                    this.server.getLogger().info("CHAT", "[COMMAND] [SEND] " + connection.getClientId() + ": " + Arrays.toString(command));
                }

                String result = this.chatCommand(connection, command);

                if(this.server.getConfigManager().getConfig().isPrintChatCommandsToConsole()) {
                    this.server.getLogger().info("CHAT", "[COMMAND] [RESULT] " + connection.getClientId() + ": " + result);
                }

                this.multipleLinesMessage(result, connection);

            } else {

                boolean chatEnabledCondition = this.getChatEnabledCondition(connection);

                if(this.server.getConfigManager().getConfig().isPrintChatToConsole()) {
                    this.server.getLogger().info("CHAT", "[PUBLIC] " + connection.getClientId() + "(message_sent= + " + chatEnabledCondition + "): " + msg);
                }

                if(chatEnabledCondition) {
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

            if(cmd[0].equalsIgnoreCase("admin") && this.server.getConfigManager().getConfig().isEnableAdminCommand()) {
                if(account != null) {
                    if(account.getPermissionLevel() == AccountPermissionLevel.ADMIN) {

                        if(cmd.length >= 2) {
                            String[] args = new String[cmd.length-2];

                            for(int i = 2; i<cmd.length; i++) {
                                args[i-2] = cmd[i];
                            }

                            if(cmd[1].equalsIgnoreCase("config")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("config", args));
                            } else if(cmd[1].equalsIgnoreCase("connection")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("connection", args));
                            } else if(cmd[1].equalsIgnoreCase("user")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("user", args));
                            } else if(cmd[1].equalsIgnoreCase("account")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("account", args));
                            } else if(cmd[1].equalsIgnoreCase("blacklist")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("blacklist", args));
                            } else if(cmd[1].equalsIgnoreCase("game")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("game", args));
                            } else if(cmd[1].equalsIgnoreCase("logger")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("logger", args));
                            } else if(cmd[1].equalsIgnoreCase("info")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("info", args));
                            } else if(cmd[1].equalsIgnoreCase("stop")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("stop", args));
                            } else if(cmd[1].equalsIgnoreCase("help")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("help", args));
                            } else if(cmd[1].equalsIgnoreCase("shop")) {
                                return ConsoleCommands.run(this.server, this.createCmdArray("shop", args));
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

                            AccountData receiver = this.server.getAccountSystem().getAccount(cmd[1]);

                            if(receiver != null) {
                                List<ServerConnection> receiverConnections = this.server.getConnectionsByAccountId(receiver.getId());

                                for(ServerConnection receiverConnection : receiverConnections) {
                                    if(receiverConnection != null) {
                                        this.sendPrivateChatMessage(account.getUsername(), receiverConnection, messageString);
                                    }
                                }

                                if(!receiverConnections.isEmpty()) {
                                    return "YOU --> " + account.getUsername() + ":" + messageString;
                                } else {
                                    return "User is not online";
                                }

                            } else {
                                return "User does not exist";
                            }
                        } else {
                            return "Usage: /msg <TO> <MESSAGE>";
                        }
                    } else {
                        return "You don't have the permission to send private messages";
                    }
                } else {
                    return "You need to be logged in to send private messages";
                }
            } else if (cmd[0].equalsIgnoreCase("account")) {
                if(cmd.length >= 2) {
                    if(cmd[1].equalsIgnoreCase("login") && cmd.length == 4) {
                        if(account == null) {
                            if(this.server.loginConnection(commandExecutor.getClientId(), this.server.getAccountSystem().getAccount(cmd[2]).getId(), cmd[3], false)) {
                                return "Login successful";
                            } else {
                                return "Login failed";
                            }
                        } else {
                            return "You are already logged in";
                        }
                    } else if(cmd[1].equalsIgnoreCase("logout")) {
                        if(account != null) {
                            if(this.server.logoutConnection(commandExecutor.getClientId(), false)) {
                                return "Logout successful";
                            } else {
                                return "Logout failed";
                            }
                        } else {
                            return "You are already logged out";
                        }
                    } else if (cmd[1].equalsIgnoreCase("info")) {
                        if (account != null) {
                            String password;
                            if(account.getPassword() != null) {
                                password = "AVAIL";
                            } else {
                                password = "DISABLED";
                            }

                            return "YOUR ACCOUNT DETAILS:\n" +
                                    "Account ID: " + account.getId() + "\n" +
                                    "Username: " + account.getUsername() + "\n" +
                                    "Password: " + password + "\n" +
                                    "Permission: " + AccountPermissionLevel.toString(account.getPermissionLevel()) + "\n" +
                                    "Muted: " + account.isMuted() + "\n" +
                                    "Banned: " + account.isBanned() + "\n" +
                                    "Level: " + account.getLevel() + "\n" +
                                    "Balance: " + account.getBalance() + "\n" +
                                    "Shop data: " + account.getShopData().toString() + "\n";
                        } else {
                            return "You are not logged in";
                        }
                    } else if (cmd[1].equalsIgnoreCase("update") && cmd.length == 4) {
                        if(account != null) {
                            if(cmd[2].equalsIgnoreCase("username")) {
                                if(this.server.getAccountSystem().updateUsername(account.getId(), cmd[3])) {
                                    return "Updated username";
                                } else {
                                    return "Username change failed";
                                }
                            } else if(cmd[2].equalsIgnoreCase("password")) {
                                if(this.server.getAccountSystem().updatePassword(account.getId(), cmd[3])) {
                                    return "Updated password";
                                } else {
                                    return "Username change failed";
                                }
                            } else {
                                return "You can only update your username or password";
                            }
                        } else {
                            return "You are not logged in";
                        }
                    } else if(cmd[1].equalsIgnoreCase("delete")) {
                        if(account != null) {
                            if(cmd.length == 3 && cmd[2].equals("ISwearIKnowWhatImDoing")) {
                                if(this.server.getAccountSystem().deleteAccount(account.getId())) {
                                    commandExecutor.logout();
                                    return "Your account was deleted and you was logged out";
                                } else {
                                    return "Account was not deleted";
                                }
                            } else {
                                return "Run the command like this to delete your account: account delete ISwearIKnowWhatImDoing";
                            }
                        } else {
                            return "You need to be logged in to do that";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                } else {
                    return "ACCOUNT COMMAND USAGE:\n" +
                            "account login <username> <password>\n" +
                            "account logout\n" +
                            "account info\n" +
                            "account update username/password <value>\n" +
                            "account delete\n";
                }
            } else if (cmd[0].equalsIgnoreCase("togglefov")) {
                if(commandExecutor.getAuthenticationState() == AuthenticationState.PLAYER) {
                    Snake snake = this.server.getGameSession().getSnakeOfConnection(commandExecutor);
                    if(snake != null) {
                        snake.setFixedFovPlayerdataSystem(!snake.isFixedFovPlayerdataSystem());
                        return "Toggled FOV mode";
                    } else {
                        return "You are not ingame";
                    }
                } else {
                    return "You are not ingame";
                }
            } else if (cmd[0].equalsIgnoreCase("help")) {
                String returnString = "CHAT COMMANDS HELP:\n" +
                        "help - show this page\n" +
                        "account - manage your account\n" +
                        "msg - send private messages\n" +
                        "togglefov - switch between FOV scrolling/fixed mode";

                if(this.server.getConfigManager().getConfig().isEnableAdminCommand() && commandExecutor.getAccount() != null && commandExecutor.getAccount().getPermissionLevel() == AccountPermissionLevel.ADMIN) {
                    returnString = returnString + "admin - run any console commands via c";
                }

                return returnString;
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

    // SOCIALSPY

    public void sendSocialSpy(String sender, String receiver, String message) {
        for(ServerConnection connection : this.server.getConnectionList()) {
            if(connection.isSocialSpy()) {
                this.send("[SOCIALSPY] [" + sender + " --> " + receiver + "] " + message, false, connection);
            }
        }
    }

    // UTILITIES

    private String getTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private boolean getChatEnabledCondition(ServerConnection connection) {
        boolean isPunished = connection.isBanned() || connection.isMuted();
        boolean chatEnabled = this.server.getConfigManager().getConfig().isEnableChat();
        boolean guestChat = this.server.getConfigManager().getConfig().isAllowGuestChat();
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

    public void multipleLinesMessage(String message, ServerConnection connection) {
        String[] lines = message.split("\n");

        for(String line : lines) {
            this.send(line, false, connection);
        }
    }

    // GETTER

    public JSONArray getLog() {
        return this.log;
    }

    public SlakeoverflowServer getServer() {
        return this.server;
    }
}
