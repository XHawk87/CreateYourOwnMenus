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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Iterator;

/**
 * @author XHawk87
 */
public class ConsumeCommand implements ScriptCommand {

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem, Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        if (args.length != 0) {
            player.sendMessage(menu.translate(player, "expected-no-args", "Error in menu script line (expected no arguments): {0}", command));
            return false;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack held = inv.getItemInMainHand();
        if (held.equals(menuItem)) {
            int amount = menuItem.getAmount() - 1;
            if (amount > 0) {
                menuItem.setAmount(amount);
                inv.setItemInMainHand(menuItem);
            } else {
                inv.clear(inv.getHeldItemSlot());
            }
        } else {
            player.sendMessage(menu.translate(player, "consume-no-item", "Cannot locate menu item to remove it. Was it moved?"));
            return false;
        }

        //noinspection deprecation
        player.updateInventory();
        return true;
    }
}
