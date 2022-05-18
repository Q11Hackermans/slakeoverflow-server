package com.github.q11hackermans.slakeoverflow_server.console;

import com.github.q11hackermans.slakeoverflow_server.GameSession;
import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.ConnectionType;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import com.github.q11hackermans.slakeoverflow_server.game.Food;
import com.github.q11hackermans.slakeoverflow_server.game.Item;
import com.github.q11hackermans.slakeoverflow_server.game.Snake;
import com.github.q11hackermans.slakeoverflow_server.game.SuperFood;
import net.jandie1505.connectionmanager.enums.PendingClientState;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.CMSPendingClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConsoleCommands {
    public static String run(String[] cmd) {
        try {
            if(cmd.length >= 1) {
                switch(cmd[0]) {
                    case "stop":
                        SlakeoverflowServer.getServer().stop();
                        return "Shutting down";
                    case "help":
                        return helpCommand();
                    case "config":
                        return configCommand(cmd);
                    case "connection":
                        return connectionCommand(cmd);
                    case "user":
                        return userCommand(cmd);
                    case "blacklist":
                        blacklistCommand(cmd);
                    case "game":
                        return gameCommand(cmd);
                    case "logger":
                        return loggerCommand(cmd);
                    default:
                        return "Unknown command";
                }
            }
        } catch(Exception e) {
            return "Command error: " + e.getMessage();
        }
        return "";
    }

    private static String helpCommand() {
        return "COMMAND LIST:\n" +
                "help: This page\n" +
                "stop: Shutdown server\n" +
                "config: Get or set config\n" +
                "connection: Connection management (ConnectionManager)\n" +
                "user: User management (ServerConnections)\n" +
                "blacklist: IP blacklist management\n" +
                "game: Game management\n" +
                "logger: Logging management\n" +
                "Run the specific commands without arguments to show their help page.\n" +
                "COMMAND SHORTCUTS:\n" +
                "startgame --> game start default\n" +
                "stopgame --> game stop\n" +
                "list --> user list\n" +
                "kick <UUID> --> connection close <UUID>\n" +
                "ban <IP> --> blacklist add <IP>\n" +
                "accept <UUID> --> connection accept <UUID>\n";
    }

    private static String configCommand(String[] cmd) {
        if (cmd.length >= 2) {
            if (cmd[1].equalsIgnoreCase("set")) {
                if(cmd.length == 4) {
                    if (cmd[2].equalsIgnoreCase("port")) {
                        return "Cannot set port while the server is running!";
                    } else if (cmd[2].equalsIgnoreCase("whitelist")) {
                        SlakeoverflowServer.getServer().getConfigManager().getConfig().setWhitelist(Boolean.parseBoolean(cmd[3]));
                        return "Updated value whitelist to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("user_authentication")) {
                        SlakeoverflowServer.getServer().getConfigManager().getConfig().setUserAuthentication(Boolean.parseBoolean(cmd[3]));
                        return "Updated value user_authentication to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("slots")) {
                        try {
                            int slots = Integer.parseInt(cmd[3]);
                            if (slots > 0) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setSlots(slots);
                                return "Updated value slots to " + slots;
                            } else {
                                return "You can only set a positive int value";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("min_food_value")) {
                        try {
                            int minFoodValue = Integer.parseInt(cmd[3]);
                            if (minFoodValue >= 1 && minFoodValue <= 10) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setMinFoodValue(minFoodValue);
                                return "Updated value min_food_value to " + minFoodValue;
                            } else {
                                return "You can only set an int value in range 1-10";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set an int value in range 1-10";
                        }
                    } else if (cmd[2].equalsIgnoreCase("max_food_value")) {
                        try {
                            int maxFoodValue = Integer.parseInt(cmd[3]);
                            if (maxFoodValue >= 1 && maxFoodValue <= 10) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setMaxFoodValue(maxFoodValue);
                                return "Updated value max_food_value to " + maxFoodValue;
                            } else {
                                return "You can only set an int value in range 1-10";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set an int value in range 1-10";
                        }
                    } else if(cmd[2].equalsIgnoreCase("default_snake_length")) {
                        try {
                            int defaultSnakeLength = Integer.parseInt(cmd[3]);
                            if(defaultSnakeLength >= 1 && defaultSnakeLength <= 10) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setDefaultSnakeLength(defaultSnakeLength);
                                return "Updated value default_snake_length to " + defaultSnakeLength;
                            } else {
                                return "You can only set an int value in range 1-10";
                            }
                        } catch(NumberFormatException e) {
                            return "You can only set an int value in range 1-10";
                        }
                    } else if(cmd[2].equalsIgnoreCase("snake_speed_base")) {
                        try {
                            int snakeSpeedBase = Integer.parseInt(cmd[3]);
                            if(snakeSpeedBase > 0) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setSnakeSpeedBase(snakeSpeedBase);
                                return "Updated value snake_speed_base";
                            } else {
                                return "You can only set a positive int value";
                            }
                        } catch(NumberFormatException e) {
                            return "You can only set a positive int value";
                        }
                    } else if(cmd[2].equalsIgnoreCase("snake_speed_modifier_value")) {
                        try {
                            int snakeSpeedModifierValue = Integer.parseInt(cmd[3]);
                            if(snakeSpeedModifierValue >= 0) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setSnakeSpeedBase(snakeSpeedModifierValue);
                                return "Updated value snake_speed_modifier_value";
                            } else {
                                return "You can only set a positive (or 0) int value";
                            }
                        } catch(NumberFormatException e) {
                            return "You can only set a positive (or 0) int value";
                        }
                    } else if(cmd[2].equalsIgnoreCase("snake_speed_modifier_bodycount")) {
                        try {
                            int snakeSpeedModifierBodycount = Integer.parseInt(cmd[3]);
                            if(snakeSpeedModifierBodycount > 0) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setSnakeSpeedModifierBodycount(snakeSpeedModifierBodycount);
                                return "Updated value snake_speed_modifier_bodycount";
                            } else {
                                return "You can only set a positive int value";
                            }
                        } catch(NumberFormatException e) {
                            return "You can only set a positive int value";
                        }
                    } else if(cmd[2].equalsIgnoreCase("default_gamefield_size_x")) {
                        try {
                            int defaultGameFiendSizeX = Integer.parseInt(cmd[3]);
                            if(defaultGameFiendSizeX >= 50) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setSnakeSpeedModifierBodycount(defaultGameFiendSizeX);
                                return "Updated value default_gamefield_size_x";
                            } else {
                                return "You can only set a positive int value (50 or higher)";
                            }
                        } catch(NumberFormatException e) {
                            return "You can only set a positive int value (50 or higher)";
                        }
                    } else if(cmd[2].equalsIgnoreCase("default_gamefield_size_y")) {
                        try {
                            int defaultGameFiendSizeY = Integer.parseInt(cmd[3]);
                            if(defaultGameFiendSizeY >= 50) {
                                SlakeoverflowServer.getServer().getConfigManager().getConfig().setSnakeSpeedModifierBodycount(defaultGameFiendSizeY);
                                return "Updated value default_gamefield_size_y";
                            } else {
                                return "You can only set a positive int value (50 or higher)";
                            }
                        } catch(NumberFormatException e) {
                            return "You can only set a positive int value (50 or higher)";
                        }
                    } else if(cmd[2].equalsIgnoreCase("unauthenticate_player_on_death")) {
                        SlakeoverflowServer.getServer().getConfigManager().getConfig().setUnauthenticatePlayerOnDeath(Boolean.parseBoolean(cmd[3]));
                        return "Updated value unauthenticate_player_on_death to " + cmd[3];
                    } else {
                        return "Unknown config option";
                    }
                } else if(cmd[2].equalsIgnoreCase("enable_advanced_options")) {
                    return "This option can only be enabled with the start argument enableAdvancedConfigOptions";
                } else if(cmd[2].equalsIgnoreCase("advanced_override_server_tickrate") || cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate") || cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate_idle")) {
                    return "This option can't be changed while the server is running";
                } else {
                    return "Run command without arguments for help";
                }
            } else if (cmd[1].equalsIgnoreCase("get")) {
                if(cmd.length == 3) {
                    if(cmd[2].equalsIgnoreCase("port")) {
                        return "Value port: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getPort();
                    } else if(cmd[2].equalsIgnoreCase("slots")) {
                        return "Value slots: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSlots();
                    } else if(cmd[2].equalsIgnoreCase("whitelist")) {
                        return "Value whitelist: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isWhitelist();
                    } else if(cmd[2].equalsIgnoreCase("user_authentication")) {
                        return "Value user_authentication: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isUserAuthentication();
                    } else if(cmd[2].equalsIgnoreCase("min_food_value")) {
                        return "Value min_food_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMinFoodValue();
                    } else if(cmd[2].equalsIgnoreCase("max_food_value")) {
                        return "Value max_food_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMaxFoodValue();
                    } else if(cmd[2].equalsIgnoreCase("default_snake_length")) {
                        return "Value default_snake_length: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultSnakeLength();
                    } else if(cmd[2].equalsIgnoreCase("snake_speed_base")) {
                        return "Value snake_speed_base: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedBase();
                    } else if(cmd[2].equalsIgnoreCase("snake_speed_modifier_value")) {
                        return "Value snake_speed_modifier_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedModifierValue();
                    } else if(cmd[2].equalsIgnoreCase("snake_speed_modifier_bodycount")) {
                        return "Value snake_speed_modifier_bodycount: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedModifierBodycount();
                    } else if(cmd[2].equalsIgnoreCase("enable_advanced_options")) {
                        return "Value enable_advanced_options: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isAdvancedOptionsEnabled();
                    } else if(cmd[2].equalsIgnoreCase("advanced_override_server_tickrate")) {
                        return "Value advanced_override_server_tickrate: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isOverrideServerTickrate();
                    } else if(cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate")) {
                        return "Value advanced_custom_server_tickrate: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getCustomServerTickrate();
                    } else if(cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate_idle")) {
                        return "Value advanced_custom_server_tickrate_idle: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getCustomServerTickrateIdle();
                    } else if(cmd[2].equalsIgnoreCase("default_gamefield_size_x")) {
                        return "Value default_gamefield_size_x: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultGameFieldSizeX();
                    } else if(cmd[2].equalsIgnoreCase("default_gamefield_size_y")) {
                        return "Value default_gamefield_size_y: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultGameFieldSizeY();
                    } else if(cmd[2].equalsIgnoreCase("unauthenticate_player_on_death")) {
                        return "Value unauthenticate_player_on_death: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isUnauthenticatePlayerOnDeath();
                    } else {
                        return "Unknown config option";
                    }
                } else {
                    return "Run command without arguments for help";
                }
            } else if (cmd[1].equalsIgnoreCase("list")) {
                return "ALL CONFIG OPTIONS:\n" +
                        "port: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getPort() + "\n" +
                        "slots: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSlots() + "\n" +
                        "whitelist: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isWhitelist() + "\n" +
                        "user_authentication: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isUserAuthentication() + "\n" +
                        "min_food_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMinFoodValue() + "\n" +
                        "max_food_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMaxFoodValue() + "\n" +
                        "default_snake_length: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultSnakeLength() + "\n" +
                        "snake_speed_base: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedBase() + "\n" +
                        "snake_speed_modifier_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedModifierValue() + "\n" +
                        "snake_speed_modifier_bodycount: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedModifierBodycount() + "\n" +
                        "default_gamefield_size_x: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultGameFieldSizeX() + "\n" +
                        "default_gamefield_size_y: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultGameFieldSizeY() + "\n" +
                        "unauthenticate_player_on_death: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isUnauthenticatePlayerOnDeath() + "\n" +
                        "enable_advanced_options: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isAdvancedOptionsEnabled() + "\n" +
                        "advanced_override_server_tickrate: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().isOverrideServerTickrate() + "\n" +
                        "advanced_custom_server_tickrate: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getCustomServerTickrate() + "\n" +
                        "advanced_custom_server_tickrate_idle: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getCustomServerTickrateIdle() + "\n";
            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "Command usage:\nconfig set <OPTION> <VALUE>\nconfig get <OPTION>\nconfig list\n";
        }
    }

    private static String connectionCommand(String[] cmd) {
        if(cmd.length >= 2) {
            switch(cmd[1]) {
                case "list":
                {
                    String returnString = "CONNECTIONS:\n";
                    returnString = returnString + connectionCommandGetEstablishedConnectionList();
                    returnString = returnString + connectionCommandPendingConnectionList();
                    return returnString;
                }
                case "list-established":
                    return connectionCommandGetEstablishedConnectionList();
                case "list-pending":
                    return connectionCommandPendingConnectionList();
                case "info":
                    if(cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSClient client = SlakeoverflowServer.getServer().getConnectionhandler().getClientById(uuid);

                            if(client != null) {
                                String returnString = "CONNECTION INFO:\n";
                                returnString = returnString + "UUID: " + uuid + "\n";
                                returnString = returnString + "IP: " + client.getIP() + "\n";
                                ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(uuid);
                                if(connection != null) {
                                    returnString = returnString + "SERVERCONNECTION: AVAIL (" + ConnectionType.toString(connection.getConnectionType()) + ")\n";
                                } else {
                                    returnString = returnString + "SERVERCONNECTION: N/A\n";
                                }
                                return returnString;
                            } else {
                                return "The UUID you specified does not exist (you can't use info for pending connections)";
                            }
                        } catch(IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "close":
                    if(cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSClient client = SlakeoverflowServer.getServer().getConnectionhandler().getClientById(uuid);

                            if(client != null) {
                                client.close();
                                return "Connection closed";
                            } else {
                                return "The UUID you specified does not exist (if you want to kick a pending connection, use connection deny instead)";
                            }
                        } catch(IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "accept":
                    if(cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSPendingClient pendingClient = SlakeoverflowServer.getServer().getConnectionhandler().getPendingConnections().get(uuid);

                            if(pendingClient != null) {
                                pendingClient.setState(PendingClientState.ACCEPTED);
                                return "Connection accepted";
                            } else {
                                return "This pending connection does not exist";
                            }
                        } catch(IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "deny":
                    if(cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSPendingClient pendingClient = SlakeoverflowServer.getServer().getConnectionhandler().getPendingConnections().get(uuid);

                            if(pendingClient != null) {
                                pendingClient.setState(PendingClientState.DENIED);
                                return "Connection refused";
                            } else {
                                return "This pending connection does not exist";
                            }
                        } catch(IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                default:
                    return "Run command without arguments for help";
            }
        } else {
            return "CONNECTION COMMAND USAGE:\n" +
                    "connection list\n" +
                    "connection list-established\n" +
                    "connection list-pending\n" +
                    "connection info <UUID>\n" +
                    "connection close <UUID>\n" +
                    "connection accept <UUID>\n" +
                    "connection deny <UUID>\n";
        }
    }

    private static String connectionCommandGetEstablishedConnectionList() {
        String returnString = "ESTABLISHED CONNECTIONS:\n";
        for(UUID uuid : SlakeoverflowServer.getServer().getConnectionhandler().getClients().keySet()) {
            CMSClient cmsClient = SlakeoverflowServer.getServer().getConnectionhandler().getClients().get(uuid);
            returnString = returnString + uuid + " " + cmsClient.getIP() + "\n";
        }
        return returnString;
    }

    private static String connectionCommandPendingConnectionList() {
        String returnString = "PENDING CONNECTIONS:\n";
        for(UUID uuid : SlakeoverflowServer.getServer().getConnectionhandler().getPendingConnections().keySet()) {
            CMSPendingClient pendingClient = SlakeoverflowServer.getServer().getConnectionhandler().getPendingConnections().get(uuid);
            returnString = returnString + uuid + " " + pendingClient.getState() + " " + pendingClient.getSocket().getInetAddress() + "\n";
        }
        return returnString;
    }

    private static String userCommand(String[] cmd) {
        if(cmd.length >= 2) {
            switch(cmd[1]) {
                case "list":
                {
                    String returnString = "USER LIST:\n";
                    for(ServerConnection connection : SlakeoverflowServer.getServer().getConnectionList()) {
                        returnString = returnString + connection.getClientId() + " " + ConnectionType.toString(connection.getConnectionType()) + "\n";
                    }
                    return returnString;
                }
                case "info":
                    if(cmd.length == 3) {
                        try {
                            ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(UUID.fromString(cmd[2]));
                            if(connection != null) {
                                return "USER INFO:\n" +
                                        "UUID: " + connection.getClientId() + "\n" +
                                        "Auth state: " + ConnectionType.toString(connection.getConnectionType()) + "\n";
                            } else {
                                return "This user does not exist";
                            }
                        } catch(IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "auth":
                    if(cmd.length == 4) {
                        try {
                            ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(UUID.fromString(cmd[2]));
                            if(connection != null) {
                                if(cmd[3].equalsIgnoreCase("player")) {
                                    connection.authenticateAsPlayer();
                                    return "User authenticated as player";
                                } else if(cmd[3].equalsIgnoreCase("spectator")) {
                                    connection.authenticateAsSpectator();
                                    return "User authenticated as spectator";
                                } else {
                                    return "Run command without arguments for help";
                                }
                            } else {
                                return "This user does not exist";
                            }
                        } catch(IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "unauth":
                    if(cmd.length == 3) {
                        try {
                            ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(UUID.fromString(cmd[2]));
                            if(connection != null) {
                                connection.unauthenticate();
                                return "Connection unauthenticated";
                            } else {
                                return "This user does not exist";
                            }
                        } catch(IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                default:
                    return "Run command without arguments for help";
            }
        } else {
            return "USER COMMAND USAGE:\n" +
                    "user list\n" +
                    "user info <UUID>\n" +
                    "user auth <UUID> player/spectator\n" +
                    "user unauth <UUID>\n";
        }
    }

    private static String blacklistCommand(String[] cmd) {
        if(cmd.length >= 2) {
            switch(cmd[1]) {
                case "list":
                    String returnString = "BLACKLIST:\n";
                    for(InetAddress ip : SlakeoverflowServer.getServer().getIpBlacklist()) {
                        returnString = returnString + ip.toString() + "\n";
                    }
                    return returnString;
                case "add":
                    if(cmd.length == 3) {
                        try {
                            InetAddress inetAddress = InetAddress.getByName(cmd[2]);
                            SlakeoverflowServer.getServer().addIpToBlacklist(inetAddress);
                            return "Added IP to blacklist";
                        } catch (UnknownHostException e) {
                            return "Please enter a valid IP address";
                        }
                    } else {
                        return "blacklist add <IP>";
                    }
                case "remove":
                    if(cmd.length == 3) {
                        try {
                            InetAddress inetAddress = InetAddress.getByName(cmd[2]);
                            SlakeoverflowServer.getServer().removeIpFromBlacklist(inetAddress);
                            return "Removed IP from blacklist";
                        } catch (UnknownHostException e) {
                            return "Please enter a valid IP address";
                        }
                    } else {
                        return "blacklist add <IP>";
                    }
                case "clear":
                    SlakeoverflowServer.getServer().clearIpBlacklist();
                    return "Cleared IP blacklist";
                default:
                    return "Run command without arguments for help";
            }
        } else {
            return "BLACKLIST COMMAND USAGE:\n" +
                    "blacklist list\n" +
                    "blacklist add <IP>\n" +
                    "blacklist remove <IP>\n" +
                    "blacklist clear\n";
        }
    }

    private static String gameCommand(String[] cmd) {
        if(cmd.length >= 2) {
            if(cmd[1].equalsIgnoreCase("start")) {
                if(cmd.length >= 3) {
                    if(cmd[2].equalsIgnoreCase("default") && cmd.length == 3) {
                        SlakeoverflowServer.getServer().setupGameDefault();
                        return "Set up game with default settings";
                    } else if(cmd[2].equalsIgnoreCase("automatic") && cmd.length == 3) {
                        SlakeoverflowServer.getServer().setupGameAutomatically();
                        return "Set up game with player count based settings";
                    } else if(cmd[2].equalsIgnoreCase("custom") && cmd.length == 5) {
                        try {
                            int sizex = Integer.parseInt(cmd[3]);
                            int sizey = Integer.parseInt(cmd[3]);

                            if(sizex > 10 && sizey > 10) {
                                SlakeoverflowServer.getServer().setupGame(sizex, sizey);
                                return "Set up game with custom settings";
                            } else {
                                return "Both values have to be 10 or more";
                            }
                        } catch(NumberFormatException e) {
                            return "Please specify an int value";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                } else {
                    return "GAME START COMMAND USAGE\n" +
                            "game start default\n" +
                            "game start automatic\n" +
                            "game start custom <sizex> <sizey>\n";
                }
            } else if(cmd[1].equalsIgnoreCase("stop")) {
                if(SlakeoverflowServer.getServer().stopGame()) {
                    return "Stopped game";
                } else {
                    return "No game running";
                }
            } else if(cmd[1].equalsIgnoreCase("pause")) {
                if(SlakeoverflowServer.getServer().pauseGame()) {
                    return "Paused game";
                } else {
                    return "Game is not running";
                }
            } else if(cmd[1].equalsIgnoreCase("resume")) {
                if(SlakeoverflowServer.getServer().resumeGame()) {
                    return "Resumed game";
                } else {
                    return "Game is not paused";
                }
            } else if(cmd[1].equalsIgnoreCase("info")) {
                if(SlakeoverflowServer.getServer().isGameAvail()) {
                    String returnString = "GAME INFORMATION:\n" +
                            "Game state: " + GameState.getString(SlakeoverflowServer.getServer().getGameState()) + "(" + SlakeoverflowServer.getServer().getGameState() + ")" + "\n";
                    try {
                        returnString = returnString + "Players: " + SlakeoverflowServer.getServer().getGameSession().getSnakeList().size() + "\n" +
                                "Items: " + SlakeoverflowServer.getServer().getGameSession().getItemList().size() + "\n" +
                                "World border: x=" + SlakeoverflowServer.getServer().getGameSession().getBorder()[0] + " y=" + SlakeoverflowServer.getServer().getGameSession().getBorder()[1] + "\n";
                    } catch(Exception e) {
                        returnString = returnString + "ERROR: " + Arrays.toString(e.getStackTrace());
                    }
                    return returnString;
                } else {
                    return "Currently is no game running";
                }
            } else if(cmd[1].equalsIgnoreCase("get")) {
                if(SlakeoverflowServer.getServer().isGameAvail()) {
                    if(cmd.length >= 3) {
                        if(cmd[2].equalsIgnoreCase("snakes")) {
                            String returnString = "SNAKES:\n";
                            for(Snake snake : SlakeoverflowServer.getServer().getGameSession().getSnakeList()) {
                                returnString = returnString + snake.getConnection().getClientId() + " x=" + snake.getPosX() + " y=" + snake.getPosY() + " " + " " + snake.getLength() + " " + snake.isAlive();
                            }
                            return returnString;
                        } else if(cmd[2].equalsIgnoreCase("snake") && cmd.length == 4) {
                            try {
                                Snake snake = SlakeoverflowServer.getServer().getGameSession().getSnakeOfConnection(SlakeoverflowServer.getServer().getConnectionByUUID(UUID.fromString(cmd[3])));
                                if(snake != null) {
                                    return "SNAKE INFORMATION:\n" +
                                            "UUID: " + snake.getConnection().getClientId() + "\n" +
                                            "Position: x=" + snake.getPosX() + ", y=" + snake.getPosY() + "\n" +
                                            "Length: " + snake.getLength() + "\n" +
                                            "Facing: " + snake.getFacing() + "\n" +
                                            "Body positions: " + snake.getBodyPositions().toString() + "\n";
                                } else {
                                    return "This snake does not exist";
                                }
                            } catch(IllegalArgumentException e) {
                                return "Please specify a valid UUID";
                            }
                        } else if(cmd[2].equalsIgnoreCase("items")) {
                            String returnString = "ITEMS:\n";
                            List<Item> itemList = SlakeoverflowServer.getServer().getGameSession().getItemList();
                            for(int i = 0; i < itemList.size(); i++) {
                                Item item = itemList.get(i);
                                returnString = returnString + i + " " + item.getDescription() + " " + item.getPosX() + " " + item.getPosY() + " ";
                                if(item instanceof Food) {
                                    returnString = returnString + ((Food) item).getFoodValue() + " ";
                                } else if(item instanceof SuperFood) {
                                    returnString = returnString + ((SuperFood) item).getValue() + " ";
                                }
                                returnString = returnString + "\n";
                            }
                            return returnString;
                        } else if(cmd[2].equalsIgnoreCase("border")) {
                            return "Worldborder: x1=-1, y1=-1, x2= " + SlakeoverflowServer.getServer().getGameSession().getBorder()[0] + ", y2=" + SlakeoverflowServer.getServer().getGameSession().getBorder()[1];
                        } else if(cmd[2].equalsIgnoreCase("fov")) {
                            return "Player FOV: x=" + SlakeoverflowServer.getServer().getGameSession().getPlayerFOV()[0] + ", y=" + SlakeoverflowServer.getServer().getGameSession().getPlayerFOV()[1];
                        } else {
                            return "Value not found";
                        }
                    } else {
                        return "GAME GET COMMAND USAGE:\n" +
                                "game get snakes\n" +
                                "game get snake <connectionUUID>\n" +
                                "game get items\n" +
                                "game get border\n" +
                                "game get fov\n";
                    }
                } else {
                    return "No game available";
                }
            } else if(cmd[1].equalsIgnoreCase("modify")) {
                if(SlakeoverflowServer.getServer().isGameAvail()) {
                    if(cmd.length >= 3) {
                        if(cmd[2].equalsIgnoreCase("snake")) {
                            if(cmd.length >= 4) {
                                try {
                                    Snake snake = SlakeoverflowServer.getServer().getGameSession().getSnakeOfConnection(SlakeoverflowServer.getServer().getConnectionByUUID(UUID.fromString(cmd[3])));

                                    if(snake != null) {

                                        if(cmd.length >= 5) {
                                            if(cmd[4].equalsIgnoreCase("kill")) {
                                                snake.killSnake();
                                                return "Killed snake";
                                            } else if(cmd[4].equalsIgnoreCase("position")) {
                                                if(cmd.length == 7) {
                                                    try {
                                                        snake.setPosition(Integer.parseInt(cmd[5]), Integer.parseInt(cmd[6]));
                                                        return "Updated snake position";
                                                    } catch(IllegalArgumentException e) {
                                                        return "Please specify a valid value";
                                                    }
                                                } else {
                                                    return "USAGE: game modify snake <UUID> position <X> <Y>";
                                                }
                                            } else if(cmd[4].equalsIgnoreCase("bodies")) {
                                                if(cmd.length >= 6) {
                                                    if(cmd[5].equalsIgnoreCase("add")) {
                                                        if(cmd.length == 7) {
                                                            snake.addBody(Integer.parseInt(cmd[6]));
                                                            return "Updated snake length";
                                                        } else {
                                                            return "Value required";
                                                        }
                                                    } else if(cmd[5].equalsIgnoreCase("remove")) {
                                                        if(cmd.length == 7) {
                                                            snake.removeBody(Integer.parseInt(cmd[6]));
                                                            return "Updated snake length";
                                                        } else {
                                                            return "Value required";
                                                        }
                                                    } else if(cmd[5].equalsIgnoreCase("clear")) {
                                                        snake.clearBodies();
                                                        return "Cleared snake length";
                                                    } else {
                                                        return "USAGE: game modify snake <UUID> bodies add/remove/clear <Value for add/remove>";
                                                    }
                                                } else {
                                                    return "USAGE: game modify snake <UUID> bodies add/remove/clear <Value for add/remove>";
                                                }
                                            } else if(cmd[4].equalsIgnoreCase("facing")) {
                                                if(cmd.length == 6) {
                                                    try {
                                                        int value = Integer.parseInt(cmd[5]);
                                                        if(value >= 0 && value <= 3) {
                                                            snake.setNewFacing(value, true);
                                                            return "Updated snake facing";
                                                        } else {
                                                            return "Wrong facing value (0-3)";
                                                        }
                                                    } catch(IllegalArgumentException e) {
                                                        return "Please specify a valid value";
                                                    }
                                                } else {
                                                    return "USAGE: game modify snake <UUID> facing <newFacing>";
                                                }
                                            } else {
                                                return "Unknown action";
                                            }
                                        } else {
                                            return "This snake does exist (you need to specify what you want to do with it)";
                                        }

                                    } else {
                                        return "This snake does not exist";
                                    }
                                } catch(IllegalArgumentException e) {
                                    return "Please specify a valid UUID";
                                }
                            } else {
                                return "UUID required";
                            }
                        } else if(cmd[2].equalsIgnoreCase("item")) {
                            if(cmd.length >= 4) {
                                try {
                                    int itemIndex = Integer.parseInt(cmd[3]);
                                    Item item = SlakeoverflowServer.getServer().getGameSession().getItemList().get(itemIndex);

                                    if(item != null) {

                                        if(cmd.length >= 5) {
                                            if(cmd[4].equalsIgnoreCase("kill")) {
                                                SlakeoverflowServer.getServer().getGameSession().killItem(itemIndex);
                                                return "Killed item";
                                            } else if(cmd[4].equalsIgnoreCase("position")) {
                                                if(cmd.length == 7) {
                                                    try {
                                                        item.setPosition(Integer.parseInt(cmd[5]), Integer.parseInt(cmd[6]));
                                                        return "Updated item position";
                                                    } catch(IllegalArgumentException e) {
                                                        return "Please specify a valid position";
                                                    }
                                                } else {
                                                    return "game modify item <ID> position <X> <Y>";
                                                }
                                            } else {
                                                return "Unknown action";
                                            }
                                        } else {
                                            return "This item does exist (you need to specify what you want to do with it)";
                                        }

                                    } else {
                                        return "This item does not exist";
                                    }
                                } catch(IndexOutOfBoundsException e) {
                                    return "This item does not exist";
                                } catch(IllegalArgumentException e) {
                                    return "Please specify a valid int";
                                }
                            } else {
                                return "Item id required";
                            }
                        } else if(cmd[2].equalsIgnoreCase("items")) {
                            if(cmd.length == 4 && cmd[3].equalsIgnoreCase("kill")) {
                                SlakeoverflowServer.getServer().getGameSession().killItems();
                                return "Killed items";
                            } else {
                                return "Value not found";
                            }
                        } else {
                            return "Value not found";
                        }
                    } else {
                        return "GAME MODIFY COMMAND USAGE\n" +
                                "game modify snake <UUID> kill\n" +
                                "game modify snake <UUID> position <X> <Y>\n" +
                                "game modify snake <UUID> bodies add <length>\n" +
                                "game modify snake <UUID> bodies remove <length>\n" +
                                "game modify snake <UUID> bodies clear\n" +
                                "game modify snake <UUID> facing <N=0,E=1,S=2,W=3>\n" +
                                "game modify item <INDEX> kill\n" +
                                "game modify item <INDEX> position <X> <Y>\n" +
                                "game modify items kill\n";
                    }
                } else {
                    return "No game available";
                }
            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "GAME COMMAND USAGE:\n" +
                    "game start default/automatic\n" +
                    "game start custom <sizex> <sizey>\n" +
                    "game stop\n" +
                    "game pause\n" +
                    "game resume\n" +
                    "game info\n" +
                    "game getvalue <...>\n" +
                    "game modify <...>\n";
        }
    }

    public static String loggerCommand(String[] cmd) {
        if(cmd.length >= 2) {
            if(cmd[1].equalsIgnoreCase("list")) {
                String returnString = "LOG:\n";

                int index = 0;
                for(Object logEntryObject : SlakeoverflowServer.getServer().getLogger().getLog()) {
                    JSONObject logEntry = (JSONObject) logEntryObject;

                    returnString = returnString + index + " " + logEntry.getString("time") + " " + logEntry.getString("type") + " " + logEntry.getString("module") + "\n";
                    index++;
                }
                return returnString;
            } else if(cmd[1].equalsIgnoreCase("get")) {
                try {
                    if(cmd.length == 3) {
                        int index = Integer.parseInt(cmd[2]);
                        JSONObject logEntry = SlakeoverflowServer.getServer().getLogger().getLog().getJSONObject(index);
                        return "LOG ENTRY:\n" +
                                "ID: " + index + "\n" +
                                "Time: " + logEntry.getString("time") + "\n" +
                                "Type: " + logEntry.getString("type") + "\n" +
                                "Module: " + logEntry.getString("module") + "\n" +
                                "Content: " + logEntry.getString("text").replace("\n", "%20") + "\n";
                    } else {
                        return "Please specify a log id";
                    }
                } catch(JSONException e) {
                    return "Log error";
                } catch(IllegalArgumentException e) {
                    return "Please specify a valid int value";
                }
            } else if(cmd[1].equalsIgnoreCase("save")) {
                if(cmd.length == 3) {
                    try {
                        File file = new File(System.getProperty("user.dir"), cmd[2]);
                        if(!file.exists()) {
                            SlakeoverflowServer.getServer().getLogger().saveLog(file, false);
                            return "File was written";
                        } else {
                            return "File does already exist";
                        }
                    } catch(IOException e) {
                        return "IOException. Please check your file";
                    }
                } else {
                    return "Please specify a valid file name";
                }
            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "LOGGER COMMAND USAGE:\n" +
                    "logger list\n" +
                    "logger get <ID>\n" +
                    "logger save\n";
        }
    }
}
