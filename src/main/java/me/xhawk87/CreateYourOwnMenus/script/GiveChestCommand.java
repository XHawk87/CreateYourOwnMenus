/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.Menu;
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

        String slotsString = args[3];
        List<Integer> slots = new ArrayList<>();
        String[] slotStrings = slotsString.split(",");
        for (String slotString : slotStrings) {
            if (slotString.contains("-")) {
                String[] fromToStrings = slotString.split("-");
                if (fromToStrings.length != 2) {
                    player.sendMessage("Error in menu script line (expected slot range from-to): " + slotString);
                    return false;
                }
                int fromSlot;
                try {
                    fromSlot = Integer.parseInt(fromToStrings[0]);
                    if (fromSlot < 0) {
                        player.sendMessage("Error in menu script line (expected positive slot range from number): " + fromSlot);
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage("Error in menu script line (expected slot range from as whole number): " + fromToStrings[0]);
                    return false;
                }
                int toSlot;
                try {
                    toSlot = Integer.parseInt(fromToStrings[1]);
                    if (fromSlot <= toSlot) {
                        player.sendMessage("Error in menu script line (expected slot range to number higher than from number " + fromSlot + "): " + toSlot);
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage("Error in menu script line (expected slot range to as whole number): " + fromToStrings[1]);
                    return false;
                }
                for (int i = fromSlot; i <= toSlot; i++) {
                    slots.add(i);
                }
            } else {
                try {
                    int slotNumber = Integer.parseInt(slotString);
                    if (slotNumber < 0) {
                        player.sendMessage("Error in menu script line (expected positive slot number): " + slotNumber);
                        return false;
                    }
                    slots.add(slotNumber);
                } catch (NumberFormatException ex) {
                    player.sendMessage("Error in menu script line (expected whole number for slot): " + slotString);
                    return false;
                }
            }
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

            int spaces = 0;
            int empties = 0;
            int maxStackSize = 0;
            ItemStack type = null;
            for (int slot : slots) {
                if (slot >= toInv.getSize()) {
                    player.sendMessage("Error in menu script line (expected slot number less than inventory size " + toInv.getSize() + "): " + slot);
                    return false;
                }
                ItemStack item = toInv.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (type == null || item.isSimilar(item)) {
                        if (type == null) {
                            type = item;
                            maxStackSize = type.getMaxStackSize();
                            if (maxStackSize == -1) {
                                maxStackSize = 64;
                            }
                        }
                        spaces += maxStackSize - item.getAmount();
                    } else {
                        player.sendMessage("Error in menu script line (expected similar items in slots " + type.toString() + "): " + item.toString());
                        return false;
                    }
                } else {
                    empties++;
                }
            }

            if (type == null) {
                player.sendMessage("This item is no longer being stocked");
                return false;
            }
            spaces += empties * maxStackSize;
            if (spaces < amount) {
                player.sendMessage("This item is already fully stocked");
                return false;
            }

            int count = 0;
            List<Integer> toGive = new ArrayList<>();
            PlayerInventory fromInv = player.getInventory();
            for (int slot = 0; slot < fromInv.getSize(); slot++) {
                ItemStack item = fromInv.getItem(slot);
                if (item != null && item.isSimilar(type)) {
                    count += item.getAmount();
                    toGive.add(slot);
                    if (count >= amount) {
                        break;
                    }
                }
            }
            if (count < amount) {
                player.sendMessage("You do not have enough of this item");
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
                if (toItem == null) {
                    toInv.setItem(toSlot, fromItem);
                    count += fromItem.getAmount();
                    if (itFrom.hasNext()) {
                        fromSlot = itFrom.next();
                    }
                } else {
                    int space = maxStackSize - toItem.getAmount();
                    if (fromItem.getAmount() > space) {
                        fromItem.setAmount(fromItem.getAmount() - space);
                        toItem.setAmount(maxStackSize);
                        count += space;
                        if (itTo.hasNext()) {
                            toSlot = itTo.next();
                        }
                    } else {
                        toItem.setAmount(toItem.getAmount() + fromItem.getAmount());
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
