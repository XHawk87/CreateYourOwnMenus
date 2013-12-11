/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.utils.InventoryReport;
import me.xhawk87.CreateYourOwnMenus.utils.ValidationUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author XHawk87
 */
@Deprecated
public class GiveChestCommand implements ScriptCommand {

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem, Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        if (args.length != 5) {
            player.sendMessage("Error in menu script line (expected /GiveChest [x] [y] [z] [slot1,fromSlot-toSlot...] [amount]): " + command);
            return false;
        }

        int x, y, z;

        try {
            x = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            player.sendMessage("Error in menu script line (expected whole number for x): " + args[0]);
            return false;
        }

        try {
            y = Integer.parseInt(args[1]);
            if (y < 0 || y > 255) {
                player.sendMessage("Error in menu script line (expected y between 0 and 255): " + args[2]);
                return false;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage("Error in menu script line (expected whole number for y): " + args[1]);
            return false;
        }

        try {
            z = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            player.sendMessage("Error in menu script line (expected whole number for z): " + args[2]);
            return false;
        }

        List<Integer> slots;
        try {
            slots = ValidationUtils.getSlotRange(args[3]);
        } catch (IllegalArgumentException ex) {
            player.sendMessage("Error in menu script line " + ex.getLocalizedMessage());
            return false;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[4]);
            if (amount < 1) {
                player.sendMessage("Error in menu script line (expected amount of at least 1): " + args[4]);
                return false;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage("Error in menu script line (expected whole number for amount): " + args[4]);
            return false;
        }

        Block block = player.getWorld().getBlockAt(x, y, z);
        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder chest = (InventoryHolder) block.getState();
            Inventory toInv = chest.getInventory();
            InventoryReport chestData;
            try {
                chestData = InventoryReport.getReport(toInv, slots);
            } catch (IllegalArgumentException ex) {
                player.sendMessage(ex.getLocalizedMessage());
                return false;
            }

            if (!chestData.hasType()) {
                player.sendMessage("This item is no longer being stocked");
                return false;
            }
            if (chestData.getSpaces() < amount) {
                player.sendMessage(chestData.toString() + " is already fully stocked");
                return false;
            }

            int count = 0;
            List<Integer> toGive = new ArrayList<>();
            PlayerInventory fromInv = player.getInventory();
            for (int slot = 0; slot < fromInv.getSize(); slot++) {
                ItemStack item = fromInv.getItem(slot);
                if (item != null && item.isSimilar(chestData.getType())) {
                    count += item.getAmount();
                    toGive.add(slot);
                    if (count >= amount) {
                        break;
                    }
                }
            }
            if (count < amount) {
                player.sendMessage("You do not have enough " + chestData.toString());
                return false;
            }

            count = 0;
            Iterator<Integer> itTo = slots.iterator();
            Iterator<Integer> itFrom = toGive.iterator();
            int toSlot = itTo.next();
            int fromSlot = itFrom.next();
            while (count < amount) {
                ItemStack fromItem = fromInv.getItem(fromSlot);
                ItemStack toItem = toInv.getItem(toSlot);
                if (toItem == null || toItem.getType() == Material.AIR) {
                    // toSlot is empty
                    if (count + fromItem.getAmount() > amount) {
                        // Don't take more than required
                        int moving = amount - count;
                        toItem = fromItem.clone();
                        toItem.setAmount(moving);
                        fromItem.setAmount(fromItem.getAmount() - moving);
                        fromInv.setItem(fromSlot, fromItem);
                        toInv.setItem(toSlot, toItem);
                        count += moving;
                    } else {
                        // moving fromItem to toSlot
                        toInv.setItem(toSlot, fromItem);
                        count += fromItem.getAmount();
                        fromInv.clear(fromSlot);
                        if (itFrom.hasNext()) {
                            fromSlot = itFrom.next();
                        }
                    }
                } else {
                    // toSlot is occupied, find out how much can move
                    int moving = chestData.getMaxStackSize() - toItem.getAmount();

                    // Don't take more than required
                    if (count + moving > amount) {
                        moving = amount - count;
                    }

                    if (fromItem.getAmount() > moving) {
                        // There are more in fromSlot than are needed/can fit
                        fromItem.setAmount(fromItem.getAmount() - moving);
                        fromInv.setItem(fromSlot, fromItem);
                        toItem.setAmount(toItem.getAmount() + moving);
                        toInv.setItem(toSlot, toItem);
                        count += moving;
                        if (itTo.hasNext()) {
                            toSlot = itTo.next();
                        }
                    } else {
                        // Combine the two stacks into toSlot
                        toItem.setAmount(toItem.getAmount() + fromItem.getAmount());
                        toInv.setItem(toSlot, toItem);
                        count += fromItem.getAmount();
                        fromInv.clear(fromSlot);
                        if (itFrom.hasNext()) {
                            fromSlot = itFrom.next();
                        }
                    }
                }
            }

            player.updateInventory();
            for (HumanEntity viewer : chest.getInventory().getViewers()) {
                if (viewer instanceof Player) {
                    ((Player) viewer).updateInventory();
                }
            }
            return true;
        } else {
            player.sendMessage("Error in menu script line (expected chest at " + x + "," + y + "," + z + "): " + block.getType().name());
            return false;
        }
    }
}
