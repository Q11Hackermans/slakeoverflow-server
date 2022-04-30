package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.console.ConsoleLogger;
import com.github.q11hackermans.slakeoverflow_server.console.ServerConsole;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import net.jandie1505.connectionmanager.server.CMSServer;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOManager;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOType;

import java.io.IOException;

public class SlakeoverflowServer {
    // STATIC
    private static SlakeoverflowServer server;
    // CONSOLE
    private final ServerConsole console;
    private final ConsoleLogger logger;
    // CONNECTION MANAGER
    private final CMSServer connectionhandler;
    private final DataIOManager dataIOManager;
    // GAME SESSION
    private int gameState;
    private GameSession game;


    public SlakeoverflowServer() throws IOException {
        // SET SERVER (RUN ALWAYS FIRST)
        server = this;

        // CONSOLE
        this.logger = new ConsoleLogger();
        this.console = new ServerConsole(this.logger);
        this.console.start();

        // CONNECTION MANAGER
        this.connectionhandler = new CMSServer(55555);
        this.connectionhandler.addListener(new EventListener());
        this.connectionhandler.addGlobalListener(new EventListener());
        this.dataIOManager = new DataIOManager(this.connectionhandler, DataIOType.UTF, false);

        // GAME SESSION
        this.gameState = GameState.STOPPED;
        this.game = null;

        // FINISHED (RUN ALWAYS LAST)
        this.logger.info("INIT", "Setup complete");
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
