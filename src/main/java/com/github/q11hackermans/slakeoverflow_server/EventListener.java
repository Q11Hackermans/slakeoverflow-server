package com.github.q11hackermans.slakeoverflow_server;

import net.jandie1505.connectionmanager.CMListenerAdapter;
import net.jandie1505.connectionmanager.enums.PendingClientState;
import net.jandie1505.connectionmanager.server.events.CMSServerConnectionAttemptEvent;

public class EventListener extends CMListenerAdapter {

    @Override
    public void onServerConnectionAttempt(CMSServerConnectionAttemptEvent event) {
        if(!SlakeoverflowServer.getServer().getIpBlacklist().contains(event.getClient().getSocket().getInetAddress())) {
            if(SlakeoverflowServer.getServer().getConfigManager().getConfig().isWhitelist()) {
                event.getClient().setTime(10000);
            } else {
                event.getClient().setState(PendingClientState.ACCEPTED);
            }
        } else {
            event.getClient().setState(PendingClientState.DENIED);
        }
    }
}
