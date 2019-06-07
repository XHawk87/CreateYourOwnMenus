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
package me.xhawk87.CreateYourOwnMenus.commands;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuCopyCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuCreateCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuDeleteCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuEditCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuGrabCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuListCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuOpenCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuReloadCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuScriptCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.MenuSetCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The base for all menu commands
 *
 * @author XHawk87
 */
public class MenuCommand implements CommandExecutor {

    /**
     * All subcommands of the menu command, stored by their name
     */
    private Map<String, IMenuCommand> subCommands = new HashMap<>();
    private CreateYourOwnMenus plugin;

    public MenuCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
        subCommands.put("create", new MenuCreateCommand(plugin));
        subCommands.put("edit", new MenuEditCommand(plugin));
        subCommands.put("delete", new MenuDeleteCommand(plugin));
        subCommands.put("list", new MenuListCommand(plugin));
        subCommands.put("open", new MenuOpenCommand(plugin));
        subCommands.put("script", new MenuScriptCommand(plugin));
        subCommands.put("reload", new MenuReloadCommand(plugin));
        subCommands.put("grab", new MenuGrabCommand(plugin));
        subCommands.put("copy", new MenuCopyCommand(plugin));
        subCommands.put("set", new MenuSetCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // It is assumed that entering the menu command without parameters is an
        // attempt to get information about it. So let's give it to them.
        if (args.length == 0) {
            for (Map.Entry<String, IMenuCommand> entry : subCommands.entrySet()) {
                String name = entry.getKey();
                IMenuCommand menuCommand = entry.getValue();
                String permission = menuCommand.getPermission();
                if (permission != null && sender.hasPermission(permission)) {
                    sender.sendMessage(plugin.translate(sender, "menu-" + name + "-usage", menuCommand.getUsage()));
                }
            }
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        IMenuCommand menuCommand = subCommands.get(subCommandName);
        if (menuCommand == null) {
            return false; // They mistyped or entered an invalid subcommand
        }
        // Handle the permissions check
        String permission = menuCommand.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(plugin.translate(sender, "command-no-perms", "You do not have permission to use this command"));
            return true;
        }
        // Remove the sub-command from the args list and pass along the rest
        if (!menuCommand.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length))) {
            // A sub-command returning false should display the usage information for that sub-command
            sender.sendMessage(plugin.translate(sender, "menu-" + subCommandName + "-usage", menuCommand.getUsage()));
        }
        return true;
    }
}
