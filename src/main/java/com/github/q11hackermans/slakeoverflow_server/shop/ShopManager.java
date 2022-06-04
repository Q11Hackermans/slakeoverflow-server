package com.github.q11hackermans.slakeoverflow_server.shop;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {
    private final List<Integer> customShopIds;

    public ShopManager() {
        this.customShopIds = new ArrayList<>();
    }

    // ACCOUNT SHOP DATA

    /**
     * Add item to an account
     * @param accountId account id
     * @param itemId item id
     */
    public void addItemToAccount(long accountId, int itemId) {
        AccountData account = SlakeoverflowServer.getServer().getAccountSystem().getAccount(accountId);

        if(account != null) {
            JSONArray shopData = account.getShopData();

            if(this.itemExists(itemId) && !shopData.toList().contains(itemId)) {
                shopData.put(itemId);
            }

            SlakeoverflowServer.getServer().getAccountSystem().updateShopData(account.getId(), shopData);
        }
    }

    /**
     * Remove item from account
     * @param accountId account id
     * @param itemId item id
     */
    public void removeItemFromAccount(long accountId, int itemId) {
        AccountData account = SlakeoverflowServer.getServer().getAccountSystem().getAccount(accountId);

        if(account != null) {
            JSONArray shopData = account.getShopData();

            if(shopData.toList().contains(itemId)) {
                shopData.remove(itemId);
            }

            SlakeoverflowServer.getServer().getAccountSystem().updateShopData(account.getId(), shopData);
        }
    }

    /**
     * Clear items from account
     * @param accountId account id
     */
    public void clearItemsFromAccount(long accountId) {
        AccountData account = SlakeoverflowServer.getServer().getAccountSystem().getAccount(accountId);

        if(account != null) {
            SlakeoverflowServer.getServer().getAccountSystem().updateShopData(account.getId(), new JSONArray());
        }
    }

    /**
     * Get items from an account
     * @param accountId account id
     * @return List of account ids
     */
    public List<Integer> getItemsFromAccount(long accountId) {
        List<Integer> returnList = new ArrayList<>();

        AccountData account = SlakeoverflowServer.getServer().getAccountSystem().getAccount(accountId);

        if(account != null) {
            for(Object o : account.getShopData()) {
                try {
                    returnList.add((int) o);
                } catch(ClassCastException ignored) {}
            }
        }

        return returnList;
    }

    // SHOP IDS

    public boolean itemExists(int itemId) {
        if(this.customShopIds.contains(itemId)) {
            return true;
        }
        return false;
    }

    // CUSTOM SHOP IDS

    public void addCustomShopItem(int id) {
        if(id > 100 && this.customShopIds.contains(id)) {
            this.customShopIds.add(id);
        }
    }

    public void removeCustomShopItem(int id) {
        this.customShopIds.remove(id);
    }

    public void clearCustomShopItems() {
        this.customShopIds.clear();
    }

    public List<Integer> getCustomShopItems() {
        return List.copyOf(this.customShopIds);
    }
}
