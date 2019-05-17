/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptModifyLoreCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author XHawk87
 */
public class MenuScriptAppendCommand extends IMenuScriptModifyLoreCommand {

    public MenuScriptAppendCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) append [text] - Adds this text as the last line in the held item's lore";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.append";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, Command command, String label, String[] args) {
        String commandString = StringUtils.join(args, " ");
        commandString = MenuScriptUtils.replaceColorSymbol(commandString);
        MenuScriptUtils.appendToLore(commandString, loreStrings);
        sender.sendMessage(plugin.translate(sender, "script-appended", "{0} was added to the command list of this menu item", commandString));
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
