package com.github.q11hackermans.slakeoverflow_server.accounts;

public class AccountData {
    private final long id;
    private final String username;
    private final String password;
    private final boolean admin;

    public AccountData(long id, String username, String password, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.admin = isAdmin;
    }

    /**
     * Get the user ID
     * @return User ID
     */
    long getId() {
        return this.id;
    }

    /**
     * Get the username
     * @return Username String
     */
    String getUsername() {
        return this.username;
    }

    /**
     * Get the hashed password
     * @return Hashed password String
     */
    String getPassword() {
        return this.password;
    }

    /**
     * Check if the account is an admin
     * @return Player admin status
     */
    boolean isAdmin() {
        return this.admin;
    }

    public boolean equalsOtherAccount(AccountData otherAccount) {
        return otherAccount.getId() == this.getId();
    }
}
