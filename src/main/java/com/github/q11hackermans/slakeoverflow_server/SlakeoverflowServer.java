package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.config.ConfigManager;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.console.ConsoleLogger;
import com.github.q11hackermans.slakeoverflow_server.console.ServerConsole;
import com.github.q11hackermans.slakeoverflow_server.constants.ConnectionType;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.CMSServer;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOManager;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOType;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class SlakeoverflowServer {
    // STATIC
    private static SlakeoverflowServer server;
    // CONSOLE
    private final ServerConsole console;
    private final ConsoleLogger logger;
    // CONFIG
    private final ConfigManager configManager;
    // CONNECTION MANAGER
    private final CMSServer connectionhandler;
    private final DataIOManager dataIOManager;
    // THREADS
    private Thread managerThread;
    private Thread tickThread;
    private final int tickSpeed;
    private final int idleTickSpeed;
    // GAME SESSION
    private int gameState;
    private GameSession game;
    // PLAYER MANAGEMENT
    private final List<ServerConnection> connectionList;
    // LISTS
    private final List<InetAddress> ipBlacklist;
    private boolean alreadyStopping;


    public SlakeoverflowServer(boolean advancedConfigOptions, boolean defaultConfigValues) throws IOException {
        // SET SERVER (RUN ALWAYS FIRST)
        server = this;

        // WELCOME MESSAGE
        System.out.println(getASCIISignature());

        // CONSOLE
        this.logger = new ConsoleLogger();
        this.logger.info("INIT", "Server init");
        this.console = new ServerConsole(this.logger);
        this.console.start();

        // CONFIG
        this.configManager = new ConfigManager(advancedConfigOptions, !defaultConfigValues);
        if(!defaultConfigValues) {
            this.tickSpeed = this.configManager.getConfig().getCustomServerTickrate();
            this.idleTickSpeed = this.configManager.getConfig().getCustomServerTickrateIdle();
        } else {
            this.tickSpeed = 50;
            this.idleTickSpeed = 950;
        }


        // CONNECTION MANAGER
        this.connectionhandler = new CMSServer(this.configManager.getConfig().getPort());
        this.connectionhandler.addListener(new EventListener());
        this.connectionhandler.addGlobalListener(new EventListener());
        this.dataIOManager = new DataIOManager(this.connectionhandler, DataIOType.UTF, false);
        this.dataIOManager.addEventListener(new EventListener());

        // GAME SESSION
        this.gameState = GameState.STOPPED;
        this.game = null;

        // PLAYER MANAGEMENT
        this.connectionList = new ArrayList<>();

        // LIST
        this.ipBlacklist = new ArrayList<>();

        // MISC
        this.alreadyStopping = false;

        // THREADS
        this.managerThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch(Exception ignored) {}
            while(!Thread.currentThread().isInterrupted() && this.managerThread == Thread.currentThread()) {
                try {
                    checkThreads();
                    checkConnectionManager();
                    checkConnections();
                    checkGameSession();
                } catch(Exception e) {
                    try {
                        this.logger.warning("MANAGER", "EXCEPTION: " + e.toString() + ": " + Arrays.toString(e.getStackTrace()) + " (THIS EXCEPTION IS THE CAUSE FOR STOPPING THE SERVER)");
                        e.printStackTrace();
                    } catch(Exception ignored) {}
                    stop();
                }
            }
            if(!alreadyStopping) {
                stop();
            }
        });
        this.managerThread.setName("SLAKEOVERFLOW-MANAGER-" + this.toString());
        this.managerThread.start();
        this.logger.debug("INIT", "Started Thread MANAGER");

        this.tickThread = this.getTickThreadTemplate();
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
            this.alreadyStopping = true;
            this.managerThread.interrupt();
            if(this.tickThread != null) {
                this.tickThread.interrupt();
            }
            this.dataIOManager.close();
            this.connectionhandler.close();
            this.console.stop();
            this.logger.info("STOP", "Server shutdown.");
            this.logger.saveLog(new File(System.getProperty("user.dir"), "log.json"), true);
        } catch(Exception ignored) {
            ignored.printStackTrace();
        }
        System.exit(0);
    }

    // GAME MANAGEMENT
    /**
     * This will create a new game with a specific size
     */
    public boolean setupGame(int sizeX, int sizeY) {
        if(this.gameState == GameState.STOPPED && sizeX > 0 && sizeY > 0) {
            this.gameState = GameState.PREPARING;
            this.game = new GameSession(sizeX,sizeY);
            this.gameState = GameState.RUNNING;
            this.logger.info("GAME", "Game with size " + sizeX + " " + sizeY + " was set up");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Setup game automatically
     */
    public boolean setupGameAutomatically() {
        double x = 3;
        return this.setupGame((int) Math.round(50+(sqrt(((pow(x,2)*10)/((3*x)+(4*(x/6)))))*x*9)),(int)((Math.round(50+(sqrt(((pow(x,2)*10)/((3*x)+(4*(x/6)))))*x*9))))/3);
    }

    /**
     * Setup game with default config values
     */
    public boolean setupGameDefault() {
        return this.setupGame(this.configManager.getConfig().getDefaultGameFieldSizeX(), this.configManager.getConfig().getDefaultGameFieldSizeY());
    }

    /**
     * Stop a running game
     */
    public boolean stopGame() {
        boolean success = false;

        if(this.isGameAvail()) {
            success = true;
        }

        this.gameState = GameState.STOPPED;
        this.game = null;

        this.logger.info("GAME", "Stopped game (success=" + success + ")");

        return success;
    }

    /**
     * Pause a currently running game
     * @return true if the game could be paused
     */
    public boolean pauseGame() {
        if(this.gameState == GameState.RUNNING) {
            this.gameState = GameState.PAUSED;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resumes a paused game
     * @return true if the game could be resumed
     */
    public boolean resumeGame() {
        if(this.gameState == GameState.PAUSED) {
            this.gameState = GameState.RUNNING;
            return true;
        } else {
            return false;
        }
    }

    // CONNECTION MANAGEMENT

    /**
     * Check if a connection with a specific ConnectionManager UUID exists
     * @param uuid Unique Connection ID
     * @return boolean
     */
    public boolean containsConnection(UUID uuid) {
        for(ServerConnection connection : this.connectionList) {
            if(connection.getClientId().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a connection with a specific ConnectionManager UUID.
     * @param uuid ConnectionManager UUID
     * @return ServerConnection (if exists), null (if not exists)
     */
    public ServerConnection getConnectionByUUID(UUID uuid) {
        for(ServerConnection connection : this.connectionList) {
            if(connection.getClientId().equals(uuid)) {
                return connection;
            }
        }
        return null;
    }

    public int getPlayerCount() {
        int connectionCount = 0;
        for(ServerConnection connection : this.connectionList) {
            if(connection.getConnectionType() == ConnectionType.PLAYER) {
                connectionCount++;
            }
        }
        return connectionCount;
    }

    public void authenticateConnectionAsPlayer(UUID connectionUUID, boolean ignoreConnectionConditions) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);
        if(connection != null) {
            if(ignoreConnectionConditions || (this.configManager.getConfig().isUserAuthentication() && this.getPlayerCount() < this.configManager.getConfig().getSlots())) {
                connection.authenticateAsPlayer();
            }
        }
    }

    public void unauthenticateConnection(UUID connectionUUID) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);
        if(connection != null) {
            connection.unauthenticate();
        }
    }

    // PRIVATE METHODS
    private void checkConnectionManager() {
        if(this.connectionhandler == null || this.connectionhandler.isClosed()) {
            this.getLogger().warning("MANAGER", "Connection manager is closed. Stopping server.");
            this.stop();
        }
    }

    private void checkThreads() {
        if(this.tickThread == null || !this.tickThread.isAlive()) {
            this.tickThread = this.getTickThreadTemplate();
            this.tickThread.start();
            this.logger.warning("MANAGER", "Restarting Thread TICK");
        }
    }

    private void checkConnections() {
        this.connectionList.removeIf(serverConnection -> serverConnection.getDataIOStreamHandler() == null);
        this.connectionList.removeIf(serverConnection -> serverConnection.getDataIOStreamHandler().isClosed());
        this.connectionList.removeIf(serverConnection -> serverConnection.getClient() == null);
        this.connectionList.removeIf(serverConnection -> serverConnection.getClient().isClosed());

        for(CMSClient cmsClient : this.connectionhandler.getClientList()) {
            if(!this.containsConnection(cmsClient.getUniqueId())) {
                this.connectionList.add(new ServerConnection(cmsClient.getUniqueId()));
            }
        }
    }

    private void checkGameSession() {
        if((this.game == null) || (this.gameState == GameState.STOPPED)) {
            if(this.game == null && this.gameState != GameState.STOPPED && this.gameState != GameState.PREPARING) {
                this.gameState = GameState.STOPPED;
            }
            if(this.gameState == GameState.STOPPED && this.game != null) {
                this.game = null;
            }
        }
    }

    private void sendStatusMessage() {
        for(ServerConnection connection : this.connectionList) {
            JSONObject statusMessage = new JSONObject();
            statusMessage.put("cmd", "status");
            statusMessage.put("status", this.gameState);
            statusMessage.put("auth", connection.getConnectionType());

            try {
                connection.getDataIOStreamHandler().writeUTF(statusMessage.toString());
            } catch (IOException e) {
                this.logger.warning("CONNECTION", "Error while sending data to " + connection.getClientId());
                connection.getClient().close();
            }
        }
    }

    // IP BLACKLIST
    public void addIpToBlacklist(InetAddress inetAddress) {
        this.ipBlacklist.add(inetAddress);
        this.logger.info("BLACKLIST", "Added " + inetAddress.toString() + " to IP blacklist");
    }

    public void removeIpFromBlacklist(InetAddress inetAddress) {
        this.ipBlacklist.removeIf(inetAddress1 -> inetAddress1.equals(inetAddress));
        this.logger.info("BLACKLIST", "Removed " + inetAddress.toString() + " from IP blacklist");
    }

    public void clearIpBlacklist() {
        this.ipBlacklist.clear();
        this.logger.info("BLACKLIST", "Cleared IP blacklist");
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

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public List<InetAddress> getIpBlacklist() {
        return List.copyOf(this.ipBlacklist);
    }

    public List<ServerConnection> getConnectionList() {
        return List.copyOf(this.connectionList);
    }

    public int getGameState() {
        return this.gameState;
    }

    public GameSession getGameSession() {
        return this.game;
    }

    public boolean isGameAvail() {
        return (this.gameState == GameState.RUNNING || this.gameState == GameState.PAUSED);
    }

    // THREAD TEMPLATES
    private Thread getTickThreadTemplate() {
        Thread thread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted() && this.tickThread == Thread.currentThread()) {
                try {
                    this.sendStatusMessage();
                    if(this.game != null && gameState == GameState.RUNNING) {
                        this.game.tick();
                    } else {
                        Thread.sleep(this.idleTickSpeed);
                    }
                    Thread.sleep(this.tickSpeed);
                } catch(Exception e) {
                    Thread.currentThread().interrupt();
                    this.logger.warning("TICK", "EXCEPTION: " + e.toString() + ": " + Arrays.toString(e.getStackTrace()));
                    if(!(e instanceof InterruptedException)) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setName("SLAKEOVERFLOW-TICK-" + this.toString());
        return thread;
    }

    /*
    -----------------------------------------------------------------
    STATIC METHODS
    -----------------------------------------------------------------
     */
    // HIER GEHTS LOS :) {"cmd":"tick","fields":[[0,1,0],[],[]]}  {"cmd":"auth","username":"vollidiot123"} {"cmd":"auth2","sizex":100,"sizey":100} {"cmd":"auth3"}
    public static void main(String[] args) throws IOException {
        System.out.println("Slakeoverflow Server Launcher by Q11-Hackermans (https://github.com/Q11Hackermans)");

        int waitTime = 3;
        Map<String, String> startArguments = new HashMap<>();
        try {
            for(String arg : args) {
                if(arg.startsWith("-")) {
                    arg = arg.replace("-", "");
                    try {
                        String[] argument = arg.split("=");
                        startArguments.put(argument[0], argument[1]);
                    } catch(Exception e) {
                        System.out.println("Incorrect start argument: " + arg);
                        waitTime = 10;
                    }
                } else {
                    System.out.println("Wrong start argument format: " + arg);
                    waitTime = 10;
                }
            }
        } catch (Exception e) {
            System.out.println("Error with start arguments. Starting with default arguments...");
            waitTime = 30;
        }

        boolean advancedOptions = false;
        advancedOptions = Boolean.parseBoolean(startArguments.get("enableAdvancedConfigOptions"));
        if(advancedOptions) {
            System.out.println("\n" +
                    "-------------------- WARNING --------------------\n" +
                    "ADVANCED CONFIG OPTIONS ARE ENABLED.\n" +
                    "THIS ALLOWS TO MODIFY THE SERVER TICKRATE.\n" +
                    "THIS IS NOT SUPPORTED AND MAY LEAD TO ERRORS.\n" +
                    "-------------------------------------------------\n");
            if(waitTime < 10) {
                waitTime = 10;
            }
        }

        boolean defaultConfigValues = false;
        defaultConfigValues = Boolean.parseBoolean(startArguments.get("defaultConfigValues"));
        if(defaultConfigValues) {
            System.out.println("Starting with default config values");
        }

        System.out.println("Starting server in " + waitTime + " seconds...");
        try {
            TimeUnit.SECONDS.sleep(waitTime);
        } catch(Exception ignored) {}

        new SlakeoverflowServer(advancedOptions, defaultConfigValues);
    }

    public static SlakeoverflowServer getServer() {
        return server;
    }

    private static String getASCIISignature() {
        return "\n" +
                "  _____ _               _  ________ ______      ________ _____  ______ _      ______          __\n" +
                " / ____| |        /\\   | |/ /  ____/ __ \\ \\    / /  ____|  __ \\|  ____| |    / __ \\ \\        / /\n" +
                "| (___ | |       /  \\  | ' /| |__ | |  | \\ \\  / /| |__  | |__) | |__  | |   | |  | \\ \\  /\\  / / \n" +
                " \\___ \\| |      / /\\ \\ |  < |  __|| |  | |\\ \\/ / |  __| |  _  /|  __| | |   | |  | |\\ \\/  \\/ /  \n" +
                " ____) | |____ / ____ \\| . \\| |___| |__| | \\  /  | |____| | \\ \\| |    | |___| |__| | \\  /\\  /   \n" +
                "|_____/|______/_/    \\_\\_|\\_\\______\\____/   \\/   |______|_|  \\_\\_|    |______\\____/   \\/  \\/    \n" +
                "  _____                          \n" +
                " / ____|                         \n" +
                "| (___   ___ _ ____   _____ _ __ \n" +
                " \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\n" +
                " ____) |  __/ |   \\ V /  __/ |   \n" +
                "|_____/ \\___|_|    \\_/ \\___|_|   \n" +
                "\n" +
                "Slakeoverflow Server by Q11-Hackermans (https://github.com/Q11Hackermans)\n";
    }
}
