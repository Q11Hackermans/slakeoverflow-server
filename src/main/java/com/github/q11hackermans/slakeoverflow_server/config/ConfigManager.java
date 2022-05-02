package com.github.q11hackermans.slakeoverflow_server.config;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class ConfigManager {
    private final File configFile;
    private final ServerConfig config;

    public ConfigManager() {
        this.config = new ServerConfig();
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

                    SlakeoverflowServer.getServer().getLogger().info("CONFIG", "Config loaded");
                } catch(JSONException e) {
                    SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Config file corrupt");
                    this.recreateConfig();
                }
            }
        } catch(IOException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONFIG", "Configuration error. Please check r/w permission for ./config.json. Stopping server.");
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
                config.put("port", 26599);
                config.put("whitelist", true);

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
