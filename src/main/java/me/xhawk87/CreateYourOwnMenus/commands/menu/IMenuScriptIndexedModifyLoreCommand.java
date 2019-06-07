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
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class IMenuScriptIndexedModifyLoreCommand extends IMenuScriptModifyLoreCommand {
    public IMenuScriptIndexedModifyLoreCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    public abstract boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings,
                                      int index, Command command, String label, String[] args);

    @Override
    public boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        String indexString = args[0];
        int index = getIndex(indexString, loreStrings.size(), sender);
        if (index == -1) {
            return true;
        }
        return onCommand(sender, held, meta, loreStrings, index, command, label, args);
    }
}
