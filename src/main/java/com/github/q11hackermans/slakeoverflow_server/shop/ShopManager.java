package com.github.q11hackermans.slakeoverflow_server.shop;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;
import com.github.q11hackermans.slakeoverflow_server.accounts.AccountData;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    private final SlakeoverflowServer server;
    private final Map<Integer, ShopItem> customShopIds;

    public ShopManager(SlakeoverflowServer server) {
        this.server = server;
        this.customShopIds = new HashMap<>();
    }

    // ACCOUNT SHOP DATA

    /**
     * Add item to an account
     * @param accountId account id
     * @param itemId item id
     */
    public void addItemToAccount(long accountId, int itemId) {
        AccountData account = this.server.getAccountSystem().getAccount(accountId);

        if(account != null) {
            JSONArray shopData = account.getShopData();

            if(this.itemExists(itemId) && !shopData.toList().contains(itemId)) {
                shopData.put(itemId);
            }

            this.server.getAccountSystem().updateShopData(account.getId(), shopData);
        }
    }

    /**
     * Purchase an item as an account
     * @param accountId account id
     * @param itemId item id
     * @return success
     */
    public boolean purchaseItem(long accountId, int itemId) {
        AccountData account = this.server.getAccountSystem().getAccount(accountId);

        if(account != null) {
            if(this.itemExists(itemId) && !account.getShopData().toList().contains(itemId)) {
                final Map<Integer, ShopItem> shopItems = getShopItems();
                boolean isEnabled = shopItems.get(itemId).isEnabled();
                int requiredLevel = shopItems.get(itemId).getRequiredLevel();
                int price = shopItems.get(itemId).getPrice();

                if (isEnabled && account.getLevel() >= requiredLevel && account.getBalance() >= price && !this.getItemsFromAccount(account.getId()).contains(itemId)) {
                    this.server.getAccountSystem().updateBalance(account.getId(), account.getBalance() - price);
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
        AccountData account = this.server.getAccountSystem().getAccount(accountId);

        if(account != null) {
            JSONArray shopData = account.getShopData();

            if(shopData.toList().contains(itemId)) {
                shopData.remove(itemId);
            }

            this.server.getAccountSystem().updateShopData(account.getId(), shopData);
        }
    }

    /**
     * Clear items from account
     * @param accountId account id
     */
    public void clearItemsFromAccount(long accountId) {
        AccountData account = this.server.getAccountSystem().getAccount(accountId);

        if(account != null) {
            this.server.getAccountSystem().updateShopData(account.getId(), new JSONArray());
        }
    }

    /**
     * Get items from an account
     * @param accountId account id
     * @return List of account ids
     */
    public List<Integer> getItemsFromAccount(long accountId) {
        List<Integer> returnList = new ArrayList<>();

        AccountData account = this.server.getAccountSystem().getAccount(accountId);

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

    public SlakeoverflowServer getServer() {
        return this.server;
    }
}
