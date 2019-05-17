/*
 * Copyright 2015 XHawk87.
 *
 * All Rights Reserved.
 */
package me.xhawk87.CreateYourOwnMenus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * @author XHawk87
 */
public class DummyMenu extends Menu {

    public DummyMenu(CreateYourOwnMenus plugin) {
        super(plugin, "item-in-hand");
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public void save() {
    }

    @Override
    public void reload() {
    }

    @Override
    public void load() {
    }

    @Override
    public void delete() {
    }

    @Override
    public void open(final Player player) {
    }

    @Override
    public void edit(Player player) {
    }

    @Override
    public void doneEditing(Player player) {
    }

    @Override
    public boolean isEditing(Player player) {
        return false;
    }

    @Override
    public String getTitle() {
        return "Menu Item Outside of Menu";
    }
}
