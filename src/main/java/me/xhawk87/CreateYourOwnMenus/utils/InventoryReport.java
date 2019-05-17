/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XHawk87
 */
public class InventoryReport {

    private Map<Integer, ItemStack> items = new HashMap<>();
    private int count = 0;
    private int spaces = 0;
    private ItemStack type = null;
    private int maxStackSize = -1;

    public int getCount() {
        return count;
    }

    public Map<Integer, ItemStack> getItems() {
        return items;
    }

    public int getSpaces() {
        return spaces;
    }

    public ItemStack getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString().toLowerCase().replace('_', ' ');
    }

    public boolean hasType() {
        return type != null;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addItem(Integer slot, ItemStack item) {
        this.items.put(slot, item);
    }

    public void setSpaces(int spaces) {
        this.spaces = spaces;
    }

    public void setType(ItemStack type) {
        this.type = type;
        maxStackSize = type.getMaxStackSize();
        if (maxStackSize == -1) {
            maxStackSize = 64;
        }
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void addCount(int amount) {
        count += amount;
    }

    public void addSpaces(int amount) {
        spaces += amount;
    }

    public static InventoryReport getReport(Inventory inv, List<Integer> slots) {
        InventoryReport report = new InventoryReport();
        int empties = 0;
        for (int slot : slots) {
            if (slot >= inv.getSize()) {
                throw new IllegalArgumentException("Error in menu script line (expected slot number less than inventory size " + inv.getSize() + "): " + slot);
            }
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                if (!report.hasType() || item.isSimilar(item)) {
                    if (report.getType() == null) {
                        report.setType(item);
                        report.addSpaces(empties * report.getMaxStackSize());
                    }
                    report.addCount(item.getAmount());
                    report.addItem(slot, item);
                } else {
                    throw new IllegalArgumentException("Error in menu script line (expected similar items in slots " + report.getType().toString() + "): " + item.toString());
                }
            } else {
                if (!report.hasType()) {
                    empties++;
                } else {
                    report.addSpaces(report.getMaxStackSize());
                }
            }
        }
        return report;
    }
}
