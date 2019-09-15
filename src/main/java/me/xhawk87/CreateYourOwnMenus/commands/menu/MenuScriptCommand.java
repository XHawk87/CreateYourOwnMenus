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
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptAppendCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptClearCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptDeleteCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptExportCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptHideCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptImportCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptInsertCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptReplaceCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptShowCommand;
import me.xhawk87.CreateYourOwnMenus.commands.menu.script.MenuScriptTitleCommand;
import me.xhawk87.CreateYourOwnMenus.utils.HeldItemStackRef;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandUtils;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author XHawk87
 */
public class MenuScriptCommand implements IMenuCommand {

    /**
     * All subcommands of the menu command, stored by their name
     */
    private final Map<String, IMenuScriptCommand> subCommands = new HashMap<>();
    /**
     * All aliases of the menu command. these should not be listed in help as
     * separate subcommands
     */
    private final Map<String, IMenuScriptCommand> aliases = new HashMap<>();
    private final CreateYourOwnMenus plugin;

    public MenuScriptCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;

        subCommands.put("append", new MenuScriptAppendCommand(plugin));
        subCommands.put("clear", new MenuScriptClearCommand(plugin));
        subCommands.put("delete", new MenuScriptDeleteCommand(plugin));
        subCommands.put("export", new MenuScriptExportCommand(plugin));
        subCommands.put("hide", new MenuScriptHideCommand(plugin));
        subCommands.put("import", new MenuScriptImportCommand(plugin));
        subCommands.put("insert", new MenuScriptInsertCommand(plugin));
        subCommands.put("replace", new MenuScriptReplaceCommand(plugin));
        subCommands.put("show", new MenuScriptShowCommand(plugin));
        subCommands.put("title", new MenuScriptTitleCommand(plugin));

        // Aliases
        aliases.put("add", subCommands.get("append"));
        aliases.put("remove", subCommands.get("delete"));
        aliases.put("set", subCommands.get("replace"));
        aliases.put("commands", subCommands.get("show"));
        aliases.put("comments", subCommands.get("hide"));
        aliases.put("reset", subCommands.get("clear"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // It is assumed that entering the menu command without parameters is an
        // attempt to get information about it. So let's give it to them.
        if (args.length == 0) {
            for (Map.Entry<String, IMenuScriptCommand> entry : subCommands.entrySet()) {
                String name = entry.getKey();
                IMenuScriptCommand menuCommand = entry.getValue();
                String permission = menuCommand.getPermission();
                if (permission != null && sender.hasPermission(permission)) {
                    sender.sendMessage(plugin.translate(sender, "menu-script-" + name + "-usage", menuCommand.getUsage()));
                }
            }
            return true;
        }

        int index = 0;
        String subCommandName = args[index++].toLowerCase();
        ItemStackRef itemStackRef;
        int splitter = subCommandName.indexOf("#");
        if (splitter != -1) { // There is a hash in the name, so this must be a menu#slot indicator
            String menuId = subCommandName.substring(0, splitter);
            String slotStr = subCommandName.substring(splitter + 1);

            // Check there is an item in that slot
            itemStackRef = MenuCommandUtils.getMenuSlotItemStack(plugin, sender, menuId, slotStr);
            if (itemStackRef == null) {
                return true;
            }
            if (itemStackRef.get() == null) {
                sender.sendMessage(plugin.translate(sender, "menu-slot-no-item", "There is no item in slot {0} of the {1} menu", slotStr, menuId));
                return true;
            }

            // Take the next arg as the sub command
            subCommandName = args[index++].toLowerCase();
        } else {
            Player target = MenuCommandUtils.getPlayerByName(subCommandName);
            if (target != null) {
                subCommandName = args[index++].toLowerCase();
            } else {
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    sender.sendMessage(plugin.translate(sender, "console-no-target", "The console must specify a player"));
                    return false;
                }
            }
            itemStackRef = new HeldItemStackRef(target.getUniqueId());
        }

        IMenuScriptCommand menuScriptCommand = subCommands.get(subCommandName);
        if (menuScriptCommand == null) {
            menuScriptCommand = aliases.get(subCommandName);
            if (menuScriptCommand == null) {
                sender.sendMessage(plugin.translate(sender, "unknown-subcommand", "{0} has no {1} sub-command", "/menu script", subCommandName));
                return false; // They mistyped or entered an invalid subcommand
            }
        }

        // Handle the permissions check
        String permission = menuScriptCommand.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(plugin.translate(sender, "command-no-perms", "You do not have permission to use this command"));
            return true;
        }
        // Remove the sub-command from the args list and pass along the rest
        if (MenuScriptUtils.isEmptyHand(itemStackRef, sender, plugin)) {
            return true;
        }
        if (!menuScriptCommand.onCommand(sender, itemStackRef, command, label, Arrays.copyOfRange(args, index, args.length))) {
            // A sub-command returning false should display the usage information for that sub-command
            sender.sendMessage(plugin.translate(sender, "menu-script-" + subCommandName + "-usage", menuScriptCommand.getUsage()));
        }
        itemStackRef.update();
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu script ([player | menu#slot]) [clear|show|hide|append|insert|replace|delete] [parameters...]";
    }

    @Override
    public String getPermission() {
        return null;
    }
}
