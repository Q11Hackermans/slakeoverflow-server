package com.github.q11hackermans.slakeoverflow_server;

import net.jandie1505.connectionmanager.CMListenerAdapter;
import net.jandie1505.connectionmanager.enums.PendingClientState;
import net.jandie1505.connectionmanager.events.CMClientCreatedEvent;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionAcceptedEvent;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionAttemptEvent;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionRefusedEvent;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOManager;

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

        // HIER WIRD READY AN DEN CLIENT GESCHICKT
    }
}
