/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author XHawk87
 */
public class MenuScriptCommand implements IMenuCommand {

    /**
     * All subcommands of the menu command, stored by their name
     */
    private Map<String, IMenuScriptCommand> subCommands = new HashMap<>();
    /**
     * All aliases of the menu command. these should not be listed in help as
     * separate subcommands
     */
    private Map<String, IMenuScriptCommand> aliases = new HashMap<>();
    private CreateYourOwnMenus plugin;

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
        Player target = plugin.getServer().getPlayerExact(subCommandName);
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

        IMenuScriptCommand menuScriptCommand = subCommands.get(subCommandName);
        if (menuScriptCommand == null) {
            menuScriptCommand = aliases.get(subCommandName);
            if (menuScriptCommand == null) {
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
        if (!menuScriptCommand.onCommand(sender, target, command, label, Arrays.copyOfRange(args, index, args.length))) {
            // A sub-command returning false should display the usage information for that sub-command
            sender.sendMessage(plugin.translate(sender, "menu-script-" + subCommandName + "-usage", menuScriptCommand.getUsage()));
        }
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) [clear|show|hide|append|insert|replace|delete] [parameters...]";
    }

    @Override
    public String getPermission() {
        return null;
    }
}
