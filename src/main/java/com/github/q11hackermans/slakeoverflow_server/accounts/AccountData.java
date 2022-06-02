package com.github.q11hackermans.slakeoverflow_server.accounts;

import org.json.JSONArray;

public class AccountData {
    private final long id;
    private final String username;
    private final String password;
    private final int permissionLevel;
    private final int level;
    private final int balance;
    private final JSONArray shopData;

    public AccountData(long id, String username, String password, int permissionLevel, int level, int balance, JSONArray shopData) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.permissionLevel = permissionLevel;
        this.level = level;
        this.balance = balance;
        this.shopData = shopData;
    }

    /**
     * Get the user ID
     * @return User ID
     */
    public long getId() {
        return this.id;
    }

    /**
     * Get the username
     * @return Username String
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Get the hashed password
     * @return Hashed password String
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Check if the account is an admin
     * @return Player admin status
     */
    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public boolean equalsOtherAccount(AccountData otherAccount) {
        return otherAccount.getId() == this.getId();
    }

    public int getLevel() {
        return this.level;
    }

    public int getBalance() {
        return this.balance;
    }

    public JSONArray getShopData() {
        return this.shopData;
    }
}
