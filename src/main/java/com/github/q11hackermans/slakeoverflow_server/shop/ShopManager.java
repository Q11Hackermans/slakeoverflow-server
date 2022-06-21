package com.github.q11hackermans.slakeoverflow_server.shop;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {
    private final Map<Integer, ShopItem> customShopIds;

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
                boolean isEnabled = this.customShopIds.get(itemId).isEnabled();
                int requiredLevel = this.customShopIds.get(itemId).getRequiredLevel();
                int price = this.customShopIds.get(itemId).getPrice();

                if(isEnabled && account.getLevel() >= requiredLevel && account.getBalance() >= price) {
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
                    int itemId = (int) o;
                    ShopItem item = this.getShopItems().get(itemId);

                    if(item != null) {
                        if(item.isEnabled()) {
                            returnList.add(itemId);
                        }
                    }
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

    public Map<Integer, ShopItem> getPersistentShopItems() {
        return Map.of(
                1, new ShopItem(this, true, 0, 500),
                2, new ShopItem(this, true, 0, 5000),
                3, new ShopItem(this, true, 0, 50000),
                4, new ShopItem(this, true, 0, -100)
        );
    }

    // CUSTOM SHOP IDS

    public void addCustomShopItem(int id, boolean enabled, int requiredLevel, int price) {
        if(id > 100 && this.customShopIds.containsKey(id)) {
            this.customShopIds.put(id, new ShopItem(this, enabled, requiredLevel, price));
        }
    }

    public void removeCustomShopItem(int id) {
        this.customShopIds.remove(id);
    }

    public void clearCustomShopItems() {
        this.customShopIds.clear();
    }

    public Map<Integer, ShopItem> getCustomShopItems() {
        return Map.copyOf(this.customShopIds);
    }

    public Map<Integer, ShopItem> getShopItems() {
        Map<Integer, ShopItem> returnMap = new HashMap<>();
        returnMap.putAll(this.getPersistentShopItems());
        returnMap.putAll(this.getCustomShopItems());
        return returnMap;
    }
}
