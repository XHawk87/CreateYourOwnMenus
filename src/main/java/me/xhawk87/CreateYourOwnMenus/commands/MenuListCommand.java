/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author XHawk87
 */
public class MenuListCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuListCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Expecting no parameters
        if (args.length != 0) {
            return false;
        }

        plugin.displayMenuList(sender);
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu list - Lists all menus";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.list";
    }
}
