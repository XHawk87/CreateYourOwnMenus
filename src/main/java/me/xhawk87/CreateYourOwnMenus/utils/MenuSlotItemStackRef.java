/*
 * Copyright 2015 XHawk87.
 *
 * All Rights Reserved.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author XHawk87
 */
public class MenuSlotItemStackRef implements ItemStackRef {

    private final Menu menu;
    private final int slot;

    public MenuSlotItemStackRef(Menu menu, int slot) {
        this.menu = menu;
        this.slot = slot;
    }

    @Override
    public ItemStack get() {
        return menu.getInventory().getItem(slot);
    }

    @Override
    public void update() {
        for (HumanEntity viewer : menu.getInventory().getViewers()) {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                player.updateInventory();
            }
        }
    }
}
