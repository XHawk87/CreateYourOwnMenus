/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
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

        // Expecting exactly one parameter, the id
        if (args.length < 1 || args.length > 2) {
            return false;
        }

        Player target;
        String id;
        if (args.length == 2) {
            target = plugin.getServer().getPlayer(args[0]);
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
