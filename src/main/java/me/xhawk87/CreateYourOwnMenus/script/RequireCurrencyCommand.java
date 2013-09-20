/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.Iterator;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.EconomyWrapper;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
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
            player.sendMessage("The /requirecurrency special command requires Vault to work");
            return false;
        }
        if (args.length != 1) {
            player.sendMessage("Error in menu script line (expected currency amount): " + command);
            return false;
        }
        String amountString = args[0];
        try {
            double amount = Double.parseDouble(amountString);
            if (economy.getBalance(player.getName()) < amount) {
                player.sendMessage("You must have at least " + economy.format(amount) + ChatColor.RESET + " to do this");
                return false;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage("Error in menu script line (expected currency amount): " + command);
            return false;
        }
        return true;
    }
}
