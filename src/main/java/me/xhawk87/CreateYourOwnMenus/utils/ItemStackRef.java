/*
 * Copyright (C) 2013-2019 XHawk87
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author XHawk87
 */
public abstract class ItemStackRef {

    public abstract ItemStack get();

    abstract boolean set(ItemStack itemStack);

    public ItemMeta getItemMeta() {
        ItemStack item = get();
        return item == null ? null : item.getItemMeta();
    }

    public boolean setItemMeta(ItemMeta meta) {
        ItemStack item = get();
        return item != null && item.setItemMeta(meta);
    }

    public abstract void update();
}
