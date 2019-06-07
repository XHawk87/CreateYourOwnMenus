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
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author XHawk87
 */
public abstract class IMenuScriptCommand implements IMenuCommand {

    protected CreateYourOwnMenus plugin;

    public IMenuScriptCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public abstract boolean onCommand(CommandSender sender, ItemStackRef itemStackRef, Command command, String label, String[] args);

    protected int getIndex(String indexString, int maxIndex, CommandSender sender) {
        int index;
        try {
            index = Integer.parseInt(indexString);
            if (index < 0) {
                sender.sendMessage(plugin.translate(sender, "error-negative-index", "The index must be at least 0: {0}", indexString));
                return -1;
            }
            if (index >= maxIndex) {
                sender.sendMessage(plugin.translate(sender, "error-index-too-high", "The index must be less than the number of lines in the lore ({0}): {1}", maxIndex, indexString));
                return -1;
            }
            return index;
        } catch (NumberFormatException ex) {
            sender.sendMessage(plugin.translate(sender, "expected-integer-index", "The index must be a whole number: {0}", indexString));
            return -1;
        }
    }
}
