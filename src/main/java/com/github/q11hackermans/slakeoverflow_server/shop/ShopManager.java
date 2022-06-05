package com.github.q11hackermans.slakeoverflow_server.shop;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {
    private final Map<Integer, Integer> customShopIds;

    public ShopManager() {
        this.customShopIds = new HashMap<>();
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
     * Purchase an item as an account
     * @param accountId account id
     * @param itemId item id
     * @return success
     */
    public boolean purchaseItem(long accountId, int itemId) {
        AccountData account = SlakeoverflowServer.getServer().getAccountSystem().getAccount(accountId);

        if(account != null) {
            if(this.itemExists(itemId) && !account.getShopData().toList().contains(itemId)) {
                int price = this.customShopIds.get(itemId);
                if(account.getBalance() >= price && price > 0) {
                    SlakeoverflowServer.getServer().getAccountSystem().updateBalance(account.getId(), account.getBalance() - price);
                    this.addItemToAccount(account.getId(), itemId);

                    return true;
                }
            }
        }

        return false;
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
        if(this.getPersistentShopItems().containsKey(itemId) || this.customShopIds.containsKey(itemId)) {
            return true;
        }
        return false;
    }

    public Map<Integer, Integer> getPersistentShopItems() {
        return Map.of(
                1, -1,
                2, -1,
                3, -1,
                4, -1
        );
    }

    // CUSTOM SHOP IDS

    public void addCustomShopItem(int id, int price) {
        if(id > 100 && this.customShopIds.containsKey(id)) {
            this.customShopIds.put(id, price);
        }
    }

    public void removeCustomShopItem(int id) {
        this.customShopIds.remove(id);
    }

    public void clearCustomShopItems() {
        this.customShopIds.clear();
    }

    public Map<Integer, Integer> getCustomShopItems() {
        return Map.copyOf(this.customShopIds);
    }
}
