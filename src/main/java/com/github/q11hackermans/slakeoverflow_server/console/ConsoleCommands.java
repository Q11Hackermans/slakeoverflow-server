package com.github.q11hackermans.slakeoverflow_server.console;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.ConnectionType;
import net.jandie1505.connectionmanager.enums.PendingClientState;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.CMSPendingClient;

import java.util.UUID;

public class ConsoleCommands {
    public static String run(String[] cmd) {
        try {

        } catch(Exception e) {
            return "Command error: " + e.getMessage();
        }
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
                default:
                    return "Unknown command";
            }
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
                        "min_food_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMinFoodValue() + "\n" +
                        "max_food_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMaxFoodValue() + "\n" +
                        "default_snake_length: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getDefaultSnakeLength() + "\n" +
                        "snake_speed_base: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedBase() + "\n" +
                        "snake_speed_modifier_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedModifierValue() + "\n" +
                        "snake_speed_modifier_bodycount: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getSnakeSpeedModifierBodycount() + "\n" +
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
                                    connection.authorizeAsPlayer();
                                    return "User authenticated as player";
                                } else if(cmd[3].equalsIgnoreCase("spectator")) {
                                    connection.authorizeAsSpectator();
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
                                connection.deauthorize();
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
}
