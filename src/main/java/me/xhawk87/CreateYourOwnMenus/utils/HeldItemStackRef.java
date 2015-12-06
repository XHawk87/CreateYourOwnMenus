/*
 * Copyright 2015 XHawk87.
 *
 * All Rights Reserved.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author XHawk87
 */
public class HeldItemStackRef implements ItemStackRef {

    private String playerName;

    public HeldItemStackRef(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public ItemStack get() {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            return null;
        }
        return player.getItemInHand();
    }

    @Override
    public void update() {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            player.updateInventory();
        }
    }
}
