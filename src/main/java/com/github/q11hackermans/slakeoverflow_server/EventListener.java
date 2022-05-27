package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.config.ServerConfig;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import com.github.q11hackermans.slakeoverflow_server.constants.Direction;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import com.github.q11hackermans.slakeoverflow_server.game.Snake;
import net.jandie1505.connectionmanager.CMListenerAdapter;
import net.jandie1505.connectionmanager.enums.PendingClientState;
import net.jandie1505.connectionmanager.events.CMClientClosedEvent;
import net.jandie1505.connectionmanager.events.CMClientCreatedEvent;
import net.jandie1505.connectionmanager.events.CMClientInputStreamByteLimitReachedEvent;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionAcceptedEvent;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionAttemptEvent;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionRefusedEvent;
import net.jandie1505.connectionmanager.streams.CMConsumingInputStream;
import net.jandie1505.connectionmanager.streams.CMTimedInputStream;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOStreamHandler;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.events.DataIOUTFReceivedEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EventListener extends CMListenerAdapter {

    // SERVER EVENTS
    @Override
    public void onServerConnectionAttempt(CMSServerConnectionAttemptEvent event) {
        if ((!SlakeoverflowServer.getServer().getIpBlacklist().contains(event.getClient().getSocket().getInetAddress()))) {
            if(SlakeoverflowServer.getServer().getConnectionCount() < SlakeoverflowServer.getServer().getConfigManager().getConfig().getMaxConnections()) {
                if (SlakeoverflowServer.getServer().getConfigManager().getConfig().isAutoConnectionAccept()) {
                    event.getClient().setState(PendingClientState.ACCEPTED);
                } else {
                    event.getClient().setTime(10000);
                    SlakeoverflowServer.getServer().getLogger().info("CONNECTION", "Connection request from " + event.getClient().getSocket().getInetAddress() + " (" + event.getUuid() + ") (10 seconds to accept)");
                }
            } else {
                event.getClient().setTime(5000);
                SlakeoverflowServer.getServer().getLogger().info("CONNECTION", "Connection request from " + event.getClient().getSocket().getInetAddress() + " (" + event.getUuid() + ") (Server full, 5 seconds to accept)");
            }
        } else {
            event.getClient().setState(PendingClientState.DENIED);
        }
    }

    @Override
    public void onServerConnectionAccept(CMSServerConnectionAcceptedEvent event) {
        SlakeoverflowServer.getServer().getLogger().info("CONNECTION", "Connection " + event.getClient().getUniqueId() + " (" + event.getClient().getIP() + ") accepted");
    }

    @Override
    public void onServerConnectionRefused(CMSServerConnectionRefusedEvent event) {
        SlakeoverflowServer.getServer().getLogger().info("CONNECTION", "Connection " + event.getUuid() + " (" + event.getClient().getSocket().getInetAddress() + ") refused");
    }

    // CLIENT EVENTS
    @Override
    public void onClientCreated(CMClientCreatedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        cmsClient.getInputStream().setStreamByteLimit(10000000);

        int count = 3;
        while ((SlakeoverflowServer.getServer().getDataIOManager().getHandlerByClientUUID(cmsClient.getUniqueId()) == null)) {
            try {
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            count--;
            if (count <= 0) {
                event.getClient().close();
                return;
            }
        }

        JSONObject readyMessage = new JSONObject();
        readyMessage.put("cmd", "server_ready");

        DataIOStreamHandler dataIOStreamHandler = SlakeoverflowServer.getServer().getDataIOManager().getHandlerByClientUUID(cmsClient.getUniqueId());
        try {
            dataIOStreamHandler.writeUTF(readyMessage.toString());
        } catch (IOException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONNECTION", "Error while sending data to " + cmsClient.getUniqueId());
            cmsClient.close();
        }
    }

    @Override
    public void onClientClosed(CMClientClosedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        SlakeoverflowServer.getServer().getLogger().info("CONNECTION", "Client " + cmsClient.getUniqueId() + " (" + cmsClient.getIP() + ") disconnected with reason " + event.getReason());
    }

    @Override
    public void onClientInputStreamByteLimitReached(CMClientInputStreamByteLimitReachedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        if (event.getStream() instanceof CMTimedInputStream) {
            SlakeoverflowServer.getServer().getLogger().warning("CONNECTION", "InputStream byte limit ( + " + ((CMTimedInputStream) event.getStream()).getStreamByteLimit() + " bytes) of " + cmsClient.getUniqueId() + " was reached. Errors with communication may occur.");
        } else if (event.getStream() instanceof CMConsumingInputStream) {
            SlakeoverflowServer.getServer().getLogger().warning("CONNECTION", "InputStream byte limit ( + " + ((CMTimedInputStream) event.getStream()).getStreamByteLimit() + " bytes) of " + cmsClient.getUniqueId() + " was reached. Errors with communication may occur.");
        }
    }

    // DATA IO EVENTS
    @Override
    public void onUTFReceived(DataIOUTFReceivedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        try {
            JSONObject data = new JSONObject(event.getData());

            if (data.has("cmd")) {
                switch (data.getString("cmd")) {
                    case "auth":
                        if (data.has("type")) {
                            if (data.getInt("type") == AuthenticationState.PLAYER) {
                                if (data.has("username")) {
                                    // CURRENTLY NOT WORKING
                                }
                                SlakeoverflowServer.getServer().authenticateConnectionAsPlayer(cmsClient.getUniqueId(), false);
                            } else if (data.getInt("type") == AuthenticationState.SPECTATOR) {
                                SlakeoverflowServer.getServer().authenticateConnectionAsSpectator(cmsClient.getUniqueId(), false);
                            } else {
                                SlakeoverflowServer.getServer().unauthenticateConnection(cmsClient.getUniqueId(), false);
                            }
                        }
                        break;
                    case "login":
                    {
                        ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(cmsClient.getUniqueId());

                        if(connection != null) {
                            if(data.has("username") && data.has("password")) {
                                AccountData account = SlakeoverflowServer.getServer().getAccountSystem().getAccount(data.getString("username"));

                                if(account != null) {
                                    SlakeoverflowServer.getServer().loginConnection(cmsClient.getUniqueId(), account.getId(), false);
                                }
                            }
                        }

                        break;
                    }
                    case "logout":
                    {
                        ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(cmsClient.getUniqueId());

                        if(connection != null) {
                            SlakeoverflowServer.getServer().logoutConnection(cmsClient.getUniqueId(), false);
                        }

                        break;
                    }
                    case "register":
                    {
                        ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(cmsClient.getUniqueId());

                        if(connection != null) {
                            if(data.has("username") && data.has("password")) {
                                SlakeoverflowServer.getServer().registerAccount(cmsClient.getUniqueId(), data.getString("username"), data.getString("password"), false);
                            }
                        }

                        break;
                    }
                    case "game_direction_change":
                        if (SlakeoverflowServer.getServer().getGameState() == GameState.RUNNING && SlakeoverflowServer.getServer().getGameSession() != null) {
                            if (data.has("direction")) {
                                try {
                                    int sendDirection = data.getInt("direction");

                                    ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(cmsClient.getUniqueId());

                                    if (connection != null && connection.getAuthenticationState() == AuthenticationState.PLAYER) {
                                        Snake snake = SlakeoverflowServer.getServer().getGameSession().getSnakeOfConnection(connection);

                                        if (snake != null) {

                                            if (Direction.isValid(sendDirection)) {
                                                SlakeoverflowServer.getServer().getLogger().debug("EVENTLISTENER", "received snake[" + SlakeoverflowServer.getServer().getGameSession().getSnakeId(SlakeoverflowServer.getServer().getGameSession().getSnakeOfConnection(connection)) + "] direction change to: " + Direction.toString(sendDirection));
                                                snake.setNewFacing(sendDirection, true);
                                            }
                                        }
                                    }
                                } catch (JSONException ignored) {
                                }
                            }
                        }
                    case "get_user_info":
                    {
                        ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(cmsClient.getUniqueId());

                        if(connection != null) {
                            JSONObject response = new JSONObject();

                            response.put("auth", connection.getAuthenticationState());

                            AccountData account = connection.getAccount();
                            if(account != null) {
                                response.put("account_id", account.getId());
                                response.put("account_name", account.getUsername());
                                response.put("account_permission", account.getPermissionLevel());
                            } else {
                                response.put("account_id", -1);
                                response.put("account_name", "");
                                response.put("account_permission", "");
                            }

                            connection.sendUTF(response.toString());
                        }

                        break;
                    }
                    case "get_server_info":
                    {
                        ServerConnection connection = SlakeoverflowServer.getServer().getConnectionByUUID(cmsClient.getUniqueId());

                        if(connection != null) {
                            ServerConfig config = SlakeoverflowServer.getServer().getConfigManager().getConfig();

                            JSONObject response = new JSONObject();

                            JSONObject serverSettings = new JSONObject();
                            serverSettings.put("server_name", "Slakeoverflow-Server");
                            serverSettings.put("user_authentication", config.isUserAuthentication());
                            serverSettings.put("allow_guests", config.isAllowGuests());
                            serverSettings.put("allow_login", config.isAllowLogin());
                            serverSettings.put("also_disable_privileged_login", config.isAlsoDisablePrivilegedLogin());
                            serverSettings.put("allow_registration", config.isAllowRegistration());
                            response.put("server_settings", serverSettings);

                            JSONObject gameSettings = new JSONObject();
                            gameSettings.put("max_players", config.getMaxPlayers());
                            gameSettings.put("max_spectators", config.getMaxSpectators());
                            gameSettings.put("snake_speed_base", config.getSnakeSpeedBase());
                            gameSettings.put("snake_speed_modifier_value", config.getSnakeSpeedModifierValue());
                            gameSettings.put("snake_speed_modifier_bodycount", config.getSnakeSpeedModifierBodycount());
                            gameSettings.put("min_food_value", config.getMinFoodValue());
                            gameSettings.put("max_food_value", config.getMaxFoodValue());
                            gameSettings.put("default_snake_length", config.getDefaultSnakeLength());
                            gameSettings.put("item_default_despawn_time", config.getItemDefaultDespawnTime());
                            gameSettings.put("item_superfood_despawn_time", config.getItemSuperFoodDespawnTime());
                            gameSettings.put("enable_spectator", config.isEnableSpectator());
                            gameSettings.put("spectator_update_interval", config.getSpectatorUpdateInterval());
                            response.put("game_settings", gameSettings);

                            JSONObject advancedSettings = new JSONObject();
                            advancedSettings.put("server_tickrate", SlakeoverflowServer.getServer().getTickSpeed());
                            advancedSettings.put("server_idle_tickrate", SlakeoverflowServer.getServer().getIdleTickSpeed());
                            response.put("advanced_settings", advancedSettings);

                            JSONObject serverStats = new JSONObject();
                            advancedSettings.put("tickrate", SlakeoverflowServer.getServer().getTickRate());
                            advancedSettings.put("connection_count", SlakeoverflowServer.getServer().getConnectionCount());
                            advancedSettings.put("player_count", SlakeoverflowServer.getServer().getPlayerCount());
                            advancedSettings.put("spectator_count", SlakeoverflowServer.getServer().getSpectatorCount());
                            response.put("server_stats", serverStats);

                            connection.sendUTF(response.toString());
                        }

                        break;
                    }
                }
            }
        } catch (JSONException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONNECTION", "Received wrong package format from " + cmsClient.getUniqueId());
            cmsClient.close();
        }
    }
}
