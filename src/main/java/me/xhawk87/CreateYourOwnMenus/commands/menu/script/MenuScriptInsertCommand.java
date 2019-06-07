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
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptIndexedModifyLoreCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author XHawk87
 */
public class MenuScriptInsertCommand extends IMenuScriptIndexedModifyLoreCommand {

    public MenuScriptInsertCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) insert [index] [text] - Inserts the given text at line with the given index (0 for before first) in the held item's lore";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.insert";
    }

    @Override
    public boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, int index,
                             Command command, String label, String[] args) {
        // Expecting one or more parameters that make up the command or comment to add
        String commandString = StringUtils.join(args, " ", 1, args.length);
        commandString = MenuScriptUtils.replaceColorSymbol(commandString);

        if (index == 0) {
            // Handle first-line special case
            String firstLine = loreStrings.get(0);
            int lastPartIndex = firstLine.lastIndexOf("\r") + 1;
            String displacedText = firstLine.substring(lastPartIndex);
            loreStrings.set(0, firstLine.substring(0, lastPartIndex) + commandString);
            loreStrings.add(1, displacedText);
        } else {
            loreStrings.add(index, commandString);
        }

        sender.sendMessage(plugin.translate(sender, "script-inserted", "{0} was inserted before line {1} in the command list of this menu item", commandString, index));

        // Update the item
        meta.setLore(loreStrings);
        held.setItemMeta(meta);
        return true;
    }
}
