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

/**
 *
 * @author XHawk87
 */
public class MenuReloadCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuReloadCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.reloadMenus();
            sender.sendMessage(plugin.translate(sender, "menus-reloaded", "Menus reloaded from disk"));
            return true;
        } else if (args.length == 1) {
            String menuId = args[0];
            if (plugin.reloadMenu(menuId)) {
                sender.sendMessage(plugin.translate(sender, "menu-reloaded", "Reloaded {0} menu from disk", menuId));
            } else {
                sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", menuId));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getUsage() {
        return "/menu reload - Reloads all menus from disk";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.reload";
    }
}
