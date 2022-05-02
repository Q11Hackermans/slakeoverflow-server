package com.github.q11hackermans.slakeoverflow_server.connections;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOStreamHandler;

import java.util.UUID;

public class ServerConnection {
    private UUID clientId;

    public ServerConnection(UUID clientId) {
        this.clientId = clientId;
    }

    /**
     * Get the CMSClient from the server's CMSServer
     * @return CMSClient (if available) or null (if not available or an error occurs
     */
    public CMSClient getClient() {
        return SlakeoverflowServer.getServer().getConnectionhandler().getClientById(this.clientId);
    }

    /**
     * Get the CMSClient from the server's CMSServer
     * @return DataIOStreamHandler (if available) or null (if not available or an error occurs
     */
    public DataIOStreamHandler getDataIOStreamHandler() {
        return SlakeoverflowServer.getServer().getDataIOManager().getHandlerByClientUUID(this.clientId);
    }
}
