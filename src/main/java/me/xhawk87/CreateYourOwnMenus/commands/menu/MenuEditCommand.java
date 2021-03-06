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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author XHawk87
 */
public class MenuEditCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuEditCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage(plugin.translate(sender, "menu-edit-usage-extended", "/menu edit ([player]) [id] - Opens the menu with the given id for editing. This allows you to move menu items in and out of a menu. If a player is given, it will open the menu for the given player for editing, whether or not they have permission"));
            return true;
        }

        if (args.length > 2) {
            return false;
        }

        Player target;
        String id;
        if (args.length == 2) {
            target = MenuCommandUtils.getPlayerByName(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.translate(sender, "player-not-online", "{0} is not online", args[0]));
                return true;
            }
            id = args[1];
        } else {
            id = args[0];
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(plugin.translate(sender, "console-no-target", "The console must specify a player"));
                return false;
            }
        }

        // Check the id is valid
        Menu menu = plugin.getMenu(id);
        if (menu == null) {
            sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", id));
            return true;
        }

        menu.edit(target);
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu edit ([player]) [id] - Open an existing menu by its id for editing";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.edit";
    }
}
