package com.github.q11hackermans.slakeoverflow_server.accounts;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountSystem {
    private final File databaseFile;
    private Connection database;

    // CONSTRUCTOR
    public AccountSystem() {
        this.databaseFile = new File(System.getProperty("user.dir") + "accounts.db");

        this.connect();
    }

    // ACCOUNT MANAGEMENT

    /**
     * Create a new account
     * @param username Username
     * @param password Password (unhashed, this method will automatically hash the specified password string)
     * @return success
     */
    public boolean createAccount(String username, String password) {
        try {

            List<AccountData> accounts = this.getAccounts();

            for(AccountData account : accounts) {
                if(!account.getUsername().equalsIgnoreCase(username)) {
                    return false;
                }
            }

            String sql = "INSERT INTO users (username, password) values (?,?)";
            PreparedStatement statement = database.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, this.getPasswordHashValue(password));

            return true;

        } catch(SQLException ignored) {}

        return false;
    }

    /**
     * Delete an account with a specific id
     * @param id ID
     * @return success
     */
    public boolean deleteAccount(long id) {
        try {
            String sql = "SELECT id FROM users WHERE id = ?;";
            PreparedStatement statement = database.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM users (id) WHERE id = ?;";
                PreparedStatement statement2 = database.prepareStatement(sql2);
                statement2.setLong(1, id);
                statement2.execute();

                return true;
            }
        } catch(SQLException ignored) {}

        return false;
    }

    /**
     * Update the username of an account
     * @param id User ID
     * @param username New username
     * @return success
     */
    public boolean updateUsername(int id, String username) {
        AccountData data = this.getAccount(id);

        if(data != null) {
            try {

                String sql = "UPDATE users SET username = ? WHERE id = ?";
                PreparedStatement statement = this.database.prepareStatement(sql);
                statement.setString(1, username);
                statement.setLong(2, data.getId());
                statement.execute();

                return true;

            } catch(SQLException ignored) {}
        }

        return false;
    }

    /**
     * Update the password of an account
     * @param id User ID
     * @param password Unhashed password (this method will hash the password before saving it into database)
     * @return success
     */
    public boolean updatePassword(int id, String password) {
        AccountData data = this.getAccount(id);

        if(data != null) {
            try {

                String sql = "UPDATE users SET password = ? WHERE id = ?";
                PreparedStatement statement = this.database.prepareStatement(sql);
                statement.setString(1, this.getPasswordHashValue(password));
                statement.setLong(2, data.getId());
                statement.execute();

                return true;

            } catch(SQLException ignored) {}
        }

        return false;
    }

    /**
     * Update the admin status of an account
     * @param id User ID
     * @param adminStatus admin status
     * @return success
     */
    public boolean updateAdminStatus(int id, boolean adminStatus) {
        AccountData data = this.getAccount(id);

        if(data != null) {
            try {

                String sql = "UPDATE users SET admin = ? WHERE id = ?";
                PreparedStatement statement = this.database.prepareStatement(sql);
                statement.setBoolean(1, adminStatus);
                statement.setLong(2, data.getId());
                statement.execute();

                return true;

            } catch(SQLException ignored) {}
        }

        return false;
    }

    /**
     * Get an account with a specific id
     * @param id ID
     * @return AccountData
     */
    public AccountData getAccount(long id) {
        try {
            String getUsersStatement = "SELECT * FROM users WHERE id = ?;";

            PreparedStatement statement = this.database.prepareStatement(getUsersStatement);
            statement.setLong(1, id);

            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                if(rs.getLong("id") == id) {
                    return new AccountData(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getBoolean("admin"));
                }
            }

        } catch(Exception ignored) {}

        return null;
    }

    /**
     * Return an account with a specific username
     * @param username Username
     * @return AccountData
     */
    public AccountData getAccount(String username) {
        for(AccountData data : this.getAccounts()) {
            if(data.getUsername().equalsIgnoreCase("username")) {
                return data;
            }
        }

        return null;
    }

    /**
     * Get a list with all registered accounts
     * @return List of AccountData
     */
    public List<AccountData> getAccounts() {
        List<AccountData> accounts = new ArrayList<>();

        try {
            String getUsersStatement = "SELECT * FROM users;";

            PreparedStatement statement = this.database.prepareStatement(getUsersStatement);
            ResultSet result = statement.executeQuery();

            while(result.next()) {
                accounts.add(new AccountData(result.getLong("id"), result.getString("username"), result.getString("password"), result.getBoolean("admin")));
            }
        } catch(SQLException e) {
            SlakeoverflowServer.getServer().getLogger().warning("ACCOUNTS", "SQL Exception: " + e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
        }

        return accounts;
    }

    // DATABASE

    private boolean connect() {
        try {

            if(!databaseFile.exists()) {
                databaseFile.createNewFile();
            }

            database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath());
            SlakeoverflowServer.getServer().getLogger().info("ACCOUNTS", "SQLite connection established");

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "username VARCHAR(255)," +
                    "password VARCHAR(255)," +
                    "admin boolean NOT NULL DEFAULT false" +
                    ");";
            PreparedStatement statement = database.prepareStatement(createUsersTable);
            statement.execute();

            return true;

        } catch(IOException e) {
            SlakeoverflowServer.getServer().getLogger().warning("ACCOUNTS", "SQLite connection failed, I/O error");
        } catch(SQLException e) {
            SlakeoverflowServer.getServer().getLogger().warning("ACCOUNTS", "SQLite connection failed, SQL error");
        }
        return false;
    }

    // UTILITIES
    public String getPasswordHashValue(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();
            for (byte aByte : bytes) {
                builder.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            return builder.toString();
        } catch(NoSuchAlgorithmException ignored) {}
        return "";
    }
}
