/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
        StringBuilder sb = new StringBuilder(args[2].replace('&', ChatColor.COLOR_CHAR));
        for (int i = 3; i < args.length; i++) {
            sb.append(" ").append(args[i].replace('&', ChatColor.COLOR_CHAR));
        }
        String title = sb.toString();
        if (title.length() > 32) {
            sender.sendMessage(plugin.translate(sender, "title-char-limit", "Titles are limited to 32 characters (including colours)"));
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
