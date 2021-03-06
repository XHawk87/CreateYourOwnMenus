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
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/**
 * @author XHawk87
 */
public class RequireLevelCommand implements ScriptCommand {

    private CreateYourOwnMenus plugin;

    public RequireLevelCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem, Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        if (args.length < 1) {
            player.sendMessage(plugin.translate(player, "require-level-usage", "Error in menu script line (expected /requirelevel ([player]) [level] ([fail message])): {0}", command));
            return false;
        }
        int index = 0;
        Player target;
        if (args.length > 1) {
            String playerName = args[index++];
            target = MenuCommandUtils.getPlayerByName(playerName);
            if (target == null) {
                target = player;
                index--;
            }
        } else {
            target = player;
        }

        int level;
        try {
            level = Integer.parseInt(args[index++]);
            if (level < 0) {
                player.sendMessage(plugin.translate(player, "error-integer-xp-level", "Error in menu script line (expected positive experience level): {0}", level));
                return false;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage(plugin.translate(player, "error-integer-xp-level", "Error in menu script line (expected positive experience level): {0}", command));
            return false;
        }

        if (target.getLevel() < level) {
            StringBuilder sb = new StringBuilder();
            while (index < args.length) {
                sb.append(args[index++]);
                if (index < args.length) {
                    sb.append(" ");
                }
            }
            String failMessage = sb.toString();
            if (!failMessage.isEmpty()) {
                player.sendMessage(failMessage);
            }
            return false;
        } else {
            return true;
        }
    }
}
