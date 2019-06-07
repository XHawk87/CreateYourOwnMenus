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
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Sub-command to create a new menu
 *
 * @author XHawk87
 */
public class MenuCopyCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuCopyCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage(plugin.translate(sender, "menu-copy-usage-extended", "/menu copy [old menu id] [new menu id] [new title] - Create a new menu with the same size and contents as the given menu but with a new id and title"));
            return true;
        }
        // The title may contain spaces, so more than 3 args may be present
        if (args.length < 3) {
            return false;
        }

        String oldMenuId = args[0];
        String newMenuId = args[1];

        Menu oldMenu = plugin.getMenu(oldMenuId);
        if (oldMenu == null) {
            sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", oldMenuId));
            return true;
        }
        int rows = oldMenu.getInventory().getSize() / 9;

        // Build the title
        String title = StringUtils.join(Arrays.asList(args).subList(2, args.length), " ")
                .replace('&', ChatColor.COLOR_CHAR);
        if (!MenuCommandUtils.isValidTitle(plugin, sender, title)) {
            return true;
        }

        // Check that the id is unique
        if (plugin.getMenu(newMenuId) != null) {
            sender.sendMessage(plugin.translate(sender, "menu-already-exists", "A menu with id {0} already exists", newMenuId));
            return true;
        }

        // Create the menu
        Menu newMenu = plugin.createMenu(newMenuId, title, rows);
        Inventory inv = newMenu.getInventory();
        ItemStack[] contents = oldMenu.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                inv.setItem(i, item.clone());
            }
        }
        newMenu.save();
        sender.sendMessage(plugin.translate(sender, "menu-created", "{0} menu has been created", title));
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu copy [old menu id] [new menu id] [new title] - Create a new menu with the same size and contents as the given menu but with a new id and title";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.copy";
    }
}
