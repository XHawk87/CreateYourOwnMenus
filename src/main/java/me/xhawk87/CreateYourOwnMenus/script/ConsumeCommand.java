/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.Iterator;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
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
        ItemStack held = inv.getItemInHand();
        if (held.equals(menuItem)) {
            int amount = menuItem.getAmount() - 1;
            if (amount > 0) {
                menuItem.setAmount(amount);
                inv.setItemInHand(menuItem);
            } else {
                inv.clear(inv.getHeldItemSlot());
            }
        } else {
            player.sendMessage(menu.translate(player, "consume-no-item", "Cannot locate menu item to remove it. Was it moved?"));
            return false;
        }

        player.updateInventory();
        return true;
    }
}
