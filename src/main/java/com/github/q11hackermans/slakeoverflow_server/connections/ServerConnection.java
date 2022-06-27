package com.github.q11hackermans.slakeoverflow_server.connections;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import com.github.q11hackermans.slakeoverflow_server.constants.AccountPermissionLevel;
import com.github.q11hackermans.slakeoverflow_server.constants.AuthenticationState;
import net.jandie1505.connectionmanager.server.CMSClient;
import net.jandie1505.connectionmanager.utilities.dataiostreamhandler.DataIOStreamHandler;

import java.io.IOException;
import java.util.UUID;

public class ServerConnection {

    private final SlakeoverflowServer server;
    private final UUID clientId;
    private int authenticationState;
    private long accountId;
    private boolean muted;
    private boolean banned;
    private boolean socialSpy;

    public ServerConnection(SlakeoverflowServer server, UUID clientId) {
        this.server = server;
        this.clientId = clientId;
        this.authenticationState = AuthenticationState.UNAUTHENTICATED;
        this.accountId = -1;
        this.banned = false;
        this.muted = false;
        this.socialSpy = false;
    }

    // AUTHORISATION

    /**
     * This method authenticates this connection as player
     */
    public void authenticateAsPlayer() {
        this.authenticationState = AuthenticationState.PLAYER;
        this.server.getLogger().info("USERS", "Connection " + this.clientId + " authenticated as player");
    }

    /**
     * This method authenticates this connection as spectator
     */
    public void authenticateAsSpectator() {
        this.authenticationState = AuthenticationState.SPECTATOR;
        this.server.getLogger().info("USERS", "Connection " + this.clientId + " authenticated as spectator");
    }

    /**
     * This method unauthenticate the connection
     */
    public void unauthenticate() {
        this.authenticationState = AuthenticationState.UNAUTHENTICATED;
        this.server.getLogger().info("USERS", "Connection " + this.clientId + " unauthenticated");
    }

    // ACCOUNT SYSTEM

    public void login(long id) {
        if(id > 0) {
            this.accountId = id;
        } else {
            throw new IllegalArgumentException("Invalid account id");
        }
    }

    public void logout() {
        this.accountId = -1;
    }

    // CONNECTION
    public boolean isConnected() {
        return this.server.getConnectionList().contains(this);
    }

    // PUNISHMENTS

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    // SOCIALSPY

    public void setSocialSpy(boolean socialSpy) {
        if(this.getAccount() != null && (this.getAccount().getPermissionLevel() == AccountPermissionLevel.ADMIN || this.getAccount().getPermissionLevel() == AccountPermissionLevel.MODERATOR)) {
            this.socialSpy = socialSpy;
        } else {
            this.socialSpy = false;
        }
    }

    // GETTER METHODS

    /**
     * Get the connection type
     *
     * @return ConnectionType
     */
    public int getAuthenticationState() {
        return this.authenticationState;
    }

    /**
     * Get UUID of client
     */
    public UUID getClientId() {
        return this.clientId;
    }

    /**
     * Get the CMSClient from the server's CMSServer
     *
     * @return CMSClient (if available) or null (if not available or an error occurs
     */
    public CMSClient getClient() {
        return this.server.getConnectionhandler().getClientById(this.clientId);
    }

    /**
     * Get the CMSClient from the server's CMSServer
     *
     * @return DataIOStreamHandler (if available) or null (if not available or an error occurs
     */
    public DataIOStreamHandler getDataIOStreamHandler() {
        return this.server.getDataIOManager().getHandlerByClientUUID(this.clientId);
    }

    /**
     * Returns the account the serverconnection is currently logged in
     * @return AccountData
     */
    public AccountData getAccount() {
        return this.server.getAccountSystem().getAccount(this.accountId);
    }

    /**
     * Check if the serverconnection is currently logged in
     * @return true if logged in
     */
    public boolean isLoggedIn() {
        return this.accountId > 0;
    }

    public long getAccountId() {
        return this.accountId;
    }

    /**
     * Returns if the connection or the account is muted
     * @return boolean
     */
    public boolean isMuted() {
        AccountData account = this.getAccount();

        if(account != null) {
            return this.muted || account.isMuted();
        } else {
            return this.muted;
        }
    }

    /**
     * Returns if the connection or the account is banned
     * @return boolean
     */
    public boolean isBanned() {
        AccountData account = this.getAccount();

        if(account != null) {
            return this.banned || account.isBanned();
        } else {
            return this.banned;
        }
    }

    /**
     * Returns only true if the connection is muted, NOT THE ACCOUNT.
     * @return boolean
     */
    public boolean isConnectionMuted() {
        return this.muted;
    }

    /**
     * Returns only true if the connection is banned, NOT THE ACCOUNT.
     * @return boolean
     */
    public boolean isConnectionBanned() {
        return this.banned;
    }

    /**
     * Returns if the connection has socialspy enabled and has the permissions for it.
     * @return boolean
     */
    public boolean isSocialSpy() {
        if(this.getAccount() != null && (this.getAccount().getPermissionLevel() == AccountPermissionLevel.ADMIN || this.getAccount().getPermissionLevel() == AccountPermissionLevel.MODERATOR)) {
            return this.socialSpy;
        } else {
            this.socialSpy = false;
            return false;
        }
    }

    public SlakeoverflowServer getServer() {
        return this.server;
    }

    // SEND DATA

    /**
     * A null safe way to send data to a connection
     *
     * @param text The text to send
     * @return if sending was successful
     */
    public boolean sendUTF(String text) {
        if (this.getDataIOStreamHandler() != null && !this.getDataIOStreamHandler().isClosed() && this.getClient() != null && !this.getClient().isClosed()) {
            try {
                this.getDataIOStreamHandler().writeUTF(text);
                return true;
            } catch (IOException e) {
                this.server.getLogger().warning("CONNECTION", "Error while sending data to " + this.getClientId());
                this.getClient().close();
                return false;
            }
        } else {
            return false;
        }
    }
}
