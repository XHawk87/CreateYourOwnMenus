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
public class MenuScriptHideCommand extends IMenuScriptModifyLoreCommand {

    public MenuScriptHideCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) hide - Hides all commands for the menu item in your hand, and shows all comments";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.hide";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, Command command, String label, String[] args) {
        // Hide the commands
        StringBuilder commands = new StringBuilder();
        List<String> comments = new ArrayList<>();
        MenuScriptUtils.unpackHiddenLinesFromLore(loreStrings);

        for (String loreString : loreStrings) {
            if (loreString.startsWith(commandStart)
                    || loreString.startsWith(playerCommand)) {
                commands.append(packHiddenText(loreString)).append(ChatColor.COLOR_CHAR).append('\r');
            } else if (loreString.startsWith(hiddenCommand)
                    || loreString.startsWith(hiddenPlayerCommand)) {
                // Legacy command hiding
                commands.append(loreString).append(ChatColor.COLOR_CHAR).append('\r');
            } else {
                comments.add(loreString);
            }
        }

        // and condense them into the first line
        loreStrings = comments;
        if (loreStrings.isEmpty()) {
            loreStrings.add(commands.toString());
        } else {
            loreStrings.set(0, commands.toString() + loreStrings.get(0));
        }

        sender.sendMessage(plugin.translate(sender, "script-commands-hidden", "All commands on this menu item should now be hidden"));

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
