package com.github.q11hackermans.slakeoverflow_server.shop;

public class ShopItem {

    private final ShopManager shopManager;
    private boolean enabled;
    private int requiredLevel;
    private int price;

    public ShopItem(ShopManager shopManager, boolean enabled, int requiredLevel, int price) {
        this.shopManager = shopManager;
        this.enabled = enabled;
        this.requiredLevel = requiredLevel;
        this.price = price;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
