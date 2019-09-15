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

import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author XHawk87
 */
public class SudoCommand implements ScriptCommand {

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem,
                           Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        if (args.length < 1) {
            player.sendMessage(menu.translate(player, "sudo-no-target",
                    "Error in menu script line (No target player was specified): {0}", command));
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(menu.translate(player, "sudo-no-command",
                    "Error in menu script line (No command was specified to run as player): {0}", command));
            return false;
        }

        Player target = MenuCommandUtils.getPlayerByName(args[0]);
        if (target == null) {
            player.sendMessage(menu.translate(player, "player-not-online", "{0} is not online", args[0]));
            return true;
        }

        target.chat("/" + StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " "));
        return true;
    }
}
