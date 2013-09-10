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
public class MenuOpenCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuOpenCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage("/menu open ([player]) [id] - Opens the menu with the given id for use. If a player is specified, the menu will open for this player instead of the command sender. Clicking an icon will not pick it up, but instead run all commands listed in the lore");
            return true;
        }

        // Expecting 1 or 2 parameters, depending on whether player is provided
        if (args.length > 2) {
            return false;
        }

        Player target;
        int i = 0;
        if (args.length == 1) {
            // No player provided, target the sender
            if (sender instanceof Player) {
                target = (Player) sender;
                
                // If the command is self-targetted, also check specific-menu permissions
                if (!sender.hasPermission("cyom.commands.menu.open")
                        && !sender.hasPermission("cyom.menu." + args[0])) {
                    sender.sendMessage("You do not have permission to open this menu");
                    return true;
                }
            } else {
                sender.sendMessage("You need to be in-game to open a menu");
                return true;
            }
        } else {
            // You must have full permissions to target another player
            if (!sender.hasPermission("cyom.commands.menu.open")) {
                sender.sendMessage("You do not have permission to use this command");
                return true;
            }
            
            // Player is provided, so target the named player
            String playerName = args[i++];
            target = plugin.getServer().getPlayerExact(playerName);
            if (target == null) {
                sender.sendMessage(playerName + " is not online");
                return true;
            }
        }

        // Ensure the menu id is valid
        String id = args[i++];
        Menu menu = plugin.getMenu(id);
        if (menu == null) {
            sender.sendMessage("There is no menu with id " + id);
            return true;
        }

        // Open the menu for the target
        menu.open(target);
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu open ([player]) [id] - Open an existing menu by its id for using. Optionally opens the menu for another player";
    }

    @Override
    public String getPermission() {
        return null; // Allow all through in order to check specific-menu permissions
    }
}
