/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author XHawk87
 */
public class MenuDeleteCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuDeleteCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage("/menu delete [id] - Deletes the menu with the given id");
            return true;
        }
        
        // Expecting exactly 1 parameter, the id
        if (args.length != 1) {
            return false;
        }
        
        String id = args[0];
        
        // Check that ths id is valid
        Menu menu = plugin.getMenu(id);
        if (menu == null) {
            sender.sendMessage("There is no menu with id " + id);
            return true;
        }
        
        // Delete the menu
        menu.delete();
        sender.sendMessage(menu.getTitle() + " has been deleted");
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu delete [id] - Deletes an existing menu by its id";
    }

    @Override
    public String getPermission() {
        return "createyourownmenus.commands.menu.delete";
    }
}
