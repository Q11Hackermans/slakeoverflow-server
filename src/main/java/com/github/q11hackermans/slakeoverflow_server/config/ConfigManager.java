package com.github.q11hackermans.slakeoverflow_server.config;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class ConfigManager {
    private final File configFile;
    private final ServerConfig config;

    public ConfigManager(boolean advancedOptionsEnabled, boolean loadConfigFile) {
        this.config = new ServerConfig(advancedOptionsEnabled);
        this.configFile = new File(System.getProperty("user.dir"), "config.json");

        if (loadConfigFile) {
            if (!this.configFile.exists()) {
                this.recreateConfig();
            } else {
                this.reloadConfig();
            }
        }
    }

    public void reloadConfig() {
        try {
            synchronized (this.configFile) {
                BufferedReader br = new BufferedReader(new FileReader(this.configFile));

                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }

                String out = sb.toString();

                try {
                    JSONObject config = new JSONObject(out);

                    JSONObject serverSettings = config.getJSONObject("server_settings");
                    this.config.setPort(serverSettings.getInt("port"));
                    this.config.setAutoConnectionAccept(serverSettings.getBoolean("auto_connection_accept"));
                    this.config.setMaxConnections(serverSettings.getInt("max_connections"));
                    this.config.setUserAuthentication(serverSettings.getBoolean("user_authentication"));
                    this.config.setUnauthenticatePlayerOnDeath(serverSettings.getBoolean("unauthenticate_player_on_death"));
                    this.config.setPrintDebugMessages(serverSettings.getBoolean("print_debug_messages"));
                    this.config.setAllowGuests(serverSettings.getBoolean("allow_guests"));
                    this.config.setAllowLogin(serverSettings.getBoolean("allow_login"));
                    this.config.setAlsoDisablePrivilegedLogin(serverSettings.getBoolean("also_disable_privileged_login"));
                    this.config.setAllowRegistration(serverSettings.getBoolean("allow_registration"));
                    this.config.setServerName(serverSettings.getString("server_name"));

                    JSONObject gameSettings = config.getJSONObject("game_settings");
                    this.config.setMaxPlayers(gameSettings.getInt("max_players"));
                    this.config.setMaxSpectators(gameSettings.getInt("max_spectators"));
                    this.config.setMaxFoodValue(gameSettings.getInt("min_food_value"));
                    this.config.setMaxFoodValue(gameSettings.getInt("max_food_value"));
                    this.config.setSnakeSpeedBase(gameSettings.getInt("snake_speed_base"));
                    this.config.setSnakeSpeedModifierValue(gameSettings.getInt("snake_speed_modifier_value"));
                    this.config.setSnakeSpeedModifierBodycount(gameSettings.getInt("snake_speed_modifier_bodycount"));
                    this.config.setDefaultGameFieldSizeX(gameSettings.getInt("default_gamefield_size_x"));
                    this.config.setDefaultGameFieldSizeY(gameSettings.getInt("default_gamefield_size_y"));
                    this.config.setItemDefaultDespawnTime(gameSettings.getInt("default_item_despawn_time"));
                    this.config.setItemSuperFoodDespawnTime(gameSettings.getInt("item_superfood_despawn_time"));
                    this.config.setEnableSpectator(gameSettings.getBoolean("enable_spectator"));
                    this.config.setSpectatorUpdateInterval(gameSettings.getInt("spectator_update_interval"));
                    this.config.setEnableSnakeSpeedBoost(gameSettings.getBoolean("enable_snake_speed_boost"));
                    this.config.setEatOwnSnake(gameSettings.getBoolean("eat_own_snake"));
                    this.config.setSnakeDeathSuperfoodMultiplier(gameSettings.getDouble("snake_death_superfood_multiplier"));

                    JSONObject advancedSettings = config.getJSONObject("advanced_settings");
                    this.config.setOverrideServerTickrate(advancedSettings.getBoolean("advanced_override_server_tickrate"));
                    this.config.setCustomServerTickrate(advancedSettings.getInt("advanced_custom_server_tickrate"));
                    this.config.setCustomServerTickrateIdle(advancedSettings.getInt("advanced_custom_server_tickrate_idle"));

                    SlakeoverflowServer.getServer().getLogger().info("CONFIG", "Config loaded");
                } catch (JSONException e) {
                    SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Config file structure corrupt");
                    this.recreateConfig();
                } catch (IllegalArgumentException e) {
                    SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Config file values corrupt");
                    this.recreateConfig();
                }
            }
        } catch (IOException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Configuration error. Please check r/w permission for ./config.json.");
        }
    }

    public void recreateConfig() {
        try {
            synchronized (this.configFile) {
                if (this.configFile.exists()) {
                    this.configFile.delete();
                }
                this.configFile.createNewFile();

                JSONObject config = new JSONObject();

                JSONObject serverSettings = new JSONObject();
                serverSettings.put("port", this.config.getPort());
                serverSettings.put("auto_connection_accept", this.config.isAutoConnectionAccept());
                serverSettings.put("user_authentication", this.config.isUserAuthentication());
                serverSettings.put("max_connections", this.config.getMaxConnections());
                serverSettings.put("unauthenticate_player_on_death", this.config.isUnauthenticatePlayerOnDeath());
                serverSettings.put("print_debug_messages", this.config.isPrintDebugMessages());
                serverSettings.put("allow_guests", this.config.isAllowGuests());
                serverSettings.put("allow_login", this.config.isAllowLogin());
                serverSettings.put("also_disable_privileged_login", this.config.isAlsoDisablePrivilegedLogin());
                serverSettings.put("allow_registration", this.config.isAllowRegistration());
                serverSettings.put("server_name", this.config.getServerName());
                config.put("server_settings", serverSettings);

                JSONObject gameSettings = new JSONObject();
                gameSettings.put("max_players", this.config.getMaxPlayers());
                gameSettings.put("max_spectators", this.config.getMaxSpectators());
                gameSettings.put("min_food_value", this.config.getMinFoodValue());
                gameSettings.put("max_food_value", this.config.getMaxFoodValue());
                gameSettings.put("snake_speed_base", this.config.getSnakeSpeedBase());
                gameSettings.put("snake_speed_modifier_value", this.config.getSnakeSpeedModifierValue());
                gameSettings.put("snake_speed_modifier_bodycount", this.config.getSnakeSpeedModifierBodycount());
                gameSettings.put("default_gamefield_size_x", this.config.getDefaultGameFieldSizeX());
                gameSettings.put("default_gamefield_size_y", this.config.getDefaultGameFieldSizeY());
                gameSettings.put("default_item_despawn_time", this.config.getItemDefaultDespawnTime());
                gameSettings.put("item_superfood_despawn_time", this.config.getItemSuperFoodDespawnTime());
                gameSettings.put("enable_spectator", this.config.isEnableSpectator());
                gameSettings.put("spectator_update_interval", this.config.getSpectatorUpdateInterval());
                gameSettings.put("enable_snake_speed_boost", this.config.isEnableSnakeSpeedBoost());
                gameSettings.put("eat_own_snake", this.config.isEatOwnSnake());
                gameSettings.put("snake_death_superfood_multiplier", this.config.getSnakeDeathSuperfoodMultiplier());
                config.put("game_settings", gameSettings);

                JSONObject advancedSettings = new JSONObject();
                advancedSettings.put("advanced_override_server_tickrate", this.config.isOverrideServerTickrate());
                advancedSettings.put("advanced_custom_server_tickrate", this.config.getCustomServerTickrate());
                advancedSettings.put("advanced_custom_server_tickrate_idle", this.config.getCustomServerTickrateIdle());
                config.put("advanced_settings", advancedSettings);

                FileWriter writer = new FileWriter(this.configFile);
                writer.write(config.toString(4));
                writer.flush();
                writer.close();

                SlakeoverflowServer.getServer().getLogger().info("CONFIG", "Config created");
            }
        } catch (IOException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Configuration error. Please check r/w permission for ./config.json. Stopping server.");
        }
    }

    public ServerConfig getConfig() {
        return this.config;
    }
}
