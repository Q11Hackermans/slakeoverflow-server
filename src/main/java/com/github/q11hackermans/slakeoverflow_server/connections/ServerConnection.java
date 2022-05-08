package com.github.q11hackermans.slakeoverflow_server.connections;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.constants.ConnectionType;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOStreamHandler;

import java.util.UUID;

public class ServerConnection {
    private UUID clientId;
    private ConnectionType connectionType;

    public ServerConnection(UUID clientId) {
        this.clientId = clientId;
        this.connectionType = ConnectionType.UNAUTHORIZED;
    }

    // AUTHORISATION
    /**
     * This method authorizes this connection as player
     */
    public void authorizeAsPlayer() {
        this.connectionType = ConnectionType.PLAYER;
    }

    /**
     * This method authorizes this connection as spectator
     */
    public void authorizeAsSpectator() {
        this.connectionType = ConnectionType.SPECTATOR;
    }

    /**
     * This method deauthorizes the connection
     */
    public void deauthorize() {
        this.connectionType = ConnectionType.UNAUTHORIZED;
    }

    // CONNECTION
    public boolean isConnected() {
        return SlakeoverflowServer.getServer().getConnectionList().contains(this);
    }

    // GETTER METHODS
    /**
     * Get the connection type
     * @return ConnectionType
     */
    public ConnectionType getConnectionType() {
        return this.connectionType;
    }

    /**
     * Get UUID of client
     */
    public UUID getClientId() {
        return this.clientId;
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
