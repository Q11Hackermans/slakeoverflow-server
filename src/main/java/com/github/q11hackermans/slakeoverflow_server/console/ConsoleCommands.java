package com.github.q11hackermans.slakeoverflow_server.console;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

public class ConsoleCommands {
    public static void run(String[] cmd) {
        if(cmd.length >= 1) {
            switch(cmd[0]) {
                case "stop":
                    System.out.println("Shutting down...");
                    SlakeoverflowServer.getServer().stop();
                    break;
            }
        }
    }
}
