/*
 * Copyright (C) 2013-2019 XHawk87
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author XHawk87
 */
public class MenuScriptTitleCommand extends IMenuScriptCommand {

    public MenuScriptTitleCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) title [text] - Sets the held item's display name to the given text. May include formatting codes (&)";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.title";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStackRef itemStackRef, Command command, String label, String[] args) {
        ItemMeta meta = itemStackRef.getItemMeta();
        if (meta == null) {
            sender.sendMessage(plugin.translate(sender, "invalid-menu-item", "This is not a valid menu item"));
            return true;
        }

        // Expecting one or more parameters that make up new title
        StringBuilder sb = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            sb.append(" ").append(args[i]);
        }
        String titleString = sb.toString();

        // Append to the lore
        if (!titleString.startsWith("/")) {
            // Support for colour codes in non-commands
            titleString = titleString.replace('&', ChatColor.COLOR_CHAR);
        }
        // Otherwise append this to the lore

        sender.sendMessage(plugin.translate(sender, "item-title-set", "The title of this menu item is now {0}", titleString));

        // Update the item
        meta.setDisplayName(titleString);
        itemStackRef.setItemMeta(meta);
        return true;
    }
}
