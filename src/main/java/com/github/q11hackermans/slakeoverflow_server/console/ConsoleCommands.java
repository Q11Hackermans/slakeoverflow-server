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
            }
        }
        return "";
    }
}
