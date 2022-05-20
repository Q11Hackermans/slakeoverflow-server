package com.github.q11hackermans.slakeoverflow_server.connections;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.constants.ConnectionType;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOStreamHandler;

import java.io.IOException;
import java.util.UUID;

public class ServerConnection {
    private UUID clientId;
    private int connectionType;

    public ServerConnection(UUID clientId) {
        this.clientId = clientId;
        this.connectionType = ConnectionType.UNAUTHENTICATED;
    }

    // AUTHORISATION
    /**
     * This method authenticates this connection as player
     */
    public void authenticateAsPlayer() {
        this.connectionType = ConnectionType.PLAYER;
        SlakeoverflowServer.getServer().getLogger().info("USERS", "Connection " + this.clientId + " authenticated as player");
    }

    /**
     * This method authenticates this connection as spectator
     */
    public void authenticateAsSpectator() {
        this.connectionType = ConnectionType.SPECTATOR;
        SlakeoverflowServer.getServer().getLogger().info("USERS", "Connection " + this.clientId + " authenticated as spectator");
    }

    /**
     * This method unauthenticate the connection
     */
    public void unauthenticate() {
        this.connectionType = ConnectionType.UNAUTHENTICATED;
        SlakeoverflowServer.getServer().getLogger().info("USERS", "Connection " + this.clientId + " unauthenticated");
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
    public int getConnectionType() {
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

    /**
     * A null safe way to send data to a connection
     * @param text The text to send
     * @return if sending was successful
     */
    public boolean sendUTF(String text) {
        if(this.getDataIOStreamHandler() != null) {
            try {
                this.getDataIOStreamHandler().writeUTF(text);
                return true;
            } catch (IOException e) {
                SlakeoverflowServer.getServer().getLogger().warning("CONNECTION", "Error while sending data to " + this.getClientId());
                this.getClient().close();
                return false;
            }
        } else {
            return false;
        }
    }
}
