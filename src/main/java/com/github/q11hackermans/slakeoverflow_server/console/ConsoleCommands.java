package com.github.q11hackermans.slakeoverflow_server.console;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

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
                case "config":
                    return configCommand(cmd);
            }
        }
        return "";
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
                    } else {
                        return "Unknown config option";
                    }
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
                        "max_food_value: " + SlakeoverflowServer.getServer().getConfigManager().getConfig().getMaxFoodValue() + "\n";
            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "Command usage:\nconfig set <OPTION> <VALUE>\nconfig get <OPTION>\nconfig list\n";
        }
    }
}
