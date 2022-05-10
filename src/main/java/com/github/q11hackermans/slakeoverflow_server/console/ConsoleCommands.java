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
        if(cmd.length >= 2) {
            if(cmd[1].equalsIgnoreCase("set")) {
                if(cmd.length == 4) {
                    if(cmd[3].equalsIgnoreCase("")) {

                    }
                } else {
                    return "Run command without arguments for help";
                }
            } else if(cmd[1].equalsIgnoreCase("get")) {

            } else if(cmd[1].equalsIgnoreCase("list")) {

            } else {
                return "Run command without arguments for help";
            }
        } else {
            return "Command usage:\nconfig set <OPTION> <VALUE>\nconfig get <OPTION>\nconfig list\n";
        }
        return "damit der fehler verschwindet..."; // TO BE REMOVED
    }
}
