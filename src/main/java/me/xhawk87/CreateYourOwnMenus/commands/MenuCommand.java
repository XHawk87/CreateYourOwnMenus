/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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

    public MenuCommand(CreateYourOwnMenus plugin) {
        subCommands.put("create", new MenuCreateCommand(plugin));
        subCommands.put("edit", new MenuEditCommand(plugin));
        subCommands.put("delete", new MenuDeleteCommand(plugin));
        subCommands.put("list", new MenuListCommand(plugin));
        subCommands.put("open", new MenuOpenCommand(plugin));
        subCommands.put("script", new MenuScriptCommand(plugin));
        subCommands.put("reload", new MenuReloadCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // It is assumed that entering the menu command without parameters is an
        // attempt to get information about it. So let's give it to them.
        if (args.length == 0) {
            for (IMenuCommand menuCommand : subCommands.values()) {
                String permission = menuCommand.getPermission();
                if (permission != null && sender.hasPermission(permission)) {
                    sender.sendMessage(menuCommand.getUsage());
                }
            }
            return true;
        }

        String subCommandName = args[0];
        IMenuCommand menuCommand = subCommands.get(subCommandName.toLowerCase());
        if (menuCommand == null) {
            return false; // They mistyped or entered an invalid subcommand
        }
        // Handle the permissions check
        String permission = menuCommand.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage("You do not have permission to use this commands");
            return true;
        }
        // Remove the sub-command from the args list and pass along the rest
        if (!menuCommand.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length))) {
            // A sub-command returning false should display the usage information for that sub-command
            sender.sendMessage(menuCommand.getUsage());
        }
        return true;
    }
}
