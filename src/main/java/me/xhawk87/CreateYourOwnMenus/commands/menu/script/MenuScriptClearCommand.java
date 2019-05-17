/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptModifyLoreCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author XHawk87
 */
public class MenuScriptClearCommand extends IMenuScriptModifyLoreCommand {

    public MenuScriptClearCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) clear - Removes all lore from the item in your hand";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.clear";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, Command command, String label, String[] args) {
        loreStrings.clear();
        sender.sendMessage(plugin.translate(sender, "script-cleared", "The command list for this menu item has been cleared"));
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
