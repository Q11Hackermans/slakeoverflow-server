package com.github.q11hackermans.slakeoverflow_server;

import net.jandie1505.connectionmanager.CMListenerAdapter;
import net.jandie1505.connectionmanager.enums.PendingClientState;
import net.jandie1505.connectionmanager.events.CMClientClosedEvent;
import net.jandie1505.connectionmanager.events.CMClientCreatedEvent;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionAcceptedEvent;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionAttemptEvent;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionRefusedEvent;
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
        if(!SlakeoverflowServer.getServer().getIpBlacklist().contains(event.getClient().getSocket().getInetAddress())) {
            if(SlakeoverflowServer.getServer().getConfigManager().getConfig().isWhitelist()) {
                event.getClient().setTime(10000);
                SlakeoverflowServer.getServer().getLogger().info("CONNECTION", "Connection request from " + event.getClient().getSocket().getInetAddress() + " (" + event.getUuid() + ")");
            } else {
                event.getClient().setState(PendingClientState.ACCEPTED);
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

        int count = 3;
        while((SlakeoverflowServer.getServer().getDataIOManager().getHandlerByClientUUID(cmsClient.getUniqueId()) == null)) {
            try {
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException ignored) {}
            count--;
            if(count <= 0) {
                event.getClient().close();
                return;
            }
        }

        JSONObject readyMessage = new JSONObject();
        readyMessage.put("cmd", "ready");

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

    // DATA IO EVENTS
    @Override
    public void onUTFReceived(DataIOUTFReceivedEvent event) {
        CMSClient cmsClient = (CMSClient) event.getClient();

        try {
            JSONObject data = new JSONObject(event.getData());

            if(data.has("cmd")) {
                switch(data.getString("cmd")) {
                    case "auth":
                        if(data.has("type")) {
                            if(data.getString("type").equalsIgnoreCase("player")) {
                                if(data.has("username")) {
                                    // REGISTER PLAYER
                                }
                            } else if(data.getString("type").equalsIgnoreCase("spectator")) {
                                cmsClient.close();
                            }
                        }
                        break;
                }
            }
        } catch(JSONException e) {
            SlakeoverflowServer.getServer().getLogger().warning("CONNECTION", "Received wrong package format from " + cmsClient.getUniqueId());
            cmsClient.close();
        }
    }
}
