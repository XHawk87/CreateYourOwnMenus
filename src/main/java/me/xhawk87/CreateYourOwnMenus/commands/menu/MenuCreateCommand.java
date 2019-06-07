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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Sub-command to create a new menu
 *
 * @author XHawk87
 */
public class MenuCreateCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuCreateCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage(plugin.translate(sender, "menu-create-usage-extended", "/menu create [id] [rows] [title] - Creates a new menu with the given id, title and number of rows. The id is used to refer to the menu in commands, it must be unique and cannot contain spaces or colours. The title is displayed to everyone opening the menu and can contain spaces and colours (using & instead of the section symbol). The number of rows determine how large an inventory is used for this menu."));
            return true;
        }
        // The title may contain spaces, so more than 3 args may be present
        if (args.length < 3) {
            return false;
        }

        String id = args[0];

        // Parse the number of rows as an integer
        int rows;
        try {
            rows = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(plugin.translate(sender, "expected-integer-rows", "The number of rows must be a whole number: {0}", args[1]));
            return true;
        }

        // Build the title
        String title = StringUtils.join(Arrays.asList(args).subList(2, args.length), " ")
                .replace('&', ChatColor.COLOR_CHAR);
        if (!MenuCommandUtils.isValidTitle(plugin, sender, title)) {
            return true;
        }

        // Check that the id is unique
        if (plugin.getMenu(id) != null) {
            sender.sendMessage(plugin.translate(sender, "menu-already-exists", "A menu with id {0} already exists", id));
            return true;
        }

        // Create the menu
        Menu menu = plugin.createMenu(id, title, rows);
        menu.save();
        sender.sendMessage(plugin.translate(sender, "menu-created", "{0} menu has been created", title));
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu create [id] [rows] [title] - Create a new menu with the given id, title and size";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.create";
    }
}
