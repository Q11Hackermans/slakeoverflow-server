package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountSystem;
import com.github.q11hackermans.slakeoverflow_server.chat.ChatSystem;
import com.github.q11hackermans.slakeoverflow_server.config.ConfigManager;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.console.ConsoleLogger;
import com.github.q11hackermans.slakeoverflow_server.console.ServerConsole;
import com.github.q11hackermans.slakeoverflow_server.constants.AccountPermissionLevel;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import com.github.q11hackermans.slakeoverflow_server.data.SnakeData;
import com.github.q11hackermans.slakeoverflow_server.game.Item;
import com.github.q11hackermans.slakeoverflow_server.shop.ShopManager;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.CMSServer;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOManager;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOStreamType;
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
    // ACCOUNT SYSTEM
    private final AccountSystem accountSystem;
    // CHAT
    private final ChatSystem chatSystem;
    // SHOP
    private final ShopManager shopManager;
    // MANAGER THREADS
    private Thread managerThread;
    private Thread managerUtilsThread;
    private Thread timesThread;
    // TICK THREAD
    private Thread tickThread;
    private final int tickSpeed;
    private final int idleTickSpeed;
    private int tickThreadCounter;
    private int tickThreadState;
    // GAME SESSION
    private int gameState;
    private GameSession game;
    private int manualTicks;
    // PLAYER MANAGEMENT
    private final List<ServerConnection> connectionList;
    // LISTS
    private final List<InetAddress> ipBlacklist;
    private boolean alreadyStopping;
    // STATS
    private int tickRate;


    public SlakeoverflowServer(boolean advancedConfigOptions, boolean defaultConfigValues) throws IOException {
        // SET SERVER (RUN ALWAYS FIRST)
        server = this;

        // WELCOME MESSAGE
        System.out.println(getASCIISignature());

        // CONSOLE
        this.logger = new ConsoleLogger(this);
        this.logger.info("INIT", "Server init");
        this.console = new ServerConsole(this, this.logger);
        this.console.start();

        // CONFIG
        this.configManager = new ConfigManager(this, advancedConfigOptions, !defaultConfigValues);
        if (!defaultConfigValues) {
            this.tickSpeed = this.configManager.getConfig().getCustomServerTickrate();
            this.idleTickSpeed = this.configManager.getConfig().getCustomServerTickrateIdle();
        } else {
            this.tickSpeed = 50;
            this.idleTickSpeed = 950;
        }


        // CONNECTION MANAGER
        this.connectionhandler = new CMSServer(this.configManager.getConfig().getPort());
        this.connectionhandler.addListener(new EventListener(this));
        this.connectionhandler.addGlobalListener(new EventListener(this));
        this.dataIOManager = new DataIOManager(this.connectionhandler, DataIOType.UTF, DataIOStreamType.MULTI_STREAM_HANDLER_CONSUMING);
        this.dataIOManager.addEventListener(new EventListener(this));

        // ACCOUNT SYSTEM
        this.accountSystem = new AccountSystem(this);

        // GAME SESSION
        this.gameState = GameState.STOPPED;
        this.game = null;
        this.manualTicks = 0;

        // PLAYER MANAGEMENT
        this.connectionList = Collections.synchronizedList(new ArrayList<>());

        // LIST
        this.ipBlacklist = new ArrayList<>();

        // CHAT SYSTEM
        this.chatSystem = new ChatSystem(this);

        // SHOP SYSTEM
        this.shopManager = new ShopManager(this);

        // MISC
        this.alreadyStopping = false;
        this.tickRate = 0;

        // THREADS
        this.tickThreadCounter = 20;
        this.tickThreadState = 0;

        this.managerThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
            while (!Thread.currentThread().isInterrupted() && this.managerThread == Thread.currentThread()) {
                try {
                    if (!alreadyStopping) {
                        checkThreads();
                        checkConnectionManager();
                        checkConnections();
                        checkGameSession();

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ignored) {
                        }
                    } else {
                        try {
                            Thread.sleep(3000);
                        } catch (Exception ignored) {
                        }
                        stop();
                    }
                } catch (Exception e) {
                    try {
                        this.logger.warning("MANAGER", "EXCEPTION: " + e.toString() + ": " + Arrays.toString(e.getStackTrace()) + " (THIS EXCEPTION IS THE CAUSE FOR STOPPING THE SERVER)");
                        e.printStackTrace();
                    } catch (Exception ignored) {
                    }
                    stop();
                }
            }
            if (!alreadyStopping) {
                stop();
            }
        });
        this.managerThread.setName("SLAKEOVERFLOW-MANAGER-" + this.toString());
        this.managerThread.start();
        this.logger.debug("INIT", "Started Thread MANAGER");

        this.tickThread = this.getTickThreadTemplate();
        this.tickThread.start();
        this.logger.debug("INIT", "Started Thread TICK");

        this.timesThread = this.getTimesThreadTemplate();
        this.timesThread.start();
        this.logger.debug("INIT", "Started Thread TIMES");

        // FINISHED (RUN ALWAYS LAST)
        this.logger.info("INIT", "Setup complete");
    }

    // SERVER MANAGEMENT

    /**
     * This will stop the server.
     */
    public void stop() {
        try {
            new Thread(() -> {
                try {
                    this.alreadyStopping = true;
                    if (!this.managerThread.isInterrupted()) {
                        this.managerThread.interrupt();
                    }
                    if (this.tickThread != null && !this.tickThread.isInterrupted()) {
                        this.tickThread.interrupt();
                    }
                    if (!this.dataIOManager.isClosed()) {
                        this.dataIOManager.close();
                    }
                    if (!this.connectionhandler.isClosed()) {
                        this.connectionhandler.close();
                    }
                    if (this.console.isRunning()) {
                        this.console.stop();
                    }
                    if (!this.alreadyStopping) {
                        this.logger.info("STOP", "Server shutdown.");
                        this.logger.saveLog(new File(System.getProperty("user.dir"), "log.json"), true);
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }).start();
        } catch (Exception ignored) {
        }

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (Exception ignored) {
            }

            System.exit(0);
        }).start();
    }

    // GAME MANAGEMENT

    /**
     * This will setup a fully custom game
     *
     * @param paused          If the game should be started paused or running
     * @param sizeX           game field size X
     * @param sizeY           game field size Y
     * @param fovsizeX        fov size X
     * @param fovsizeY        fov size Y
     * @param nextItemDespawn next item despawn (useless because the despawn count will be set to 20 after it reaches 0, so it's only for savegames)
     * @param snakeDataList   list of snake data
     * @param itemList        list of items
     * @return returns true if the game was successfully set up
     */
    public boolean setupGame(boolean paused, int sizeX, int sizeY, int fovsizeX, int fovsizeY, int nextItemDespawn, List<SnakeData> snakeDataList, List<Item> itemList) {
        if (this.gameState == GameState.STOPPED && sizeX > 10 && sizeY > 10 && fovsizeX > 10 && fovsizeY > 10) {
            this.gameState = GameState.PREPARING;
            this.game = new GameSession(this, sizeX, sizeY, fovsizeX, fovsizeY, nextItemDespawn, snakeDataList, itemList);

            if (paused) {
                this.gameState = GameState.PAUSED;
            } else {
                this.gameState = GameState.RUNNING;
            }

            int snakeCount = 0;
            int itemCount = 0;
            if (snakeDataList != null) {
                snakeCount = snakeDataList.size();
            }
            if (itemList != null) {
                itemCount = itemList.size();
            }

            this.logger.info("GAME", "Started game (PAUSED=" + paused + " SIZEX=" + sizeX + " SIZEY=" + sizeY + " FOVX=" + fovsizeX + " FOVY=" + fovsizeY + " NEXTITEMDESPAWN=" + nextItemDespawn + " SNAKES=" + snakeCount + " ITEMS=" + itemCount + ")");
            return true;
        } else {
            return false;
        }
    }

    /**
     * This will create a new game with a specific size
     */
    public boolean setupGame(int sizeX, int sizeY, boolean paused) {
        return this.setupGame(paused, sizeX, sizeY, 60, 40, 20, null, null);
    }

    /**
     * Setup game automatically
     */
    public boolean setupGameAutomatically() {
        double x = 3;
        return this.setupGame((int) Math.round(50 + (sqrt(((pow(x, 2) * 10) / ((3 * x) + (4 * (x / 6))))) * x * 9)), (int) ((Math.round(50 + (sqrt(((pow(x, 2) * 10) / ((3 * x) + (4 * (x / 6))))) * x * 9)))) / 3, false);
    }

    /**
     * Setup game with default config values
     */
    public boolean setupGameDefault() {
        return this.setupGame(this.configManager.getConfig().getDefaultGameFieldSizeX(), this.configManager.getConfig().getDefaultGameFieldSizeY(), false);
    }

    /**
     * Stop a running game
     */
    public boolean stopGame() {
        boolean success = false;

        if (this.isGameAvail()) {
            success = true;
        }

        this.gameState = GameState.STOPPED;
        this.game = null;

        this.logger.info("GAME", "Stopped game (success=" + success + ")");

        return success;
    }

    /**
     * Pause a currently running game
     *
     * @return true if the game could be paused
     */
    public boolean pauseGame() {
        if (this.gameState == GameState.RUNNING) {
            this.gameState = GameState.PAUSED;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resumes a paused game
     *
     * @return true if the game could be resumed
     */
    public boolean resumeGame() {
        if (this.gameState == GameState.PAUSED) {
            this.gameState = GameState.RUNNING;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add ticks to the manual tick system
     * @param ticks tick count
     */
    public void addManualTicks(int ticks) {
        if (ticks > 0 && ticks <= 120) {
            this.manualTicks += ticks;
        }
    }

    /**
     * Reset the manual ticks count
     */
    public void resetManualTicks() {
        this.manualTicks = 0;
    }

    // CONNECTION MANAGEMENT

    /**
     * Check if a connection with a specific ConnectionManager UUID exists
     *
     * @param uuid Unique Connection ID
     * @return boolean
     */
    public boolean containsConnection(UUID uuid) {
        for (ServerConnection connection : this.connectionList) {
            if (connection.getClientId().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a connection with a specific ConnectionManager UUID.
     *
     * @param uuid ConnectionManager UUID
     * @return ServerConnection (if exists), null (if not exists)
     */
    public ServerConnection getConnectionByUUID(UUID uuid) {
        for (ServerConnection connection : this.connectionList) {
            if (connection.getClientId().equals(uuid)) {
                return connection;
            }
        }
        return null;
    }

    /**
     * Get a connection with a specific account id
     * @param id Account ID
     * @return List of ServerConnections
     */
    public List<ServerConnection> getConnectionsByAccountId(long id) {
        List<ServerConnection> returnList = new ArrayList<>();

        for(ServerConnection connection : this.getConnectionList()) {
            if(connection.getAccountId() == id) {
                returnList.add(connection);
            }
        }

        return List.copyOf(returnList);
    }

    /**
     * Get a count of all connections
     * @return connection count
     */
    public int getConnectionCount() {
        return this.getConnectionList().size();
    }

    /**
     * Get count of all connections which are authenticated as players
     * @return player count
     */
    public int getPlayerCount() {
        int connectionCount = 0;
        for (ServerConnection connection : this.getConnectionList()) {
            if (connection.getAuthenticationState() == AuthenticationState.PLAYER) {
                connectionCount++;
            }
        }
        return connectionCount;
    }

    /**
     * Get count of all connection which are authenticated as spectators
     * @return spectator count
     */
    public int getSpectatorCount() {
        int connectionCount = 0;
        for (ServerConnection connection : this.getConnectionList()) {
            if (connection.getAuthenticationState() == AuthenticationState.SPECTATOR) {
                connectionCount++;
            }
        }
        return connectionCount;
    }

    /**
     * Authenticate a connection as player.
     * This method checks also if the connection is allowed to authenticate.
     * To force-authenticate the connection (disable auth allowed checks), set ignoreConnectionConditions to true.
     * @param connectionUUID UUID of the connection
     * @param ignoreConnectionConditions Force-authenticate connection
     */
    public void authenticateConnectionAsPlayer(UUID connectionUUID, boolean ignoreConnectionConditions) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);

        if (connection != null) {
            if (ignoreConnectionConditions || this.getAuthenticationCondition(connection, (this.getPlayerCount() < this.configManager.getConfig().getMaxPlayers()))) {
                connection.authenticateAsPlayer();
            }
        }
    }

    /**
     * Authenticate a connection as spectator.
     * This method checks also if the connection is allowed to authenticate.
     * To force-authenticate the connection (disable auth allowed checks), set ignoreConnectionConditions to true.
     * @param connectionUUID UUID of the connection
     * @param ignoreConnectionConditions Force-authenticate connection
     */
    public void authenticateConnectionAsSpectator(UUID connectionUUID, boolean ignoreConnectionConditions) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);
        if (connection != null) {
            if (ignoreConnectionConditions || this.getAuthenticationCondition(connection, (this.getSpectatorCount() < this.configManager.getConfig().getMaxSpectators()))) {
                connection.authenticateAsSpectator();
            }
        }
    }

    /**
     * Unauthenticate connection.
     * This method checks also if the connection is allowed to unauthenticate.
     * To force-unauthenticate the connection (disable auth allowed checks), set ignoreConnectionConditions to true.
     * @param connectionUUID UUID of the connection
     * @param ignoreConnectionConditions Force-unauthenticate connection
     */
    public void unauthenticateConnection(UUID connectionUUID, boolean ignoreConnectionConditions) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);
        if (connection != null) {
            if (ignoreConnectionConditions || this.configManager.getConfig().isUserAuthentication()) {
                connection.unauthenticate();
            }
        }
    }

    /**
     * Login a connection into a specific account.
     * This method also checks if the connection is allowed to login.
     * To force-login the connection, set ignoreLoginConditions to true.
     * @param connectionUUID Connection UUID
     * @param accountID account ID
     * @param ignoreLoginConditions force-login connection
     * @return success
     */
    public boolean loginConnection(UUID connectionUUID, long accountID, boolean ignoreLoginConditions) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);
        AccountData account = this.accountSystem.getAccount(accountID);

        if(connection != null && account != null) {
            if(ignoreLoginConditions || this.configManager.getConfig().isAllowLogin() || (!this.configManager.getConfig().isAllowLogin() && this.configManager.getConfig().isAlsoDisablePrivilegedLogin() && (account.getPermissionLevel() == AccountPermissionLevel.MODERATOR || account.getPermissionLevel() == AccountPermissionLevel.ADMIN))) {
                connection.login(account.getId());
                return true;
            }
        }

        return false;
    }

    /**
     * Login a connection only if the password matches
     * @param connectionUUID connection UUID
     * @param accountID account ID
     * @param password password (not the hash)
     * @param ignoreLoginConditions ignoreloginconditions
     * @return success
     */
    public boolean loginConnection(UUID connectionUUID, long accountID, String password, boolean ignoreLoginConditions) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);
        AccountData account = this.accountSystem.getAccount(accountID);

        if(connection != null && account != null) {
            if(ignoreLoginConditions || this.configManager.getConfig().isAllowLogin() || (!this.configManager.getConfig().isAllowLogin() && this.configManager.getConfig().isAlsoDisablePrivilegedLogin() && (account.getPermissionLevel() == AccountPermissionLevel.MODERATOR || account.getPermissionLevel() == AccountPermissionLevel.ADMIN))) {
                if(account.getPassword().equalsIgnoreCase(this.getAccountSystem().getPasswordHashValue(password))) {
                    connection.login(account.getId());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Logout a connection.
     * This method also checks if the connection is allowed to logout.
     * To force-logout the connection, set ignoreLoginConditions to true.
     * @param connectionUUID Connection UUID
     * @param ignoreLoginConditions force-logout connection
     * @return success
     */
    public boolean logoutConnection(UUID connectionUUID, boolean ignoreLoginConditions) {
        ServerConnection connection = this.getConnectionByUUID(connectionUUID);

        if(connection != null) {
            AccountData account = this.accountSystem.getAccount(connection.getAccountId());

            if(account != null) {
                if(ignoreLoginConditions || this.configManager.getConfig().isAllowLogin() || (!this.configManager.getConfig().isAllowLogin() && this.configManager.getConfig().isAlsoDisablePrivilegedLogin() && (account.getPermissionLevel() == AccountPermissionLevel.MODERATOR || account.getPermissionLevel() == AccountPermissionLevel.ADMIN))) {
                    connection.logout();
                    return true;
                }
            } else {
                connection.logout();
                return false; // false is returned because the user was not logged in before, but user will be logged out for safety reasons
            }

        }

        return false;
    }

    /**
     * Register an account and login the connection into this account.
     * This method also checks if registration is enabled/disabled.
     * To ignore this and register anyway, set ignoreRegistrationConditions to true.
     * @param connectionUUID Connection UUID
     * @param username Username
     * @param password Password (unhashed)
     * @param ignoreRegistrationConditions force-register account
     * @return success
     */
    public boolean registerAccount(UUID connectionUUID, String username, String password, boolean ignoreRegistrationConditions) {
        if(ignoreRegistrationConditions || this.configManager.getConfig().isAllowRegistration()) {
            long userid = this.accountSystem.createAccount(username, password);

            if(userid > 0 && this.getConnectionByUUID(connectionUUID) != null) {
                this.loginConnection(connectionUUID, userid, false);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // PRIVATE METHODS

    /**
     * Method for the manager thread to check the ConnectionManager.
     * Server will stop if the CMSServer is closed or null.
     */
    private void checkConnectionManager() {
        if (this.connectionhandler == null || this.connectionhandler.isClosed()) {
            this.getLogger().warning("MANAGER", "Connection manager is closed. Stopping server.");
            this.stop();
        }
    }

    /**
     * Method for the manager thread to check other Threads.
     * If a thread stops, this method will restart it.
     */
    private void checkThreads() {
        if (this.tickThread == null || !this.tickThread.isAlive()) {
            if (this.managerUtilsThread != null && this.managerUtilsThread.isAlive()) {
                return;
            }
            this.managerUtilsThread = new Thread(() -> {
                this.logger.warning("MANAGER", "TICK Thread stopped\n" +
                        "-------------------- TICK THREAD STOPPED --------------------\n" +
                        "TICK Thread was stopped during operation.\n" +
                        "This could have happened due to an exception.\n" +
                        "If this happens more often, please shut down the server.\n" +
                        "The TICK Thread will be restarted in 30 seconds...\n" +
                        "-------------------------------------------------------------\n");

                try {
                    Thread.sleep(30000);
                } catch (Exception ignored) {
                }

                this.tickThread = this.getTickThreadTemplate();
                this.tickThread.start();
                this.logger.warning("MANAGER", "Restarting Thread TICK");
            });
            this.managerUtilsThread.start();
        }
        if (this.timesThread == null || !this.timesThread.isAlive()) {
            this.timesThread = this.getTimesThreadTemplate();
            this.timesThread.start();
            this.logger.warning("MANAGER", "Restarting Thread TIMES");
        }
    }

    /**
     * Method for the manager thread to sync connections with the CMSServer.
     * Adds new CMSClients of the server as ServerConnections and removes disconnected ServerConnections.
     * Unauthenticates connections if no game is running.
     * Check if connections are logged in into an account that does not existsand log them out.
     */
    private void checkConnections() {
        //this.connectionList.removeIf(serverConnection -> serverConnection.getDataIOStreamHandler() == null);
        //this.connectionList.removeIf(serverConnection -> serverConnection.getDataIOStreamHandler().isClosed());
        this.connectionList.removeIf(serverConnection -> serverConnection.getClient() == null);
        this.connectionList.removeIf(serverConnection -> serverConnection.getClient().isClosed());

        for (CMSClient cmsClient : this.connectionhandler.getClientList()) {
            if (!this.containsConnection(cmsClient.getUniqueId())) {
                this.connectionList.add(new ServerConnection(this, cmsClient.getUniqueId()));
            }
        }

        for (ServerConnection connection : this.connectionList) {
            if (!this.isGameAvail() && (connection.getAuthenticationState() == AuthenticationState.PLAYER || connection.getAuthenticationState() == AuthenticationState.SPECTATOR)) {
                connection.unauthenticate();
            } else if (this.isGameAvail() && !this.configManager.getConfig().isEnableSpectator() && connection.getAuthenticationState() == AuthenticationState.SPECTATOR) {
                connection.unauthenticate();
            } else if (connection.isBanned() && (connection.getAuthenticationState() != AuthenticationState.UNAUTHENTICATED)) {
                connection.unauthenticate();
            }

            if(connection.getAccountId() != -1) {
                AccountData account = this.accountSystem.getAccount(connection.getAccountId());

                if(account == null) {
                    connection.logout();
                }
            }
        }
    }

    /**
     * Method for the manager thread to check the game session and game state
     */
    private void checkGameSession() {
        if ((this.game == null) || (this.gameState == GameState.STOPPED)) {
            if (this.game == null && this.gameState != GameState.STOPPED && this.gameState != GameState.PREPARING) {
                this.gameState = GameState.STOPPED;
            }
            if (this.gameState == GameState.STOPPED && this.game != null) {
                this.game = null;
            }
        }
    }

    /**
     * Method for tick thread to send status messages to all connections.
     */
    private void sendStatusMessage() {
        synchronized(this.connectionList) {
            for (ServerConnection connection : this.connectionList) {
                JSONObject statusMessage = new JSONObject();
                statusMessage.put("cmd", "status");
                statusMessage.put("status", this.gameState);
                statusMessage.put("auth", connection.getAuthenticationState());
                AccountData account = connection.getAccount();
                if(account != null) {
                    statusMessage.put("account", account.getId());
                } else {
                    statusMessage.put("account", -1);
                }

                connection.sendUTF(statusMessage.toString());
            }
        }
    }

    /**
     * Method for the authentication methods to check if a connection is allowed to authenticate or not
     * @param connection connection
     * @param customCondition own condition (e.g. slot condition)
     * @return true = allowed, false = not allowed
     */
    private boolean getAuthenticationCondition(ServerConnection connection, boolean customCondition) {
        if(connection != null) {
            AccountData account = connection.getAccount();

            boolean isBanned = connection.isBanned();
            boolean isUserAuthentication = this.getConfigManager().getConfig().isUserAuthentication();
            boolean isAllowGuests = this.getConfigManager().getConfig().isAllowGuests();
            boolean isUser;
            boolean isAdmin;
            if(account != null) {
                isUser = true;

                if(account.getPermissionLevel() == AccountPermissionLevel.MODERATOR || account.getPermissionLevel() == AccountPermissionLevel.ADMIN) {
                    isAdmin = true;
                } else {
                    isAdmin = false;
                }
            } else {
                isUser = false;
                isAdmin = false;
            }

            // CONDITIONS

            if(isAdmin) {
                return true;
            }

            if(isBanned) {
                return false;
            }

            if(isUserAuthentication && isAllowGuests && customCondition) {
                return true;
            }

            if(isUserAuthentication && isUser && customCondition) {
                return true;
            }
        }

        return false;
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

    public int getTickThreadCounter() {
        return this.tickThreadCounter;
    }

    public boolean isManagerThreadAlive() {
        return this.managerThread.isAlive();
    }

    public Thread.State getManagerThreadState() {
        return this.managerThread.getState();
    }

    public boolean isTickThreadAlive() {
        return this.tickThread.isAlive();
    }

    public Thread.State getTickThreadState() {
        return this.tickThread.getState();
    }

    public boolean isTimesThreadAlive() {
        return this.timesThread.isAlive();
    }

    public Thread.State getTimesThreadState() {
        return this.timesThread.getState();
    }

    public int getTickRate() {
        return this.tickRate;
    }

    public int getManualTicks() {
        return this.manualTicks;
    }

    public AccountSystem getAccountSystem() {
        return this.accountSystem;
    }

    public int getTickSpeed() {
        return this.tickSpeed;
    }

    public int getIdleTickSpeed() {
        return this.idleTickSpeed;
    }

    public ChatSystem getChatSystem() {
        return this.chatSystem;
    }

    public ShopManager getShopManager() {
        return this.shopManager;
    }

    // THREAD TEMPLATES

    /**
     * Tick thread template.
     * @return tick thread
     */
    private Thread getTickThreadTemplate() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && this.tickThread == Thread.currentThread()) {
                try {
                    this.tickRate = this.tickThreadCounter;
                    this.tickThreadCounter = 20;
                    this.tickThreadState = 0;
                    this.sendStatusMessage();
                    if (this.game != null && (gameState == GameState.RUNNING || (gameState == GameState.PAUSED && manualTicks > 0))) {
                        this.game.tick();

                        if (manualTicks > 0) {
                            manualTicks--;
                        }
                    } else {
                        if (gameState != GameState.RUNNING && gameState != GameState.PAUSED) {
                            manualTicks = 0;
                        }

                        Thread.sleep(this.idleTickSpeed);
                    }
                    Thread.sleep(this.tickSpeed);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    if (!(e instanceof InterruptedException)) {
                        this.logger.warning("TICK", "EXCEPTION: " + e.toString() + ": " + Arrays.toString(e.getStackTrace()));
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setName("SLAKEOVERFLOW-TICK-" + this.toString());
        return thread;
    }

    /**
     * Times thread template
     * @return times thread
     */
    private Thread getTimesThreadTemplate() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && this.timesThread == Thread.currentThread()) {
                try {
                    if (this.tickThread.isAlive()) {
                        int waittime = -40;
                        if (this.game != null && gameState == GameState.RUNNING) {
                            waittime = waittime - 840;
                        }
                        if (this.tickThreadCounter > waittime) {
                            this.tickThreadCounter--;
                        } else {
                            if (this.tickThreadState == 0) {
                                this.tickThread.interrupt();
                                this.tickRate = this.tickThreadCounter;
                                this.tickThreadCounter = 20;
                                this.tickThreadState = 1;
                                this.logger.warning("TIMES", "TICK Thread not responding (" + (this.tickThreadState) + "/3). Interrupting...");
                            } else if (this.tickThreadState == 1) {
                                this.tickThread.interrupt();
                                this.tickThread.stop();
                                this.tickRate = this.tickThreadCounter;
                                this.tickThreadCounter = 20;
                                this.tickThreadState = 2;
                                this.logger.warning("TIMES", "TICK Thread not responding (" + (this.tickThreadState) + "/3). Killing...");
                            } else {
                                try {
                                    this.logger.warning("TIMES", "TICK Thread not responding (" + (this.tickThreadState + 1) + "/3). Server shutdown...\n" +
                                            "-------------------- TICK THREAD NOT RESPONDING --------------------\n" +
                                            "The tick thread does not respond.\n" +
                                            "All attempts to stop and restart it have failed.\n" +
                                            "The server is now shutting down.\n" +
                                            "--------------------------------------------------------------------");
                                } catch (Exception ignored) {
                                }
                                stop();
                            }
                        }
                    }

                    Thread.sleep(this.tickSpeed);
                    if (!(this.game != null && gameState == GameState.RUNNING)) {
                        Thread.sleep(this.idleTickSpeed);
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    if (!(e instanceof InterruptedException)) {
                        this.logger.warning("TIMES", "EXCEPTION: " + e.toString() + ": " + Arrays.toString(e.getStackTrace()));
                    }
                }
            }
        });
        thread.setName("SLAKEOVERFLOW-TIMES-" + thread.toString());
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
            for (String arg : args) {
                if (arg.startsWith("-")) {
                    arg = arg.replace("-", "");
                    try {
                        String[] argument = arg.split("=");
                        startArguments.put(argument[0], argument[1]);
                    } catch (Exception e) {
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
        if (advancedOptions) {
            System.out.println("\n" +
                    "-------------------- WARNING --------------------\n" +
                    "ADVANCED CONFIG OPTIONS ARE ENABLED.\n" +
                    "THIS ALLOWS TO MODIFY THE SERVER TICKRATE.\n" +
                    "THIS IS NOT SUPPORTED AND MAY LEAD TO ERRORS.\n" +
                    "-------------------------------------------------\n");
            if (waitTime < 10) {
                waitTime = 10;
            }
        }

        boolean defaultConfigValues = false;
        defaultConfigValues = Boolean.parseBoolean(startArguments.get("defaultConfigValues"));
        if (defaultConfigValues) {
            System.out.println("Starting with default config values");
        }

        System.out.println("Starting server in " + waitTime + " seconds...");
        try {
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (Exception ignored) {
        }

        new SlakeoverflowServer(advancedOptions, defaultConfigValues);
    }

    /**
     * This is a way to globally get the server instance.
     * THIS IS NOT SUPPORTED ANYMORE!
     * @return SlakeoverflowServer
     * @deprecated DO NOT USE THIS ANYMORE!
     */
    @Deprecated
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
