/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
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
            sender.sendMessage("/menu edit [id] - Opens the menu with the given id for editing. This allows you to move menu items in and out of a menu");
            return true;
        }
        
        // Expecting exactly one parameter, the id
        if (args.length != 1) {
            return false;
        }

        // Check the id is valid
        String id = args[0];
        Menu menu = plugin.getMenu(id);
        if (menu == null) {
            sender.sendMessage("There is no menu with id " + id);
            return true;
        }
        
        // Only players can open inventories
        if (sender instanceof Player) {
            Player player = (Player) sender;
            menu.edit(player);
        } else {
            sender.sendMessage("You must be a player to edit a menu in-game");
        }
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu edit [id] - Open an existing menu by its id for editing";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.edit";
    }
}
