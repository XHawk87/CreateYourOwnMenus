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

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MenuCommandUtils {
    public static MenuSlotItemStackRef getMenuSlotItemStack(CreateYourOwnMenus plugin, CommandSender sender, String menuId, String slotStr) {
        // Check menu exists
        Menu menu = plugin.getMenu(menuId);
        if (menu == null) {
            sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", menuId));
            return null;
        }

        // Check slot is valid
        int slot;
        try {
            slot = Integer.parseInt(slotStr);
        } catch (NumberFormatException ex) {
            sender.sendMessage(plugin.translate(sender, "expected-integer-slot", "The slot should be an integer number: {0}", slotStr));
            return null;
        }

        int size = menu.getInventory().getSize();
        if (slot < 0 || slot >= size) {
            sender.sendMessage(plugin.translate(sender, "invalid-slot-number", "The menu {0} has {1} slots numbered from 0 to {2}, so {3} is out of range", menuId, size, size - 1, slotStr));
            return null;
        }

        // Check there is an item in that slot
        return new MenuSlotItemStackRef(menu, slot);
    }

    public static boolean isValidTitle(CreateYourOwnMenus plugin, CommandSender sender, String title) {
        if (title.length() > 32) {
            sender.sendMessage(plugin.translate(sender, "title-char-limit", "Titles are limited to 32 characters (including colours)"));
            return false;
        }
        return true;
    }

    public static Player getPlayerByName(String playerName) {
        //noinspection deprecation
        return Bukkit.getPlayer(playerName);
    }
}
