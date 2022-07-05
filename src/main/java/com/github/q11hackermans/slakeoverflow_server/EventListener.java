package com.github.q11hackermans.slakeoverflow_server;

import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.config.ServerConfig;
import com.github.q11hackermans.slakeoverflow_server.connections.ServerConnection;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import com.github.q11hackermans.slakeoverflow_server.constants.Direction;
import com.github.q11hackermans.slakeoverflow_server.constants.GameState;
import com.github.q11hackermans.slakeoverflow_server.game.Snake;
import com.github.q11hackermans.slakeoverflow_server.shop.ShopItem;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventListener extends CMListenerAdapter {

    private final SlakeoverflowServer server;

    public EventListener(SlakeoverflowServer server) {
        this.server = server;
    }

    // SERVER EVENTS
    @Override
    public void onServerConnectionAttempt(CMSServerConnectionAttemptEvent event) {
        if ((!this.server.getIpBlacklist().contains(event.getClient().getSocket().getInetAddress()))) {
            if(this.server.getConnectionCount() < this.server.getConfigManager().getConfig().getMaxConnections()) {
                if (this.server.getConfigManager().getConfig().isAutoConnectionAccept()) {
                    event.getClient().setState(PendingClientState.ACCEPTED);
                } else {
                    event.getClient().setTime(10000);
                    this.server.getLogger().info("CONNECTION", "Connection request from " + event.getClient().getSocket().getInetAddress() + " (" + event.getUuid() + ") (10 seconds to accept)");
                }
            } else {
                event.getClient().setTime(5000);
                this.server.getLogger().info("CONNECTION", "Connection request from " + event.getClient().getSocket().getInetAddress() + " (" + event.getUuid() + ") (Server full, 5 seconds to accept)");
            }
        } else {
            event.getClient().setState(PendingClientState.DENIED);
        }
    }

    @Override
    public void onServerConnectionAccept(CMSServerConnectionAcceptedEvent event) {
        this.server.getLogger().info("CONNECTION", "Connection " + event.getClient().getUniqueId() + " (" + event.getClient().getIP() + ") accepted");
    }

    @Override
    public void onServerConnectionRefused(CMSServerConnectionRefusedEvent event) {
        this.server.getLogger().info("CONNECTION", "Connection " + event.getUuid() + " (" + event.getClient().getSocket().getInetAddress() + ") refused");
    }

    // CLIENT EVENTS
    @Override
    public void onClientCreated(CMClientCreatedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        cmsClient.getInputStream().setStreamByteLimit(10000000);

        int count = 3;
        while ((this.server.getDataIOManager().getHandlerByClientUUID(cmsClient.getUniqueId()) == null)) {
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

        DataIOStreamHandler dataIOStreamHandler = this.server.getDataIOManager().getHandlerByClientUUID(cmsClient.getUniqueId());
        try {
            dataIOStreamHandler.writeUTF(readyMessage.toString());
        } catch (IOException e) {
            this.server.getLogger().warning("CONNECTION", "Error while sending data to " + cmsClient.getUniqueId());
            cmsClient.close();
        }
    }

    @Override
    public void onClientClosed(CMClientClosedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        this.server.getLogger().info("CONNECTION", "Client " + cmsClient.getUniqueId() + " (" + cmsClient.getIP() + ") disconnected with reason " + event.getReason());
    }

    @Override
    public void onClientInputStreamByteLimitReached(CMClientInputStreamByteLimitReachedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        if (event.getStream() instanceof CMTimedInputStream) {
            this.server.getLogger().warning("CONNECTION", "InputStream byte limit ( + " + ((CMTimedInputStream) event.getStream()).getStreamByteLimit() + " bytes) of " + cmsClient.getUniqueId() + " was reached. Errors with communication may occur.");
        } else if (event.getStream() instanceof CMConsumingInputStream) {
            this.server.getLogger().warning("CONNECTION", "InputStream byte limit ( + " + ((CMTimedInputStream) event.getStream()).getStreamByteLimit() + " bytes) of " + cmsClient.getUniqueId() + " was reached. Errors with communication may occur.");
        }
    }

    // DATA IO EVENTS
    @Override
    public void onUTFReceived(DataIOUTFReceivedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        try {
            JSONObject data = new JSONObject(event.getData());

            if (data.has("cmd")) {
                ServerConnection connection = this.server.getConnectionByUUID(cmsClient.getUniqueId());

                if(connection != null) {
                    switch (data.getString("cmd")) {
                        case "auth":
                            if (data.has("type")) {
                                if (data.getInt("type") == AuthenticationState.PLAYER) {
                                    this.server.authenticateConnectionAsPlayer(cmsClient.getUniqueId(), false);
                                } else if (data.getInt("type") == AuthenticationState.SPECTATOR) {
                                    this.server.authenticateConnectionAsSpectator(cmsClient.getUniqueId(), false);
                                } else {
                                    this.server.unauthenticateConnection(cmsClient.getUniqueId(), false);
                                }
                            }

                            break;
                        case "login":
                        {
                            if(data.has("username") && data.has("password")) {
                                AccountData account = this.server.getAccountSystem().getAccount(data.getString("username"));

                                if(account != null) {
                                    this.server.loginConnection(cmsClient.getUniqueId(), account.getId(), data.getString("password"), false);
                                }
                            }

                            break;
                        }
                        case "logout":
                        {
                            this.server.logoutConnection(cmsClient.getUniqueId(), false);

                            break;
                        }
                        case "register":
                        {
                            if(data.has("username") && data.has("password")) {
                                this.server.registerAccount(cmsClient.getUniqueId(), data.getString("username"), data.getString("password"), false);
                            }

                            break;
                        }
                        case "game_direction_change":
                            if (this.server.getGameState() == GameState.RUNNING && this.server.getGameSession() != null) {
                                if (data.has("direction")) {
                                    try {
                                        int sendDirection = data.getInt("direction");

                                        if (connection.getAuthenticationState() == AuthenticationState.PLAYER) {
                                            Snake snake = this.server.getGameSession().getSnakeOfConnection(connection);

                                            if (snake != null) {

                                                if (Direction.isValid(sendDirection)) {
                                                    this.server.getLogger().debug("EVENTLISTENER", "received snake[" + this.server.getGameSession().getSnakeId(this.server.getGameSession().getSnakeOfConnection(connection)) + "] direction change to: " + Direction.toString(sendDirection));
                                                    snake.setNewFacing(sendDirection, true);
                                                }
                                            }
                                        }
                                    } catch (JSONException ignored) {
                                    }
                                }
                            }

                            break;
                        case "game_snake_speed_boost":
                            if(this.server.getConfigManager().getConfig().isEnableSnakeSpeedBoost() && this.server.getGameState() == GameState.RUNNING && this.server.getGameSession() != null) {

                                if(connection.getAuthenticationState() == AuthenticationState.PLAYER) {
                                    Snake snake = this.server.getGameSession().getSnakeOfConnection(connection);

                                    if(snake != null) {
                                        this.server.getLogger().debug("EVENTLISTENER", "Received snake speed boost request");
                                        snake.fastMove();
                                    }
                                }
                            }

                            break;
                        case "get_user_info":
                        {
                            JSONObject response = new JSONObject();

                            response.put("cmd", "user_info");

                            response.put("auth", connection.getAuthenticationState());

                            AccountData account = connection.getAccount();
                            if(account != null) {
                                response.put("account_id", account.getId());
                                response.put("account_name", account.getUsername());
                                response.put("account_permission", account.getPermissionLevel());
                                response.put("account_level", account.getLevel());
                                response.put("account_balance", account.getBalance());
                                response.put("shopData", account.getShopData());
                            } else {
                                response.put("account_id", -1);
                                response.put("account_name", "");
                                response.put("account_permission", "");
                                response.put("account_level", 0);
                                response.put("account_balance", 0);
                                response.put("shopData", new JSONArray());
                            }

                            connection.sendUTF(response.toString());

                            break;
                        }
                        case "get_server_info":
                        {
                            ServerConfig config = this.server.getConfigManager().getConfig();

                            JSONObject response = new JSONObject();

                            response.put("cmd", "server_info");

                            JSONObject serverSettings = new JSONObject();
                            serverSettings.put("server_name", config.getServerName());
                            serverSettings.put("user_authentication", config.isUserAuthentication());
                            serverSettings.put("allow_guests", config.isAllowGuests());
                            serverSettings.put("allow_login", config.isAllowLogin());
                            serverSettings.put("also_disable_privileged_login", config.isAlsoDisablePrivilegedLogin());
                            serverSettings.put("allow_registration", config.isAllowRegistration());
                            serverSettings.put("enable_chat", config.isEnableChat());
                            serverSettings.put("allow_guest_chat", config.isAllowGuestChat());
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
                            gameSettings.put("enable_snake_speed_boost", config.isEnableSnakeSpeedBoost());
                            gameSettings.put("eat_own_snake", config.isEatOwnSnake());
                            gameSettings.put("snake_death_superfood_multiplier", config.getSnakeDeathSuperfoodMultiplier());
                            response.put("game_settings", gameSettings);

                            JSONObject advancedSettings = new JSONObject();
                            advancedSettings.put("server_tickrate", this.server.getTickSpeed());
                            advancedSettings.put("server_idle_tickrate", -1);
                            response.put("advanced_settings", advancedSettings);

                            JSONObject serverStats = new JSONObject();
                            serverStats.put("tickrate", this.server.getTickRate());
                            serverSettings.put("connection_count", this.server.getConnectionCount());
                            serverSettings.put("player_count", this.server.getPlayerCount());
                            serverSettings.put("spectator_count", this.server.getSpectatorCount());
                            response.put("server_stats", serverStats);

                            JSONObject shopItems = new JSONObject();
                            Map<Integer, ShopItem> shopItemMap = this.server.getShopManager().getShopItems();
                            for(int id : shopItemMap.keySet()) {
                                ShopItem shopItem = shopItemMap.get(id);

                                JSONObject item = new JSONObject();
                                item.put("enabled", shopItem.isEnabled());
                                item.put("required_level", shopItem.getRequiredLevel());
                                item.put("price", shopItem.getPrice());

                                shopItems.put(String.valueOf(id), item);
                            }
                            response.put("shop_items", shopItems);

                            connection.sendUTF(response.toString());

                            break;
                        }
                        case "chat":
                        {
                            this.server.getChatSystem().onChatEventReceived(connection, data);

                            break;
                        }
                        case "shop_purchase":
                        {
                            AccountData account = connection.getAccount();

                            if(account != null && data.has("item")) {
                                if(this.server.getShopManager().purchaseItem(account.getId(), data.getInt("item"))) {
                                    JSONObject successMessage = new JSONObject();
                                    successMessage.put("cmd", "shop_purchase_success");
                                    successMessage.put("item", data.getInt("item"));
                                    connection.sendUTF(successMessage.toString());
                                } else {
                                    JSONObject successMessage = new JSONObject();
                                    successMessage.put("cmd", "shop_purchase_failure");
                                    connection.sendUTF(successMessage.toString());
                                }
                            }

                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            this.server.getLogger().warning("CONNECTION", "Received wrong package format from " + cmsClient.getUniqueId());
            cmsClient.close();
        }
    }

    public SlakeoverflowServer getServer() {
        return this.server;
    }
}
