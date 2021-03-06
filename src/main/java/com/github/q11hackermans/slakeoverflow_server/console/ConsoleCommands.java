package com.github.q11hackermans.slakeoverflow_server.console;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.AccountPermissionLevel;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import com.github.q11hackermans.slakeoverflow_server.data.SnakeData;
import com.github.q11hackermans.slakeoverflow_server.game.Food;
import com.github.q11hackermans.slakeoverflow_server.game.Item;
import com.github.q11hackermans.slakeoverflow_server.game.Snake;
import com.github.q11hackermans.slakeoverflow_server.game.SuperFood;
import com.github.q11hackermans.slakeoverflow_server.shop.ShopItem;
import net.jandie1505.connectionmanager.enums.PendingClientState;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.CMSPendingClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ConsoleCommands {
    public static String run(SlakeoverflowServer server, String[] cmd) {
        try {
            if (cmd.length >= 1) {
                switch (cmd[0]) {
                    case "stop":
                        server.stop();
                        return "Shutting down";
                    case "help":
                        return helpCommand();
                    case "config":
                        return configCommand(server, cmd);
                    case "connection":
                        return connectionCommand(server, cmd);
                    case "user":
                        return userCommand(server, cmd);
                    case "blacklist":
                        return blacklistCommand(server, cmd);
                    case "game":
                        return gameCommand(server, cmd);
                    case "logger":
                        return loggerCommand(server, cmd);
                    case "info":
                        return infoCommand(server);
                    case "account":
                        return accountCommand(server, cmd);
                    case "shop":
                        return shopCommand(server, cmd);
                    case "chat":
                        return chatCommand(server, cmd);
                    default:
                        return "Unknown command";
                }
            }
        } catch (Exception e) {
            return "Command error: " + e.toString() + ": " + e.getMessage() + " " + Arrays.toString(e.getStackTrace());
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
                "account: User account management (Database)\n" +
                "blacklist: IP blacklist management\n" +
                "game: Game management\n" +
                "logger: Logging management\n" +
                "shop: Item shop management\n" +
                "info: Information" +
                "Run the specific commands without arguments to show their help page.\n";
    }

    private static String configCommand(SlakeoverflowServer server, String[] cmd) {
        if (cmd.length >= 2) {
            if (cmd[1].equalsIgnoreCase("set")) {
                if (cmd.length == 4) {
                    if (cmd[2].equalsIgnoreCase("port")) {
                        return "Cannot set port while the server is running!";
                    } else if (cmd[2].equalsIgnoreCase("auto_connection_accept")) {
                        server.getConfigManager().getConfig().setAutoConnectionAccept(Boolean.parseBoolean(cmd[3]));
                        return "Updated value auto_connection_accept to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("user_authentication")) {
                        server.getConfigManager().getConfig().setUserAuthentication(Boolean.parseBoolean(cmd[3]));
                        return "Updated value user_authentication to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("max_connections")) {
                        try {
                            server.getConfigManager().getConfig().setMaxConnections(Integer.parseInt(cmd[3]));
                            return "Updated value max_connections";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("max_players")) {
                        try {
                            server.getConfigManager().getConfig().setMaxPlayers(Integer.parseInt(cmd[3]));
                            return "Updated value max_connections";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("max_spectators")) {
                        try {
                            server.getConfigManager().getConfig().setMaxSpectators(Integer.parseInt(cmd[3]));
                            return "Updated value max_connections";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("min_food_value")) {
                        try {
                            int minFoodValue = Integer.parseInt(cmd[3]);
                            if (minFoodValue >= 1 && minFoodValue <= 10) {
                                server.getConfigManager().getConfig().setMinFoodValue(minFoodValue);
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
                                server.getConfigManager().getConfig().setMaxFoodValue(maxFoodValue);
                                return "Updated value max_food_value to " + maxFoodValue;
                            } else {
                                return "You can only set an int value in range 1-10";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set an int value in range 1-10";
                        }
                    } else if (cmd[2].equalsIgnoreCase("default_snake_length")) {
                        try {
                            int defaultSnakeLength = Integer.parseInt(cmd[3]);
                            if (defaultSnakeLength >= 1 && defaultSnakeLength <= 10) {
                                server.getConfigManager().getConfig().setDefaultSnakeLength(defaultSnakeLength);
                                return "Updated value default_snake_length to " + defaultSnakeLength;
                            } else {
                                return "You can only set an int value in range 1-10";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set an int value in range 1-10";
                        }
                    } else if (cmd[2].equalsIgnoreCase("snake_speed_base")) {
                        try {
                            int snakeSpeedBase = Integer.parseInt(cmd[3]);
                            if (snakeSpeedBase > 0) {
                                server.getConfigManager().getConfig().setSnakeSpeedBase(snakeSpeedBase);
                                return "Updated value snake_speed_base";
                            } else {
                                return "You can only set a positive int value";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("snake_speed_modifier_value")) {
                        try {
                            int snakeSpeedModifierValue = Integer.parseInt(cmd[3]);
                            if (snakeSpeedModifierValue >= 0) {
                                server.getConfigManager().getConfig().setSnakeSpeedModifierValue(snakeSpeedModifierValue);
                                return "Updated value snake_speed_modifier_value";
                            } else {
                                return "You can only set a positive (or 0) int value";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set a positive (or 0) int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("snake_speed_modifier_bodycount")) {
                        try {
                            int snakeSpeedModifierBodycount = Integer.parseInt(cmd[3]);
                            if (snakeSpeedModifierBodycount > 0) {
                                server.getConfigManager().getConfig().setSnakeSpeedModifierBodycount(snakeSpeedModifierBodycount);
                                return "Updated value snake_speed_modifier_bodycount";
                            } else {
                                return "You can only set a positive int value";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("default_gamefield_size_x")) {
                        try {
                            int defaultGameFiendSizeX = Integer.parseInt(cmd[3]);
                            if (defaultGameFiendSizeX >= 50) {
                                server.getConfigManager().getConfig().setSnakeSpeedModifierBodycount(defaultGameFiendSizeX);
                                return "Updated value default_gamefield_size_x";
                            } else {
                                return "You can only set a positive int value (50 or higher)";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set a positive int value (50 or higher)";
                        }
                    } else if (cmd[2].equalsIgnoreCase("default_gamefield_size_y")) {
                        try {
                            int defaultGameFiendSizeY = Integer.parseInt(cmd[3]);
                            if (defaultGameFiendSizeY >= 50) {
                                server.getConfigManager().getConfig().setSnakeSpeedModifierBodycount(defaultGameFiendSizeY);
                                return "Updated value default_gamefield_size_y";
                            } else {
                                return "You can only set a positive int value (50 or higher)";
                            }
                        } catch (NumberFormatException e) {
                            return "You can only set a positive int value (50 or higher)";
                        }
                    } else if (cmd[2].equalsIgnoreCase("default_item_despawn_time")) {
                        try {
                            server.getConfigManager().getConfig().setItemDefaultDespawnTime(Integer.parseInt(cmd[3]));
                            return "Updated value default_item_despawn_time";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value (50 or higher)";
                        }
                    } else if (cmd[2].equalsIgnoreCase("item_superfood_despawn_time")) {
                        try {
                            server.getConfigManager().getConfig().setItemSuperFoodDespawnTime(Integer.parseInt(cmd[3]));
                            return "Updated value item_superfood_despawn_time";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("unauthenticate_player_on_death")) {
                        server.getConfigManager().getConfig().setUnauthenticatePlayerOnDeath(Boolean.parseBoolean(cmd[3]));
                        return "Updated value unauthenticate_player_on_death to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("print_debug_messages")) {
                        server.getConfigManager().getConfig().setPrintDebugMessages(Boolean.parseBoolean(cmd[3]));
                        return "Updated value print_debug_messages to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("enable_spectator")) {
                        server.getConfigManager().getConfig().setEnableSpectator(Boolean.parseBoolean(cmd[3]));
                        return "Updated value enable_spectator to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("spectator_update_interval")) {
                        try {
                            server.getConfigManager().getConfig().setSpectatorUpdateInterval(Integer.parseInt(cmd[3]));
                            return "Updated value spectator_update_interval";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("enable_snake_speed_boost")) {
                        server.getConfigManager().getConfig().setEnableSnakeSpeedBoost(Boolean.parseBoolean(cmd[3]));
                        return "Updated value enable_snake_speed_boost to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("eat_own_snake")) {
                        server.getConfigManager().getConfig().setEatOwnSnake(Boolean.parseBoolean(cmd[3]));
                        return "Updated value eat_own_snake to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("snake_death_superfood_multiplier")) {
                        try {
                            server.getConfigManager().getConfig().setSnakeDeathSuperfoodMultiplier(Double.parseDouble(cmd[3]));
                            return "Updated value snake_death_superfood_multiplier";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a value between 0.01 and 2";
                        }
                    } else if (cmd[2].equalsIgnoreCase("enable_advanced_options")) {
                        return "This option can only be enabled with the start argument enableAdvancedConfigOptions";
                    } else if (cmd[2].equalsIgnoreCase("advanced_override_server_tickrate") || cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate") || cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate_idle")) {
                        return "This option can't be changed while the server is running";
                    } else if (cmd[2].equalsIgnoreCase("enable_chat")) {
                        server.getConfigManager().getConfig().setEnableChat(Boolean.parseBoolean(cmd[3]));
                        return "Updated value enable_chat to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("allow_guest_chat")) {
                        server.getConfigManager().getConfig().setAllowGuestChat(Boolean.parseBoolean(cmd[3]));
                        return "Updated value allow_guest_chat to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("enable_admin_command")) {
                        server.getConfigManager().getConfig().setEnableAdminCommand(Boolean.parseBoolean(cmd[3]));
                        return "Updated value enable_admin_command to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("print_chat_to_console")) {
                        server.getConfigManager().getConfig().setPrintChatToConsole(Boolean.parseBoolean(cmd[3]));
                        return "Updated value print_chat_to_console to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("print_chat_commands_to_console")) {
                        server.getConfigManager().getConfig().setPrintChatCommandsToConsole(Boolean.parseBoolean(cmd[3]));
                        return "Updated value print_chat_commands_to_console to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("verbose_chat_logs")) {
                        server.getConfigManager().getConfig().setVerboseChatLogs(Boolean.parseBoolean(cmd[3]));
                        return "Updated value verbose_chat_logs to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("allow_login")) {
                        server.getConfigManager().getConfig().setAllowLogin(Boolean.parseBoolean(cmd[3]));
                        return "Updated value allow_login to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("allow_registration")) {
                        server.getConfigManager().getConfig().setAllowRegistration(Boolean.parseBoolean(cmd[3]));
                        return "Updated value allow_registration to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("allow_guests")) {
                        server.getConfigManager().getConfig().setAllowGuests(Boolean.parseBoolean(cmd[3]));
                        return "Updated value allow_guests to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("also_disable_privileged_login")) {
                        server.getConfigManager().getConfig().setAlsoDisablePrivilegedLogin(Boolean.parseBoolean(cmd[3]));
                        return "Updated value also_disable_privileged_login to " + cmd[3];
                    } else if (cmd[2].equalsIgnoreCase("playing_time_coins_reward_time")) {
                        try {
                            server.getConfigManager().getConfig().setPlayingTimeCoinsRewardTime(Integer.parseInt(cmd[3]));
                            return "Updated value playing_time_coins_reward_time";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("playing_time_coins_reward_amount")) {
                        try {
                            server.getConfigManager().getConfig().setPlayingTimeCoinsRewardAmount(Integer.parseInt(cmd[3]));
                            return "Updated value playing_time_coins_reward_amount";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("playing_time_coins_reward_snake_length_increment")) {
                        try {
                            server.getConfigManager().getConfig().setPlayingTimeCoinsRewardSnakeLengthIncrement(Integer.parseInt(cmd[3]));
                            return "Updated value playing_time_coins_reward_snake_length_increment";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("food_coins_reward_amount")) {
                        try {
                            server.getConfigManager().getConfig().setFoodCoinsRewardAmount(Integer.parseInt(cmd[3]));
                            return "Updated value food_coins_reward_amount";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("food_coins_reward_food_value_increment")) {
                        try {
                            server.getConfigManager().getConfig().setFoodCoinsRewardFoodValueIncrement(Integer.parseInt(cmd[3]));
                            return "Updated value food_coins_reward_food_value_increment";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("superfood_coins_reward_amount")) {
                        try {
                            server.getConfigManager().getConfig().setSuperFoodCoinsRewardAmount(Integer.parseInt(cmd[3]));
                            return "Updated value superfood_coins_reward_amount";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("superfood_coins_reward_food_value_increment")) {
                        try {
                            server.getConfigManager().getConfig().setSuperFoodCoinsRewardFoodValueIncrement(Integer.parseInt(cmd[3]));
                            return "Updated value superfood_coins_reward_food_value_increment";
                        } catch (IllegalArgumentException e) {
                            return "You can only set a positive int value";
                        }
                    } else {
                        return "Unknown config option";
                    }
                } else {
                    return "Run command without arguments for help";
                }
            } else if (cmd[1].equalsIgnoreCase("get")) {
                return "Command not supported anymore. Use config list instead.";
                /*
                if (cmd.length == 3) {
                    if (cmd[2].equalsIgnoreCase("port")) {
                        return "Value port: " + server.getConfigManager().getConfig().getPort();
                    } else if (cmd[2].equalsIgnoreCase("max_connections")) {
                        return "Value max_connections: " + server.getConfigManager().getConfig().getMaxConnections();
                    } else if (cmd[2].equalsIgnoreCase("max_players")) {
                        return "Value max_connections: " + server.getConfigManager().getConfig().getMaxPlayers();
                    } else if (cmd[2].equalsIgnoreCase("max_spectators")) {
                        return "Value max_connections: " + server.getConfigManager().getConfig().getMaxSpectators();
                    } else if (cmd[2].equalsIgnoreCase("auto_connection_accept")) {
                        return "Value auto_connection_accept: " + server.getConfigManager().getConfig().isAutoConnectionAccept();
                    } else if (cmd[2].equalsIgnoreCase("user_authentication")) {
                        return "Value user_authentication: " + server.getConfigManager().getConfig().isUserAuthentication();
                    } else if (cmd[2].equalsIgnoreCase("min_food_value")) {
                        return "Value min_food_value: " + server.getConfigManager().getConfig().getMinFoodValue();
                    } else if (cmd[2].equalsIgnoreCase("max_food_value")) {
                        return "Value max_food_value: " + server.getConfigManager().getConfig().getMaxFoodValue();
                    } else if (cmd[2].equalsIgnoreCase("default_snake_length")) {
                        return "Value default_snake_length: " + server.getConfigManager().getConfig().getDefaultSnakeLength();
                    } else if (cmd[2].equalsIgnoreCase("snake_speed_base")) {
                        return "Value snake_speed_base: " + server.getConfigManager().getConfig().getSnakeSpeedBase();
                    } else if (cmd[2].equalsIgnoreCase("snake_speed_modifier_value")) {
                        return "Value snake_speed_modifier_value: " + server.getConfigManager().getConfig().getSnakeSpeedModifierValue();
                    } else if (cmd[2].equalsIgnoreCase("snake_speed_modifier_bodycount")) {
                        return "Value snake_speed_modifier_bodycount: " + server.getConfigManager().getConfig().getSnakeSpeedModifierBodycount();
                    } else if (cmd[2].equalsIgnoreCase("enable_spectator")) {
                        return "Value enable_spectator: " + server.getConfigManager().getConfig().isEnableSpectator();
                    } else if (cmd[2].equalsIgnoreCase("spectator_update_interval")) {
                        return "Value spectator_update_interval: " + server.getConfigManager().getConfig().getSpectatorUpdateInterval();
                    } else if (cmd[2].equalsIgnoreCase("enable_advanced_options")) {
                        return "Value enable_advanced_options: " + server.getConfigManager().getConfig().isAdvancedOptionsEnabled();
                    } else if (cmd[2].equalsIgnoreCase("advanced_override_server_tickrate")) {
                        return "Value advanced_override_server_tickrate: " + server.getConfigManager().getConfig().isOverrideServerTickrate();
                    } else if (cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate")) {
                        return "Value advanced_custom_server_tickrate: " + server.getConfigManager().getConfig().getCustomServerTickrate();
                    } else if (cmd[2].equalsIgnoreCase("advanced_custom_server_tickrate_idle")) {
                        return "Value advanced_custom_server_tickrate_idle: " + server.getConfigManager().getConfig().getCustomServerTickrateIdle();
                    } else if (cmd[2].equalsIgnoreCase("default_gamefield_size_x")) {
                        return "Value default_gamefield_size_x: " + server.getConfigManager().getConfig().getDefaultGameFieldSizeX();
                    } else if (cmd[2].equalsIgnoreCase("default_gamefield_size_y")) {
                        return "Value default_gamefield_size_y: " + server.getConfigManager().getConfig().getDefaultGameFieldSizeY();
                    } else if (cmd[2].equalsIgnoreCase("unauthenticate_player_on_death")) {
                        return "Value unauthenticate_player_on_death: " + server.getConfigManager().getConfig().isUnauthenticatePlayerOnDeath();
                    } else if (cmd[2].equalsIgnoreCase("print_debug_messages")) {
                        return "Value print_debug_messages: " + server.getConfigManager().getConfig().isPrintDebugMessages();
                    } else if (cmd[2].equalsIgnoreCase("default_item_despawn_time")) {
                        return "Value default_item_despawn_time: " + server.getConfigManager().getConfig().getItemDefaultDespawnTime();
                    } else if (cmd[2].equalsIgnoreCase("item_superfood_despawn_time")) {
                        return "Value item_superfood_despawn_time: " + server.getConfigManager().getConfig().getItemSuperFoodDespawnTime();
                    } else if (cmd[2].equalsIgnoreCase("enable_snake_speed_boost")) {
                        return "Value enable_snake_speed_boost: " + server.getConfigManager().getConfig().isEnableSnakeSpeedBoost();
                    } else if (cmd[2].equalsIgnoreCase("eat_own_snake")) {
                        return "Value eat_own_snake: " + server.getConfigManager().getConfig().isEatOwnSnake();
                    } else if (cmd[2].equalsIgnoreCase("eat_own_snake")) {
                        return "Value snake_death_superfood_multiplier: " + server.getConfigManager().getConfig().getSnakeDeathSuperfoodMultiplier();
                    } else {
                        return "Unknown config option";
                    }
                } else {
                    return "Run command without arguments for help";
                }

                 */
            } else if (cmd[1].equalsIgnoreCase("list")) {
                return "CONFIG OPTIONS:\n" +
                        "Server Settings:\n" +
                        "port: " + server.getConfigManager().getConfig().getPort() + "\n" +
                        "max_connections: " + server.getConfigManager().getConfig().getMaxConnections() + "\n" +
                        "auto_connection_accept: " + server.getConfigManager().getConfig().isAutoConnectionAccept() + "\n" +
                        "user_authentication: " + server.getConfigManager().getConfig().isUserAuthentication() + "\n" +
                        "unauthenticate_player_on_death: " + server.getConfigManager().getConfig().isUnauthenticatePlayerOnDeath() + "\n" +
                        "print_debug_messages: " + server.getConfigManager().getConfig().isPrintDebugMessages() + "\n" +
                        "allow_login: " + server.getConfigManager().getConfig().isAllowLogin() + "\n" +
                        "allow_registration: " + server.getConfigManager().getConfig().isAllowRegistration() + "\n" +
                        "allow_guests: " + server.getConfigManager().getConfig().isAllowGuests() + "\n" +
                        "also_disable_privileged_login: " + server.getConfigManager().getConfig().isAlsoDisablePrivilegedLogin() + "\n" +
                        "enable_chat: " + server.getConfigManager().getConfig().isEnableChat() + "\n" +
                        "allow_guest_chat: " + server.getConfigManager().getConfig().isAllowGuestChat() + "\n" +
                        "enable_admin_command: " + server.getConfigManager().getConfig().isEnableAdminCommand() + "\n" +
                        "print_chat_to_console: " + server.getConfigManager().getConfig().isPrintChatToConsole() + "\n" +
                        "print_chat_commands_to_console: " + server.getConfigManager().getConfig().isPrintChatCommandsToConsole() + "\n" +
                        "verbose_chat_logs: " + server.getConfigManager().getConfig().isVerboseChatLogs() + "\n" +
                        "Game Settings:\n" +
                        "max_players: " + server.getConfigManager().getConfig().getMaxPlayers() + "\n" +
                        "max_spectators: " + server.getConfigManager().getConfig().getMaxSpectators() + "\n" +
                        "min_food_value: " + server.getConfigManager().getConfig().getMinFoodValue() + "\n" +
                        "max_food_value: " + server.getConfigManager().getConfig().getMaxFoodValue() + "\n" +
                        "default_snake_length: " + server.getConfigManager().getConfig().getDefaultSnakeLength() + "\n" +
                        "snake_speed_base: " + server.getConfigManager().getConfig().getSnakeSpeedBase() + "\n" +
                        "snake_speed_modifier_value: " + server.getConfigManager().getConfig().getSnakeSpeedModifierValue() + "\n" +
                        "snake_speed_modifier_bodycount: " + server.getConfigManager().getConfig().getSnakeSpeedModifierBodycount() + "\n" +
                        "default_gamefield_size_x: " + server.getConfigManager().getConfig().getDefaultGameFieldSizeX() + "\n" +
                        "default_gamefield_size_y: " + server.getConfigManager().getConfig().getDefaultGameFieldSizeY() + "\n" +
                        "default_item_despawn_time: " + server.getConfigManager().getConfig().getItemDefaultDespawnTime() + "\n" +
                        "item_superfood_despawn_time: " + server.getConfigManager().getConfig().getItemSuperFoodDespawnTime() + "\n" +
                        "enable_spectator: " + server.getConfigManager().getConfig().isEnableSpectator() + "\n" +
                        "spectator_update_interval: " + server.getConfigManager().getConfig().getSpectatorUpdateInterval() + "\n" +
                        "enable_snake_speed_boost: " + server.getConfigManager().getConfig().isEnableSnakeSpeedBoost() + "\n" +
                        "eat_own_snake: " + server.getConfigManager().getConfig().isEatOwnSnake() + "\n" +
                        "snake_death_superfood_multiplier: " + server.getConfigManager().getConfig().getSnakeDeathSuperfoodMultiplier() + "\n" +
                        "playing_time_coins_reward_time: " + server.getConfigManager().getConfig().getPlayingTimeCoinsRewardTime() + "\n" +
                        "playing_time_coins_reward_amount: " + server.getConfigManager().getConfig().getPlayingTimeCoinsRewardAmount() + "\n" +
                        "playing_time_coins_reward_snake_length_increment: " + server.getConfigManager().getConfig().getPlayingTimeCoinsRewardSnakeLengthIncrement() + "\n" +
                        "food_coins_reward_amount: " + server.getConfigManager().getConfig().getFoodCoinsRewardAmount() + "\n" +
                        "food_coins_reward_food_value_increment: " + server.getConfigManager().getConfig().getFoodCoinsRewardFoodValueIncrement() + "\n" +
                        "superfood_coins_reward_amount: " + server.getConfigManager().getConfig().getSuperFoodCoinsRewardAmount() + "\n" +
                        "superfood_coins_reward_food_value_increment: " + server.getConfigManager().getConfig().getSuperFoodCoinsRewardFoodValueIncrement() + "\n" +
                        "Advanced Settings:\n" +
                        "enable_advanced_options: " + server.getConfigManager().getConfig().isAdvancedOptionsEnabled() + "\n" +
                        "advanced_override_server_tickrate: " + server.getConfigManager().getConfig().isOverrideServerTickrate() + "\n" +
                        "advanced_custom_server_tickrate: " + server.getConfigManager().getConfig().getCustomServerTickrate() + "\n" +
                        "advanced_custom_server_tickrate_idle: " + server.getConfigManager().getConfig().getCustomServerTickrateIdle() + "\n";
            } else if(cmd[1].equalsIgnoreCase("load") || cmd[1].equalsIgnoreCase("reload")) {
                server.getConfigManager().reloadConfig();
                return "Sent config load command";
            } else if(cmd[1].equalsIgnoreCase("write")) {
                server.getConfigManager().recreateConfig();
                return "Sent write config command";
            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "Command usage:\n" +
                    "config set <OPTION> <VALUE>\n" +
                    "config get <OPTION>\n" +
                    "config list\n" +
                    "config load/reload\n" +
                    "config write\n";
        }
    }

    private static String connectionCommand(SlakeoverflowServer server, String[] cmd) {
        if (cmd.length >= 2) {
            switch (cmd[1]) {
                case "list": {
                    String returnString = "CONNECTIONS:\n";
                    returnString = returnString + connectionCommandGetEstablishedConnectionList(server);
                    returnString = returnString + connectionCommandPendingConnectionList(server);
                    return returnString;
                }
                case "list-established":
                    return connectionCommandGetEstablishedConnectionList(server);
                case "list-pending":
                    return connectionCommandPendingConnectionList(server);
                case "info":
                    if (cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSClient client = server.getConnectionhandler().getClientById(uuid);

                            if (client != null) {
                                String returnString = "CONNECTION INFO:\n";
                                returnString = returnString + "UUID: " + uuid + "\n";
                                returnString = returnString + "IP: " + client.getIP() + "\n";
                                ServerConnection connection = server.getConnectionByUUID(uuid);
                                if (connection != null) {
                                    returnString = returnString + "SERVERCONNECTION: AVAIL (" + AuthenticationState.toString(connection.getAuthenticationState()) + ")\n";
                                } else {
                                    returnString = returnString + "SERVERCONNECTION: N/A\n";
                                }
                                return returnString;
                            } else {
                                return "The UUID you specified does not exist (you can't use info for pending connections)";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "close":
                    if (cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSClient client = server.getConnectionhandler().getClientById(uuid);

                            if (client != null) {
                                client.close();
                                return "Connection closed";
                            } else {
                                return "The UUID you specified does not exist (if you want to kick a pending connection, use connection deny instead)";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "accept":
                    if (cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSPendingClient pendingClient = server.getConnectionhandler().getPendingConnections().get(uuid);

                            if (pendingClient != null) {
                                pendingClient.setState(PendingClientState.ACCEPTED);
                                return "Connection accepted";
                            } else {
                                return "This pending connection does not exist";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "deny":
                    if (cmd.length == 3) {
                        try {
                            UUID uuid = UUID.fromString(cmd[2]);
                            CMSPendingClient pendingClient = server.getConnectionhandler().getPendingConnections().get(uuid);

                            if (pendingClient != null) {
                                pendingClient.setState(PendingClientState.DENIED);
                                return "Connection refused";
                            } else {
                                return "This pending connection does not exist";
                            }
                        } catch (IllegalArgumentException e) {
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

    private static String connectionCommandGetEstablishedConnectionList(SlakeoverflowServer server) {
        String returnString = "ESTABLISHED CONNECTIONS:\n";
        for (UUID uuid : server.getConnectionhandler().getClients().keySet()) {
            CMSClient cmsClient = server.getConnectionhandler().getClients().get(uuid);
            returnString = returnString + uuid + " " + cmsClient.getIP() + "\n";
        }
        return returnString;
    }

    private static String connectionCommandPendingConnectionList(SlakeoverflowServer server) {
        String returnString = "PENDING CONNECTIONS:\n";
        for (UUID uuid : server.getConnectionhandler().getPendingConnections().keySet()) {
            CMSPendingClient pendingClient = server.getConnectionhandler().getPendingConnections().get(uuid);
            returnString = returnString + uuid + " " + pendingClient.getState() + " " + pendingClient.getSocket().getInetAddress() + "\n";
        }
        return returnString;
    }

    private static String userCommand(SlakeoverflowServer server, String[] cmd) {
        if (cmd.length >= 2) {
            switch (cmd[1]) {
                case "list": {
                    String returnString = "USER LIST (UUID, AUTH STATE, ACCOUNT ID, MUTED, BANNED):\n";
                    for (ServerConnection connection : server.getConnectionList()) {
                        returnString = returnString + connection.getClientId() + " " + AuthenticationState.toString(connection.getAuthenticationState()) + " " + connection.getAccountId() + " " + connection.isMuted() + " " + connection.isBanned() + "\n";
                    }
                    return returnString;
                }
                case "info":
                    if (cmd.length == 3) {
                        try {
                            ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));

                            if (connection != null) {

                                String accountString;
                                if(connection.getAccount() != null) {
                                    accountString = "LOGGED IN (" + connection.getAccount().getId() + ", " + connection.getAccount().getUsername() + ")";
                                } else {
                                    accountString = "LOGGED OUT";
                                }

                                String mutedString;
                                if(connection.isMuted() && connection.isConnectionMuted()) {
                                    mutedString = "true (CONNECTION)";
                                } else if(connection.isMuted() && !connection.isConnectionMuted()) {
                                    mutedString = "true (ACCOUNT)";
                                } else {
                                    mutedString = "false";
                                }

                                String bannedString;
                                if(connection.isBanned() && connection.isConnectionBanned()) {
                                    bannedString = "true (CONNECTION)";
                                } else if(connection.isBanned() && !connection.isConnectionBanned()) {
                                    bannedString = "true (ACCOUNT)";
                                } else {
                                    bannedString = "false";
                                }

                                return "USER INFO:\n" +
                                        "UUID: " + connection.getClientId() + "\n" +
                                        "Auth state: " + AuthenticationState.toString(connection.getAuthenticationState()) + "\n" +
                                        "Account: " + accountString + "\n" +
                                        "Muted: " + mutedString + "\n" +
                                        "Banned: " + bannedString + "\n" +
                                        "Social Spy: " + connection.isSocialSpy() + "\n";
                            } else {
                                return "This user does not exist";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "auth":
                    if (cmd.length == 4) {
                        try {
                            ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));
                            if (connection != null) {
                                if (cmd[3].equalsIgnoreCase("player")) {
                                    connection.authenticateAsPlayer();
                                    return "User authenticated as player";
                                } else if (cmd[3].equalsIgnoreCase("spectator")) {
                                    connection.authenticateAsSpectator();
                                    return "User authenticated as spectator";
                                } else {
                                    return "Run command without arguments for help";
                                }
                            } else {
                                return "This user does not exist";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "unauth":
                    if (cmd.length == 3) {
                        try {
                            ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));
                            if (connection != null) {
                                connection.unauthenticate();
                                return "Connection unauthenticated";
                            } else {
                                return "This user does not exist";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                case "login":
                    if(cmd.length == 4) {
                        try {
                            ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));
                            if (connection != null) {
                                AccountData account = server.getAccountSystem().getAccount(Long.parseLong(cmd[3]));

                                if(account != null) {
                                    connection.login(account.getId());
                                    return "Logged in user " + connection.getClientId() + " into account " + account.getUsername() + " (" + account.getId() + ")";
                                } else {
                                    return "Account does not exist";
                                }
                            } else {
                                return "This user does not exist";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "Usage: user login <UUID> <AccountID>";
                    }
                case "logout":
                    try {
                        ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));
                        if (connection != null) {
                            connection.logout();
                            return "Logged out user " + connection.getClientId();
                        } else {
                            return "This user does not exist";
                        }
                    } catch (IllegalArgumentException e) {
                        return "Please enter a valid UUID";
                    }
                case "ban":
                    if(cmd.length == 4) {
                        try {
                            ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));

                            if(connection != null) {
                                connection.setBanned(Boolean.parseBoolean(cmd[3]));
                                return "Set banned for connection " + connection.getClientId() + " to " + Boolean.parseBoolean(cmd[3]);
                            } else {
                                return "Invalid user";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "USAGE: user ban <UUID> true/false";
                    }
                case "mute":
                    if(cmd.length == 4) {
                        try {
                            ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));

                            if(connection != null) {
                                connection.setMuted(Boolean.parseBoolean(cmd[3]));
                                return "Set muted for connection " + connection.getClientId() + " to " + Boolean.parseBoolean(cmd[3]);
                            } else {
                                return "Invalid user";
                            }

                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "USAGE: user mute <UUID> true/false";
                    }
                case "socialspy":
                    if(cmd.length == 4) {
                        try {
                            ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));

                            if(connection != null) {
                                connection.setSocialSpy(Boolean.parseBoolean(cmd[3]));
                                return "Set socialspy for connection " + connection.getClientId() + " to " + Boolean.parseBoolean(cmd[3]);
                            } else {
                                return "Invalid user";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please enter a valid UUID";
                        }
                    } else {
                        return "USAGE: user socialspy <UUID> true/false";
                    }
                default:
                    return "Run command without arguments for help";
            }
        } else {
            return "USER COMMAND USAGE:\n" +
                    "user list\n" +
                    "user info <UUID>\n" +
                    "user auth <UUID> player/spectator\n" +
                    "user unauth <UUID>\n" +
                    "user login <UUID> <AccountID>\n" +
                    "user logout <UUID>\n" +
                    "user ban <UUID> true/false\n" +
                    "user mute <UUID> true/false\n" +
                    "user socialspy <UUID> true/false\n";
        }
    }

    private static String blacklistCommand(SlakeoverflowServer server, String[] cmd) {
        if (cmd.length >= 2) {
            switch (cmd[1]) {
                case "list":
                    String returnString = "BLACKLIST:\n";
                    for (InetAddress ip : server.getIpBlacklist()) {
                        returnString = returnString + ip.toString() + "\n";
                    }
                    return returnString;
                case "add":
                    if (cmd.length == 3) {
                        try {
                            InetAddress inetAddress = InetAddress.getByName(cmd[2]);
                            server.addIpToBlacklist(inetAddress);
                            return "Added IP to blacklist";
                        } catch (UnknownHostException e) {
                            return "Please enter a valid IP address";
                        }
                    } else {
                        return "blacklist add <IP>";
                    }
                case "remove":
                    if (cmd.length == 3) {
                        try {
                            InetAddress inetAddress = InetAddress.getByName(cmd[2]);
                            server.removeIpFromBlacklist(inetAddress);
                            return "Removed IP from blacklist";
                        } catch (UnknownHostException e) {
                            return "Please enter a valid IP address";
                        }
                    } else {
                        return "blacklist add <IP>";
                    }
                case "clear":
                    server.clearIpBlacklist();
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

    private static String gameCommand(SlakeoverflowServer server, String[] cmd) {
        if (cmd.length >= 2) {
            if (cmd[1].equalsIgnoreCase("start")) {
                if (cmd.length >= 3) {
                    if (cmd[2].equalsIgnoreCase("default") && cmd.length == 3) {
                        server.setupGameDefault();
                        return "Set up game with default settings";
                    } else if (cmd[2].equalsIgnoreCase("automatic") && cmd.length == 3) {
                        server.setupGameAutomatically();
                        return "Set up game with player count based settings";
                    } else if (cmd[2].equalsIgnoreCase("custom") && (cmd.length == 5 || cmd.length == 6)) {
                        boolean paused;
                        if (cmd.length == 6 && cmd[5].equalsIgnoreCase("paused")) {
                            paused = true;
                        } else {
                            paused = false;
                        }

                        try {
                            int sizex = Integer.parseInt(cmd[3]);
                            int sizey = Integer.parseInt(cmd[3]);

                            if (sizex > 10 && sizey > 10) {
                                server.setupGame(sizex, sizey, paused);
                                return "Set up game with custom settings";
                            } else {
                                return "Both values have to be 10 or more";
                            }
                        } catch (NumberFormatException e) {
                            return "Please specify an int value";
                        }
                    } else if (cmd[2].equalsIgnoreCase("savegame") && cmd.length > 3) {
                        String savegameString = "";
                        for (int i = 3; i < cmd.length; i++) {
                            savegameString = savegameString + cmd[i];
                        }

                        System.out.println(savegameString);

                        try {
                            JSONObject savegame = new JSONObject(savegameString);
                            JSONObject settings = savegame.getJSONObject("settings");
                            JSONArray snakes = savegame.getJSONArray("snakes");
                            JSONArray items = savegame.getJSONArray("items");

                            List<SnakeData> snakeDataList = new ArrayList<>();
                            List<Item> itemList = new ArrayList<>();

                            for (Object snakeObject : snakes) {
                                JSONObject snakeJSON = (JSONObject) snakeObject;
                                List<int[]> bodyPositions = new ArrayList<>();

                                for (Object bodyPositionJSONArrayObject : snakeJSON.getJSONArray("bodies")) {
                                    JSONArray bodyPosition = (JSONArray) bodyPositionJSONArrayObject;
                                    bodyPositions.add(new int[]{bodyPosition.getInt(0), bodyPosition.getInt(1)});
                                }

                                snakeDataList.add(new SnakeData(null, snakeJSON.getInt("x"), snakeJSON.getInt("y"), snakeJSON.getInt("facing"), bodyPositions, snakeJSON.getInt("movein")));
                            }

                            for (Object itemObject : items) {
                                JSONObject item = (JSONObject) itemObject;

                                if (item.getString("description").equalsIgnoreCase("FOOD")) {
                                    itemList.add(new Food(server.getGameSession(), item.getInt("x"), item.getInt("y"), item.getInt("food_value")));
                                } else if (item.getString("description").equalsIgnoreCase("SUPERFOOD")) {
                                    itemList.add(new Food(server.getGameSession(), item.getInt("x"), item.getInt("y"), item.getInt("superfood_value")));
                                } else {
                                    itemList.add(new Item(server.getGameSession(), item.getInt("x"), item.getInt("y")) {
                                        @Override
                                        public String getDescription() {
                                            return "UNKNOWN_ITEM_FROM_SAVEGAME";
                                        }
                                    });
                                }
                            }

                            if (server.setupGame(true, settings.getInt("border_x"), settings.getInt("border_y"), settings.getInt("fovsize_x"), settings.getInt("fovsize_y"), settings.getInt("next_item_despawn"), snakeDataList, itemList)) {
                                return "Game was setup from savegame data. You can resume it with game resume (you need to assign new connections to the snakes before to prevent them to be deleted before you start).";
                            } else {
                                return "Game was not setup";
                            }
                        } catch (JSONException | ClassCastException | IndexOutOfBoundsException e) {
                            return "JSON string corrupt";
                        }
                    } else {
                        return "Run command without arguments for help";
                    }
                } else {
                    return "GAME START COMMAND USAGE\n" +
                            "game start default\n" +
                            "game start automatic\n" +
                            "game start custom <sizex> <sizey>\n" +
                            "game start savegame <SAVEGAME DATA>\n";
                }
            } else if (cmd[1].equalsIgnoreCase("stop")) {
                if (server.stopGame()) {
                    return "Stopped game";
                } else {
                    return "No game running";
                }
            } else if (cmd[1].equalsIgnoreCase("pause")) {
                if (server.pauseGame()) {
                    return "Paused game";
                } else {
                    return "Game is not running";
                }
            } else if (cmd[1].equalsIgnoreCase("resume")) {
                if (server.resumeGame()) {
                    return "Resumed game";
                } else {
                    return "Game is not paused";
                }
            } else if (cmd[1].equalsIgnoreCase("info")) {
                if (server.isGameAvail()) {
                    String returnString = "GAME INFORMATION:\n" +
                            "Game state: " + GameState.getString(server.getGameState()) + " (" + server.getGameState() + "), MTICKS: " + server.getManualTicks() + "\n";
                    try {
                        returnString = returnString + "Players: " + server.getGameSession().getSnakeList().size() + "\n" +
                                "Items: " + server.getGameSession().getItemList().size() + "\n" +
                                "World border: x=" + server.getGameSession().getBorder()[0] + " y=" + server.getGameSession().getBorder()[1] + "\n";
                    } catch (Exception e) {
                        returnString = returnString + "ERROR: " + Arrays.toString(e.getStackTrace());
                    }
                    return returnString;
                } else {
                    return "Currently is no game running";
                }
            } else if (cmd[1].equalsIgnoreCase("getsave")) {
                if (server.isGameAvail()) {
                    return "SAVE STRING: " + server.getGameSession().getSaveString().toString() + "\n";
                } else {
                    return "Currently is no game running";
                }
            } else if (cmd[1].equalsIgnoreCase("mcontrol")) {
                if (server.isGameAvail()) {
                    if (cmd.length >= 3) {
                        if (cmd[2].equalsIgnoreCase("runtick")) {
                            server.addManualTicks(1);
                            return "Running 1 tick";
                        } else if (cmd[2].equalsIgnoreCase("runticks")) {
                            if (cmd.length == 4) {
                                try {
                                    server.addManualTicks(Integer.parseInt(cmd[3]));
                                    return "Running " + cmd[3] + " ticks";
                                } catch (NumberFormatException e) {
                                    return "Please specify a valid int value";
                                }
                            } else {
                                return "Usage: mcontrol runticks <count> - Run a specific amount of ticks";
                            }
                        } else if (cmd[2].equalsIgnoreCase("stop") || cmd[2].equalsIgnoreCase("clear") || cmd[2].equalsIgnoreCase("remove")) {
                            server.resetManualTicks();
                            return "Cleared manual ticks";
                        } else {
                            return "Run command without arguments for help";
                        }
                    } else {
                        return "MANUAL GAME CONTROL (MCONTROL) COMMAND USAGE:\n" +
                                "game mcontrol runtick\n" +
                                "game mcontrol runticks <count>\n" +
                                "game mcontrol stop/clear/remove\n";
                    }
                } else {
                    return "Currently is no game running";
                }
            } else if (cmd[1].equalsIgnoreCase("get")) {
                if (server.isGameAvail()) {
                    if (cmd.length >= 3) {
                        if (cmd[2].equalsIgnoreCase("snakes")) {
                            String returnString = "SNAKES (ID UUID X Y LENGTH FREEZED ALIVE):\n";
                            List<Snake> snakeList = server.getGameSession().getSnakeList();
                            for (int i = 0; i < snakeList.size(); i++) {
                                Snake snake = snakeList.get(i);
                                if (snake.getConnection() != null) {
                                    returnString = returnString + i + " " + snake.getConnection().getClientId() + " x=" + snake.getPosX() + " y=" + snake.getPosY() + " " + snake.getLength() + " " + snake.isFreezed() + " " + snake.isAlive() + "\n";
                                } else {
                                    returnString = returnString + i + " " + "NULL" + " x=" + snake.getPosX() + " y=" + snake.getPosY() + " " + snake.getLength() + " " + snake.isFreezed() + " " + snake.isAlive() + "\n";
                                }
                            }
                            return returnString;
                        } else if (cmd[2].equalsIgnoreCase("snake") && cmd.length == 4) {
                            Snake snake;
                            try {
                                snake = server.getGameSession().getSnakeOfConnection(server.getConnectionByUUID(UUID.fromString(cmd[3])));
                            } catch (IllegalArgumentException e) {
                                try {
                                    snake = server.getGameSession().getSnake(Integer.parseInt(cmd[3]));
                                } catch (IllegalArgumentException e2) {
                                    return "Please specify a valid connection UUID or snake id";
                                }
                            }
                            if (snake != null) {
                                String bodyPositionString = "";
                                for (int[] bodies : snake.getBodyPositions()) {
                                    bodyPositionString = bodyPositionString + Arrays.toString(bodies) + " ";
                                }

                                String uuid = "NULL";
                                if (snake.getConnection() != null) {
                                    uuid = String.valueOf(snake.getConnection().getClientId());
                                }

                                String playerdataSystem;
                                if(snake.isFixedFovPlayerdataSystem()) {
                                    playerdataSystem = "FIXED_FOV (=true)";
                                } else {
                                    playerdataSystem = "SCROLLING_FOV (=false)";
                                }

                                return "SNAKE INFORMATION:\n" +
                                        "ID: " + server.getGameSession().getSnakeId(snake) + "\n" +
                                        "UUID: " + uuid + "\n" +
                                        "Position: x=" + snake.getPosX() + ", y=" + snake.getPosY() + "\n" +
                                        "Length: " + snake.getLength() + "\n" +
                                        "Facing: " + snake.getFacing() + "\n" +
                                        "Freezed: " + snake.isFreezed() + "\n" +
                                        "Move in: " + snake.getMoveIn() + " (" + snake.calcMoveIn() + ")\n" +
                                        "Body positions: " + bodyPositionString + "\n" +
                                        "FOV Behavior: " + playerdataSystem + "\n";
                            } else {
                                return "This snake does not exist";
                            }
                        } else if (cmd[2].equalsIgnoreCase("items")) {
                            String returnString = "ITEMS (ID X Y DESPAWN OTHER):\n";
                            List<Item> itemList = server.getGameSession().getItemList();
                            for (int i = 0; i < itemList.size(); i++) {
                                Item item = itemList.get(i);
                                returnString = returnString + i + " " + item.getDescription() + " X" + item.getPosX() + " Y" + item.getPosY() + " D" + item.getDespawnTime() + " ";
                                if (item instanceof Food) {
                                    returnString = returnString + ((Food) item).getFoodValue() + " ";
                                } else if (item instanceof SuperFood) {
                                    returnString = returnString + ((SuperFood) item).getValue() + " ";
                                }
                                returnString = returnString + "\n";
                            }
                            return returnString;
                        } else if (cmd[2].equalsIgnoreCase("border")) {
                            return "Worldborder: x1=-1, y1=-1, x2= " + server.getGameSession().getBorder()[0] + ", y2=" + server.getGameSession().getBorder()[1];
                        } else if (cmd[2].equalsIgnoreCase("fov")) {
                            return "Player FOV: x=" + server.getGameSession().getPlayerFOV()[0] + ", y=" + server.getGameSession().getPlayerFOV()[1];
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
            } else if (cmd[1].equalsIgnoreCase("modify")) {
                if (server.isGameAvail()) {
                    if (cmd.length >= 3) {
                        if (cmd[2].equalsIgnoreCase("snake")) {
                            if (cmd.length >= 4) {
                                Snake snake;

                                try {
                                    snake = server.getGameSession().getSnakeOfConnection(server.getConnectionByUUID(UUID.fromString(cmd[3])));
                                } catch (IllegalArgumentException e) {
                                    try {
                                        snake = server.getGameSession().getSnake(Integer.parseInt(cmd[3]));
                                    } catch (IllegalArgumentException e2) {
                                        return "Please specify a valid connection UUID or snake id";
                                    }
                                }

                                if (snake != null) {

                                    if (cmd.length >= 5) {
                                        if (cmd[4].equalsIgnoreCase("kill")) {
                                            snake.killSnake();
                                            return "Killed snake";
                                        } else if (cmd[4].equalsIgnoreCase("position")) {
                                            if (cmd.length == 7) {
                                                try {
                                                    snake.setPosition(Integer.parseInt(cmd[5]), Integer.parseInt(cmd[6]));
                                                    return "Updated snake position";
                                                } catch (IllegalArgumentException e) {
                                                    return "Please specify a valid value";
                                                }
                                            } else {
                                                return "USAGE: game modify snake <UUID/ID> position <X> <Y>";
                                            }
                                        } else if (cmd[4].equalsIgnoreCase("bodies")) {
                                            if (cmd.length >= 6) {
                                                if (cmd[5].equalsIgnoreCase("add")) {
                                                    if (cmd.length == 7) {
                                                        snake.addBody(Integer.parseInt(cmd[6]));
                                                        return "Updated snake length";
                                                    } else {
                                                        return "Value required";
                                                    }
                                                } else if (cmd[5].equalsIgnoreCase("remove")) {
                                                    if (cmd.length == 7) {
                                                        snake.removeBody(Integer.parseInt(cmd[6]));
                                                        return "Updated snake length";
                                                    } else {
                                                        return "Value required";
                                                    }
                                                } else if (cmd[5].equalsIgnoreCase("clear")) {
                                                    snake.clearBodies();
                                                    return "Cleared snake length";
                                                } else {
                                                    return "USAGE: game modify snake <UUID/ID> bodies add/remove/clear <Value for add/remove>";
                                                }
                                            } else {
                                                return "USAGE: game modify snake <UUID/ID> bodies add/remove/clear <Value for add/remove>";
                                            }
                                        } else if (cmd[4].equalsIgnoreCase("facing")) {
                                            if (cmd.length == 6) {
                                                try {
                                                    int value = Integer.parseInt(cmd[5]);
                                                    if (value >= 0 && value <= 3) {
                                                        snake.setNewFacing(value, true);
                                                        return "Updated snake facing";
                                                    } else {
                                                        return "Wrong facing value (0-3)";
                                                    }
                                                } catch (IllegalArgumentException e) {
                                                    return "Please specify a valid value";
                                                }
                                            } else {
                                                return "USAGE: game modify snake <UUID/ID> facing <newFacing>";
                                            }
                                        } else if (cmd[4].equalsIgnoreCase("connection")) {
                                            if (cmd.length == 6) {
                                                ServerConnection connection = null;

                                                try {
                                                    connection = server.getConnectionByUUID(UUID.fromString(cmd[5]));
                                                } catch (IllegalArgumentException ignored) {
                                                }

                                                snake.setNewServerConnection(connection);

                                                if (connection != null) {
                                                    return "Updated connection to " + connection.getClientId();
                                                } else {
                                                    return "Set connection to null";
                                                }

                                            } else {
                                                return "USAGE: game modify snake <UUID/ID> connection <newConnectionUUID>";
                                            }
                                        } else if(cmd[4].equalsIgnoreCase("freeze")) {
                                            if(cmd.length == 6) {
                                                snake.setFreezed(Boolean.parseBoolean(cmd[5]));
                                                return "Set freeze snake to " + snake.isFreezed();
                                            } else {
                                                return "USAGE: game modify snake <UUID/ID> freeze true/false";
                                            }
                                        } else if(cmd[4].equalsIgnoreCase("fovbehavior")) {
                                            if(cmd.length == 6) {
                                                snake.setFixedFovPlayerdataSystem(Boolean.parseBoolean(cmd[5]));
                                                return "Set fovbehavior snake to " + snake.isFixedFovPlayerdataSystem();
                                            } else {
                                                return "USAGE: game modify snake <UUID/ID> fovbehavior true/false";
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
                            } else {
                                return "UUID required";
                            }
                        } else if (cmd[2].equalsIgnoreCase("item")) {
                            if (cmd.length >= 4) {
                                try {
                                    int itemIndex = Integer.parseInt(cmd[3]);
                                    Item item = server.getGameSession().getItemList().get(itemIndex);

                                    if (item != null) {

                                        if (cmd.length >= 5) {
                                            if (cmd[4].equalsIgnoreCase("kill")) {
                                                server.getGameSession().killItem(itemIndex);
                                                return "Killed item";
                                            } else if (cmd[4].equalsIgnoreCase("position")) {
                                                if (cmd.length == 7) {
                                                    try {
                                                        item.setPosition(Integer.parseInt(cmd[5]), Integer.parseInt(cmd[6]));
                                                        return "Updated item position";
                                                    } catch (IllegalArgumentException e) {
                                                        return "Please specify a valid position";
                                                    }
                                                } else {
                                                    return "Usage: game modify item <ID> position <X> <Y>";
                                                }
                                            } else if (cmd[4].equalsIgnoreCase("despawn")) {
                                                if (cmd.length == 6) {
                                                    try {
                                                        item.setDespawnTime(Integer.parseInt(cmd[5]));
                                                        return "Updated item despawn time";
                                                    } catch (IllegalArgumentException e) {
                                                        return "Please specify a valid int value";
                                                    }
                                                } else {
                                                    return "Usage: game modify item <ID> despawn <despawnTime>";
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
                                } catch (IndexOutOfBoundsException e) {
                                    return "This item does not exist";
                                } catch (IllegalArgumentException e) {
                                    return "Please specify a valid int";
                                }
                            } else {
                                return "Item id required";
                            }
                        } else if (cmd[2].equalsIgnoreCase("items")) {
                            if (cmd.length == 4 && cmd[3].equalsIgnoreCase("kill")) {
                                server.getGameSession().killItems();
                                return "Killed items";
                            } else {
                                return "Value not found";
                            }
                        } else {
                            return "Value not found";
                        }
                    } else {
                        return "GAME MODIFY COMMAND USAGE\n" +
                                "game modify snake <UUID/ID> kill\n" +
                                "game modify snake <UUID/ID> position <X> <Y>\n" +
                                "game modify snake <UUID/ID> bodies add <length>\n" +
                                "game modify snake <UUID/ID> bodies remove <length>\n" +
                                "game modify snake <UUID/ID> bodies clear\n" +
                                "game modify snake <UUID/ID> facing <N=0,E=1,S=2,W=3>\n" +
                                "game modify snake <UUID/ID> connection <newConnectionUUID>" +
                                "game modify item <ID> kill\n" +
                                "game modify item <ID> position <X> <Y>\n" +
                                "game modify item <ID> despawn <time>\n" +
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
                    "game start custom <sizex> <sizey> (paused)\n" +
                    "game start savegame <SAVEGAME DATA>\n" +
                    "game stop\n" +
                    "game pause\n" +
                    "game resume\n" +
                    "game info\n" +
                    "game getsave\n" +
                    "game mcontrol <...>\n" +
                    "game get <...>\n" +
                    "game modify <...>\n";
        }
    }

    public static String loggerCommand(SlakeoverflowServer server, String[] cmd) {
        if (cmd.length >= 2) {
            if (cmd[1].equalsIgnoreCase("list")) {
                String returnString = "LOG:\n";

                int index = 0;
                for (Object logEntryObject : server.getLogger().getLog()) {
                    JSONObject logEntry = (JSONObject) logEntryObject;

                    returnString = returnString + index + " " + logEntry.getString("time") + " " + logEntry.getString("type") + " " + logEntry.getString("module") + "\n";
                    index++;
                }
                return returnString;
            } else if (cmd[1].equalsIgnoreCase("get")) {
                try {
                    if (cmd.length == 3) {
                        int index = Integer.parseInt(cmd[2]);
                        JSONObject logEntry = server.getLogger().getLog().getJSONObject(index);
                        return "LOG ENTRY:\n" +
                                "ID: " + index + "\n" +
                                "Time: " + logEntry.getString("time") + "\n" +
                                "Type: " + logEntry.getString("type") + "\n" +
                                "Module: " + logEntry.getString("module") + "\n" +
                                "Content: " + logEntry.getString("text").replace("\n", "%20") + "\n";
                    } else {
                        return "Please specify a log id";
                    }
                } catch (JSONException e) {
                    return "Log error";
                } catch (IllegalArgumentException e) {
                    return "Please specify a valid int value";
                }
            } else if (cmd[1].equalsIgnoreCase("save")) {
                if (cmd.length == 3) {
                    try {
                        File file = new File(System.getProperty("user.dir"), cmd[2]);
                        if (!file.exists()) {
                            server.getLogger().saveLog(file, false);
                            return "File was written";
                        } else {
                            return "File does already exist";
                        }
                    } catch (IOException e) {
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

    private static String infoCommand(SlakeoverflowServer server) {
        return "SERVER INFO:\n" +
                "Tick rate: " + server.getTickRate() + " tps, Skipped Ticks (1m): " + server.getSkippedTicks() + " (counter=" + server.getTickDuration() + ")" + "\n" +
                "Used memory: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "MB / " + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB\n" +
                "Manager Thread: " + server.isManagerThreadAlive() + " " + server.getManagerThreadState() + "\n" +
                "Tick Thread: " + server.isTickThreadAlive() + " " + server.getTickThreadState() + "\n" +
                "Times Thread: " + server.isTimesThreadAlive() + " " + server.getTimesThreadState() + "\n";
    }

    private static String accountCommand(SlakeoverflowServer server, String[] cmd) {
        if(cmd.length >= 2) {
            if(cmd[1].equalsIgnoreCase("create")) {
                if(cmd.length == 3) {
                    long id = server.getAccountSystem().createAccount(cmd[2], null);
                    if(id > -1) {
                        return "Account created with id " + id;
                    } else {
                        return "Account could not be created";
                    }
                } else if(cmd.length == 4) {
                    long id = server.getAccountSystem().createAccount(cmd[2], cmd[3]);
                    if(id > -1) {
                        return "Account created with id " + id;
                    } else {
                        return "Account could not be created";
                    }
                } else {
                    return "USAGE: account create <username> (password)";
                }
            } else if(cmd[1].equalsIgnoreCase("delete")) {
                if(cmd.length == 3) {
                    if(server.getAccountSystem().deleteAccount(Long.parseLong(cmd[2]))) {
                        return "Account deleted";
                    } else {
                        return "Account could not be deleted";
                    }
                } else {
                    return "USAGE: account delete <ID>";
                }
            } else if(cmd[1].equalsIgnoreCase("info")) {
                if(cmd.length == 3) {
                    AccountData accountData = server.getAccountSystem().getAccount(Long.parseLong(cmd[2]));

                    if(accountData != null) {
                        String password;
                        if(accountData.getPassword() != null) {
                            password = "AVAIL";
                        } else {
                            password = "DISABLED";
                        }

                        return "ACCOUNT INFORMATION:\n" +
                                "ID: " + accountData.getId() + "\n" +
                                "Username: " + accountData.getUsername() + "\n" +
                                "Password: " + password + "\n" +
                                "Permission: " + AccountPermissionLevel.toString(accountData.getPermissionLevel()) + "\n" +
                                "Muted: " + accountData.isMuted() + "\n" +
                                "Banned: " + accountData.isBanned() + "\n" +
                                "Level: " + accountData.getLevel() + "\n" +
                                "Balance: " + accountData.getBalance() + " Coins\n" +
                                "Shop data: " + accountData.getShopData().toString() + "\n";
                    } else {
                        return "Account does not exist";
                    }

                } else {
                    return "Usage: account info <ID>";
                }
            } else if(cmd[1].equalsIgnoreCase("list")) {
                List<AccountData> accountDataList = server.getAccountSystem().getAccounts();

                int count = 0;
                String returnString = "REGISTERED ACCOUNTS:\n";
                for(AccountData account : accountDataList) {
                    String password;
                    if(account.getPassword() != null) {
                        password = "AVAIL";
                    } else {
                        password = "DISABLED";
                    }

                    returnString = returnString + account.getId() + " " + account.getUsername() + " " + password + " " + AccountPermissionLevel.toString(account.getPermissionLevel()) + " " + account.isMuted() + " " + account.isBanned() + "\n";
                    count++;
                }
                returnString = returnString + "---- " + count + " registered accounts ----";
                return returnString;
            } else if(cmd[1].equalsIgnoreCase("update")) {
                if(cmd.length == 5) {
                    AccountData account = server.getAccountSystem().getAccount(Long.parseLong(cmd[2]));

                    if(account != null) {

                        try {
                            if(cmd[3].equalsIgnoreCase("username")) {
                                if(server.getAccountSystem().updateUsername(account.getId(), cmd[4])) {
                                    return "Updated username";
                                } else {
                                    return "Username not updated";
                                }
                            } else if(cmd[3].equalsIgnoreCase("password")) {
                                if(server.getAccountSystem().updatePassword(account.getId(), cmd[4])) {
                                    return "Updated password";
                                } else {
                                    return "Password not updated";
                                }
                            } else if(cmd[3].equalsIgnoreCase("permission")) {
                                if(server.getAccountSystem().updatePermissionLevel(account.getId(), Integer.parseInt(cmd[4]))) {
                                    return "Updated permission level";
                                } else {
                                    return "Permission level was not updated";
                                }
                            } else if (cmd[3].equalsIgnoreCase("ban")) {
                                if(server.getAccountSystem().updateBanned(account.getId(), Boolean.parseBoolean(cmd[4]))) {
                                    return "Updated banned state to " + Boolean.parseBoolean(cmd[4]);
                                } else {
                                    return "Banned state was not updated";
                                }
                            } else if (cmd[3].equalsIgnoreCase("mute")) {
                                if(server.getAccountSystem().updateMuted(account.getId(), Boolean.parseBoolean(cmd[4]))) {
                                    return "Updated muted state to " + Boolean.parseBoolean(cmd[4]);
                                } else {
                                    return "Muted state was not updated";
                                }
                            } else if(cmd[3].equalsIgnoreCase("level")) {
                                if(server.getAccountSystem().updateLevel(account.getId(), Integer.parseInt(cmd[4]))) {
                                    return "Updated level";
                                } else {
                                    return "Level was not updated";
                                }
                            } else if(cmd[3].equalsIgnoreCase("balance")) {
                                if(server.getAccountSystem().updateBalance(account.getId(), Integer.parseInt(cmd[4]))) {
                                    return "Updated balance";
                                } else {
                                    return "Balance was not updated";
                                }
                            } else if(cmd[3].equalsIgnoreCase("shopdata")) {
                                if(cmd[4].equalsIgnoreCase("reset")) {
                                    if(server.getAccountSystem().resetShopData(account.getId())) {
                                        return "Shop data was reset";
                                    } else {
                                        return "Shop data was not reset";
                                    }
                                } else {
                                    return "Usage: account update <ID> shopData reset (ShopData can be modified via shop command)";
                                }
                            } else {
                                return "Run command without arguments for help";
                            }
                        } catch (IllegalArgumentException e) {
                            return "Please specify a valid value";
                        }

                    } else {
                        return "Account does not exist";
                    }
                } else {
                    return "Usage: account update <ID> username/password/permission/ban/mute/level/balance/shopdata <value>";
                }
            } else if(cmd[1].equalsIgnoreCase("getid")) {
                if(cmd.length == 3) {
                    AccountData accountData = server.getAccountSystem().getAccount(cmd[2]);

                    if(accountData != null) {
                        return "Account ID of " + accountData.getUsername() + ": " + accountData.getId();
                    } else {
                        return "Username not found";
                    }
                } else {
                    return "Usage: account getid <username>";
                }
            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "ACCOUNT COMMAND USAGE:\n" +
                    "account create <username> (password)\n" +
                    "account delete <ID>\n" +
                    "account info <ID>\n" +
                    "account list\n" +
                    "account update <ID> username/password/permission/ban/mute/level/balance/shopdata <value>\n" +
                    "account getid <username>\n";
        }
    }

    public static String shopCommand(SlakeoverflowServer server, String[] cmd) {
        if(cmd.length >= 2) {

            if(cmd[1].equalsIgnoreCase("list")) {
                String returnString = "SHOP ITEMS (ID, PRICE):\n";

                Map<Integer, ShopItem> persistentItems = server.getShopManager().getPersistentShopItems();
                Map<Integer, ShopItem> customItems = server.getShopManager().getCustomShopItems();

                for(int id : persistentItems.keySet()) {
                    returnString = returnString + id + " " + persistentItems.get(id).isEnabled() + " " + persistentItems.get(id).getRequiredLevel() + " " + persistentItems.get(id).getPrice() + " " + "PERSISTENT" +  "\n";
                }

                for(int id : customItems.keySet()) {
                    returnString = returnString + id + " " + customItems.get(id).isEnabled() + " " + customItems.get(id).getRequiredLevel() + " " + customItems.get(id).getPrice() + " " + "CUSTOM" +  "\n";
                }

                returnString = returnString + persistentItems.size() + " persistent items, " + customItems.size() + " custom items.\n";

                return returnString;
            } else if(cmd[1].equalsIgnoreCase("addItemToUser")) {
                if(cmd.length == 4) {
                    AccountData account;

                    try {
                        account = server.getAccountSystem().getAccount(Integer.parseInt(cmd[2]));
                    } catch (NumberFormatException e) {
                        account = server.getAccountSystem().getAccount(cmd[2]);
                    }

                    if(account != null) {
                        server.getShopManager().addItemToAccount(account.getId(), Integer.parseInt(cmd[3]));
                        return "Item added (if it exist)";
                    } else {
                        return "Account not found";
                    }
                } else {
                    return "USAGE: shop addItemToUser <accountId> <itemId>";
                }
            } else if(cmd[1].equalsIgnoreCase("removeItemFromUser")) {
                if(cmd.length == 4) {
                    AccountData account;

                    try {
                        account = server.getAccountSystem().getAccount(Integer.parseInt(cmd[2]));
                    } catch (NumberFormatException e) {
                        account = server.getAccountSystem().getAccount(cmd[2]);
                    }

                    if(account != null) {
                        server.getShopManager().removeItemFromAccount(account.getId(), Integer.parseInt(cmd[3]));
                        return "Item removed (if it exists)";
                    } else {
                        return "Account not found";
                    }
                } else {
                    return "USAGE: shop removeItemFromUser <accountId> <itemId>";
                }
            } else if(cmd[1].equalsIgnoreCase("clearItemsFromUser")) {
                if(cmd.length == 3) {
                    AccountData account;

                    try {
                        account = server.getAccountSystem().getAccount(Integer.parseInt(cmd[2]));
                    } catch (NumberFormatException e) {
                        account = server.getAccountSystem().getAccount(cmd[2]);
                    }

                    if(account != null) {
                        server.getShopManager().clearItemsFromAccount(account.getId());
                        return "Items cleared";
                    } else {
                        return "Account not found";
                    }
                } else {
                    return "USAGE: shop clearItemsFromUser <accountId>";
                }
            } else if(cmd[1].equalsIgnoreCase("listItemsFromUser")) {
                if(cmd.length == 3) {
                    AccountData account;

                    try {
                        account = server.getAccountSystem().getAccount(Integer.parseInt(cmd[2]));
                    } catch (NumberFormatException e) {
                        account = server.getAccountSystem().getAccount(cmd[2]);
                    }

                    if(account != null) {
                        List<Integer> items = server.getShopManager().getItemsFromAccount(account.getId());

                        String returnString = "ITEMS OF ACCOUNT " + account.getId() + ":\n";

                        for(int id : items) {
                            returnString = returnString + id + " ";
                        }

                        returnString = returnString + "\n";

                        returnString = returnString + account.getId() + " has " + items.size() + " items";

                        return returnString;
                    } else {
                        return "Account not found";
                    }
                } else {
                    return "USAGE: shop listItemsFromUser <accountId>";
                }
            } else {
                return "Run command without arguments for help";
            }

        } else {
            return "SHOP COMMAND USAGE:\n" +
                    "shop list\n" +
                    "shop addItemToUser <accountId> <itemId>\n" +
                    "shop removeItemsFromUser <accountId> <itemId>\n" +
                    "shop clearItemsFromUser <accountId>\n" +
                    "shop listItemsFromUser <accountId>\n";
        }
    }

    public static String chatCommand(SlakeoverflowServer server, String[] cmd) {
        if(cmd.length >= 2) {
            if(cmd[1].equalsIgnoreCase("write") && cmd.length >= 3) {
                String message = "";
                for(int i = 2; i < cmd.length; i++) {
                    message = message + cmd[i];
                }

                server.getChatSystem().adminMessage(message, false);

                return "Message " + message + " sent to everyone";
            } else if (cmd[1].equalsIgnoreCase("broadcast") && cmd.length >= 3) {
                String message = "";
                for(int i = 2; i < cmd.length; i++) {
                    message = message + cmd[i];
                }

                server.getChatSystem().adminMessage(message, true);

                return "Message " + message + " broadcasted to everyone";
            } else if (cmd[1].equalsIgnoreCase("private") && cmd.length >= 4) {
                try {
                    ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));

                    if(connection != null) {
                        String message = "";
                        for(int i = 3; i < cmd.length; i++) {
                            message = message + cmd[i];
                        }

                        server.getChatSystem().adminMessage(message, false, connection);

                        return "Message " + message + " sent to " + connection.getClientId();
                    } else {
                        return "Connection not found";
                    }
                } catch (IllegalArgumentException e) {
                    return "Wrong UUID format";
                }
            } else if (cmd[1].equalsIgnoreCase("privatebc") && cmd.length >= 4) {
                ServerConnection connection = server.getConnectionByUUID(UUID.fromString(cmd[2]));

                if(connection != null) {
                    String message = "";
                    for(int i = 3; i < cmd.length; i++) {
                        message = message + cmd[i];
                    }

                    server.getChatSystem().adminMessage(message, true, connection);

                    return "Message " + message + " broadcasted to " + connection.getClientId();
                } else {
                    return "Connection not found";
                }
            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "CHAT COMMAND USAGE:\n" +
                    "chat write <message>\n" +
                    "chat broadcast <message>\n" +
                    "chat private <user> <message>\n" +
                    "chat privatebc <user> <message>\n" +
                    "To moderate the chat, you can use config options and/or ban/mute users/accounts via user/account command\n";
        }
    }
}
