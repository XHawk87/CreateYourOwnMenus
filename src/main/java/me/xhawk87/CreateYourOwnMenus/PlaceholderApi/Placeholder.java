package me.xhawk87.CreateYourOwnMenus.PlaceholderApi;

import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.TreeMap;

/**
 * Placeholder Data and simple inner class
 */
public class Placeholder {
    public Placeholder(int slot, ItemStack item) {
        this.slot = slot;
        this.item = item;
    }

    private ItemStack item; //stores a reference of the used item
    private int slot;  //stores the inventory slot
    private String title; //stores a custom title, may be null
    private Map<Integer, String> lorePlaceholders = new TreeMap<>(); //stores the custom lores and theire line,position, may be empty

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<Integer, String> getLorePlaceholders() {
        return lorePlaceholders;
    }

    public void setLorePlaceholders(Map<Integer, String> lorePlaceholderPositions) {
        this.lorePlaceholders = lorePlaceholderPositions;
    }
}