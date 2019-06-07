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
package me.xhawk87.CreateYourOwnMenus.script;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * @author XHawk87
 */
public class DelayCommand implements ScriptCommand {

    private CreateYourOwnMenus plugin;

    public DelayCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final Menu menu, final Player player, String[] args, String command, final ItemStack menuItem, final Iterator<String> commands, final Player targetPlayer, final Block targetBlock) {
        if (args.length != 1) {
            player.sendMessage(plugin.translate(player, "error-no-delay", "Error in menu script line (expected delay in ticks): {0}", command));
            return false;
        }
        try {
            int delay = Integer.parseInt(args[0]);
            new BukkitRunnable() {
                @Override
                public void run() {
                    menu.parseCommands(commands, player, menuItem, targetPlayer, targetBlock);
                }
            }.runTaskLater(plugin, delay);
            return false;
        } catch (NumberFormatException ex) {
            player.sendMessage(plugin.translate(player, "expected-integer-delay", "Error in menu script line (delay must be a whole number of ticks): {0}", command));
            return false;
        }
    }
}
