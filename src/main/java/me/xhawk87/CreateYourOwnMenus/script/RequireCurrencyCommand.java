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
import me.xhawk87.CreateYourOwnMenus.EconomyWrapper;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/**
 * @author XHawk87
 */
public class RequireCurrencyCommand implements ScriptCommand {

    private CreateYourOwnMenus plugin;

    public RequireCurrencyCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem, Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        EconomyWrapper economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(plugin.translate(player, "vault-required", "The /requirecurrency special command requires Vault to work"));
            return false;
        }
        if (args.length != 1) {
            player.sendMessage(plugin.translate(player, "error-no-currency", "Error in menu script line (expected currency amount): {0}", command));
            return false;
        }
        String amountString = args[0];
        try {
            double amount = Double.parseDouble(amountString);
            if (economy.getBalance(player) < amount) {
                player.sendMessage(plugin.translate(player, "not-enough-currency", "You must have at least {0} to do this", economy.format(amount) + ChatColor.RESET));
                return false;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage(plugin.translate(player, "error-number-currency", "Error in menu script line (expected currency amount): {0}", command));
            return false;
        }
        return true;
    }
}
