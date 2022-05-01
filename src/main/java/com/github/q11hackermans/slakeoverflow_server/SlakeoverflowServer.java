package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.console.ConsoleLogger;
import com.github.q11hackermans.slakeoverflow_server.console.ServerConsole;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import net.jandie1505.connectionmanager.server.CMSServer;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOManager;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.sqrt;
import static java.lang.Math.pow;

public class SlakeoverflowServer {
    // STATIC
    private static SlakeoverflowServer server;
    // CONSOLE
    private final ServerConsole console;
    private final ConsoleLogger logger;
    // CONNECTION MANAGER
    private final CMSServer connectionhandler;
    private final DataIOManager dataIOManager;
    // THREADS
    private Thread managerThread;
    private Thread tickThread;
    // GAME SESSION
    private int gameState;
    private GameSession game;
    // PLAYER MANAGEMENT
    private final List<Player> playerList;


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

        // PLAYER MANAGEMENT
        this.playerList = new ArrayList<>();

        // THREADS
        this.managerThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted() && this.managerThread != null) {
                try {
                    checkConnectionManager();
                    checkPlayerList();
                } catch(Exception e) {
                    try {
                        this.logger.warning("TICK", "EXCEPTION: " + e.toString() + ": " + Arrays.toString(e.getStackTrace()) + " (THIS EXCEPTION IS THE CAUSE FOR STOPPING THE SERVER)");
                        e.printStackTrace();
                    } catch(Exception ignored) {}
                    stop();
                }
            }
        });
        this.managerThread.setName("SLAKEOVERFLOW-MANAGER-" + this.toString());
        this.managerThread.start();
        this.logger.debug("INIT", "Started Thread MANAGER");

        this.tickThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted() && this.tickThread != null) {
                try {
                    if(this.game != null && gameState == GameState.RUNNING) {
                        this.game.tick();
                    }
                    Thread.sleep(50);
                } catch(Exception e) {
                    Thread.currentThread().interrupt();
                    this.logger.warning("TICK", "EXCEPTION: " + e.toString() + ": " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                }
            }
        });
        this.tickThread.setName("SLAKEOVERFLOW-TICK-" + this.toString());
        this.tickThread.start();
        this.logger.debug("INIT", "Started Thread TICK");

        // FINISHED (RUN ALWAYS LAST)
        this.logger.info("INIT", "Setup complete");
    }

    // SERVER MANAGEMENT
    /**
     * This will stop the server.
     */
    public void stop() {
        try {
            this.managerThread.interrupt();
            this.tickThread.interrupt();
            this.dataIOManager.close();
            this.connectionhandler.close();
            this.console.stop();
        } catch(Exception ignored) {}
        System.exit(0);
    }

    // GAME MANAGEMENT
    /**
     *
     */
    private void setupGame(int sizeX, int sizeY) {
        if (this.gameState == GameState.STOPPED){
            this.game = new GameSession(sizeX,sizeY);
        }
    }

    /**
     *
     */
    public void setupGame() {
        double x = 3;
        this.setupGame((int) Math.round(50+(sqrt(((pow(x,2)*10)/((3*x)+(4*(x/6)))))*x*9)),(int)((Math.round(50+(sqrt(((pow(x,2)*10)/((3*x)+(4*(x/6)))))*x*9))))/3);
    }

    // PRIVATE METHODS
    private void checkConnectionManager() {

    }

    private void checkPlayerList() {

    }

    // GETTER METHODS
    public ServerConsole getConsole() {
        return this.console;
    }

    public ConsoleLogger getLogger() {
        return this.logger;
    }

    public CMSServer getConnectionhandler() {
        return this.connectionhandler;
    }

    public DataIOManager getDataIOManager() {
        return this.dataIOManager;
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
