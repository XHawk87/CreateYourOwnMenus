/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptModifyLoreCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils.*;

/**
 * @author XHawk87
 */
public class MenuScriptShowCommand extends IMenuScriptModifyLoreCommand {

    public MenuScriptShowCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) show - Shows all hidden commands for the menu item in your hand, and hides all comments";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.show";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, Command command, String label, String[] args) {
        // Show the hidden commands
        StringBuilder comments = new StringBuilder();
        List<String> commands = new ArrayList<>();
        MenuScriptUtils.unpackHiddenLinesFromLore(loreStrings);

        for (String loreString : loreStrings) {
            if (loreString.startsWith(commandStart)
                    || loreString.startsWith(playerCommand)) {
                commands.add(loreString);
            } else if (loreString.startsWith(hiddenCommand)
                    || loreString.startsWith(hiddenPlayerCommand)) {
                // Legacy command hiding
                commands.add(unpackHiddenText(loreString));
            } else {
                comments.append(packHiddenText(loreString)).append(ChatColor.COLOR_CHAR).append('\r');
            }
        }

        // and condense them into the first line
        loreStrings = commands;
        if (loreStrings.isEmpty()) {
            loreStrings.add(comments.toString());
        } else {
            loreStrings.set(0, comments.toString() + loreStrings.get(0));
        }

        sender.sendMessage(plugin.translate(sender, "script-commands-shown", "All commands on this menu item should now be visible"));

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
