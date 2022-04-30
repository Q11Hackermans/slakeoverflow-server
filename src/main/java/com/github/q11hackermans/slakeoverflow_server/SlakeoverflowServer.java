package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.console.ConsoleLogger;
import com.github.q11hackermans.slakeoverflow_server.console.ServerConsole;
import net.jandie1505.connectionmanager.server.CMSServer;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOManager;

import java.io.IOException;

public class SlakeoverflowServer {
    // STATIC
    private static SlakeoverflowServer server;
    // CONSOLE
    private final ServerConsole console;
    private final ConsoleLogger logger;
    // CONNECTION MANAGER
    private CMSServer connectionhandler;
    private DataIOManager dataIOManager;


    public SlakeoverflowServer() throws IOException {
        // SET SERVER (RUN ALWAYS FIRST)
        server = this;
        // CONSOLE
        this.logger = new ConsoleLogger();
        this.console = new ServerConsole(this.logger);
        this.console.start();
        // CONNECTION MANAGER
        this.connectionhandler = new CMSServer(55555);
    }

    /**
     * This will stop the server.
     */
    public void stop() {
        try {
            this.dataIOManager.close();
            this.connectionhandler.close();
            this.console.stop();
        } catch(Exception ignored) {}
        System.exit(0);
    }

    // GETTER METHODS
    public ServerConsole getConsole() {
        return this.console;
    }

    public ConsoleLogger getLogger() {
        return this.logger;
    }

    /*
    -----------------------------------------------------------------
    STATIC METHODS
    -----------------------------------------------------------------
     */
    // HIER GEHTS LOS :) {"cmd":"tick","fields":[[0,1,0],[],[]]}  {"cmd":"auth","username":"vollidiot123"} {"cmd":"auth2","sizex":100,"sizey":100} {"cmd":"auth3"}
    public static void main(String[] args) throws IOException {
        new SlakeoverflowServer();
    }

    public static SlakeoverflowServer getServer() {
        return server;
    }
}
