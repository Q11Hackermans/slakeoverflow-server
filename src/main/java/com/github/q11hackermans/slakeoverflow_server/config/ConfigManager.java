package com.github.q11hackermans.slakeoverflow_server.config;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class ConfigManager {
    private final File configFile;
    private final ServerConfig config;

    public ConfigManager(boolean advancedOptionsEnabled) {
        this.config = new ServerConfig(advancedOptionsEnabled);
        this.configFile = new File(System.getProperty("user.dir"), "config.json");

        if(!this.configFile.exists()) {
            this.recreateConfig();
        } else {
            this.reloadConfig();
        }
    }

    public void reloadConfig() {
        try {
            synchronized(this.configFile) {
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
                    JSONObject jsonConfig = new JSONObject(out);

                    this.config.setPort(jsonConfig.getInt("port"));
                    this.config.setWhitelist(jsonConfig.getBoolean("whitelist"));
                    this.config.setUserAuthentication(jsonConfig.getBoolean("user_authentication"));
                    this.config.setSlots(jsonConfig.getInt("slots"));
                    this.config.setMaxFoodValue(jsonConfig.getInt("min_food_value"));
                    this.config.setMaxFoodValue(jsonConfig.getInt("max_food_value"));
                    this.config.setSnakeSpeedBase(jsonConfig.getInt("snake_speed_base"));
                    this.config.setSnakeSpeedModifierValue(jsonConfig.getInt("snake_speed_modifier_value"));
                    this.config.setSnakeSpeedModifierBodycount(jsonConfig.getInt("snake_speed_modifier_bodycount"));
                    this.config.setDefaultGameFieldSizeX(jsonConfig.getInt("default_gamefield_size_x"));
                    this.config.setDefaultGameFieldSizeY(jsonConfig.getInt("default_gamefield_size_y"));

                    this.config.setOverrideServerTickrate(jsonConfig.getBoolean("advanced_override_server_tickrate"));
                    this.config.setCustomServerTickrate(jsonConfig.getInt("advanced_custom_server_tickrate"));
                    this.config.setCustomServerTickrateIdle(jsonConfig.getInt("advanced_custom_server_tickrate_idle"));

                    SlakeoverflowServer.getServer().getLogger().info("CONFIG", "Config loaded");
                } catch(JSONException e) {
                    SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Config file corrupt");
                    this.recreateConfig();
                }
            }
        } catch(IOException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Configuration error. Please check r/w permission for ./config.json.");
        }
    }

    public void recreateConfig() {
        try {
            synchronized(this.configFile) {
                if(this.configFile.exists()) {
                    this.configFile.delete();
                }
                this.configFile.createNewFile();

                JSONObject config = new JSONObject();
                config.put("port", 26677);
                config.put("whitelist", true);
                config.put("user_authentication", false);
                config.put("min_food_value", 1);
                config.put("max_food_value", 2);
                config.put("snake_speed_base", 20);
                config.put("snake_speed_modifier_value", 1);
                config.put("snake_speed_modifier_bodycount", 2);
                config.put("default_gamefield_size_x", 100);
                config.put("default_gamefield_size_y", 100);
                config.put("advanced_override_server_tickrate", false);
                config.put("advanced_custom_server_tickrate", 50);
                config.put("advanced_custom_server_tickrate_idle", 950);

                FileWriter writer = new FileWriter(this.configFile);
                writer.write(config.toString(4));
                writer.flush();
                writer.close();

                SlakeoverflowServer.getServer().getLogger().info("CONFIG", "Config created");
            }
        } catch(IOException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Configuration error. Please check r/w permission for ./config.json. Stopping server.");
        }
    }

    public ServerConfig getConfig() {
        return this.config;
    }
}
