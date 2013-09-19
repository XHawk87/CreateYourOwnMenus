/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
public class TakeChestCommand implements ScriptCommand {

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem, Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        if (args.length != 5) {
            player.sendMessage("Error in menu script line (expected /TakeChest [x] [y] [z] [slot1,fromSlot-toSlot...] [amount]): " + command);
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
            Inventory inv = chest.getInventory();
            int count = 0;
            ItemStack type = null;
            Map<Integer, ItemStack> toTake = new HashMap<>();
            for (int slot : slots) {
                if (slot >= inv.getSize()) {
                    player.sendMessage("Error in menu script line (expected slot number less than inventory size " + inv.getSize() + "): " + slot);
                    return false;
                }
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (type == null || item.isSimilar(item)) {
                        type = item;
                        count += item.getAmount();
                        toTake.put(slot, item);
                        if (count >= amount) {
                            break;
                        }
                    } else {
                        player.sendMessage("Error in menu script line (expected similar items in slots " + type.toString() + "): " + item.toString());
                        return false;
                    }
                }
            }
            if (count <= amount) {
                player.sendMessage("There are not enough of these items in stock");
                return false;
            }
            count = 0;
            PlayerInventory to = player.getInventory();
            List<ItemStack> toDrop = new ArrayList<>();
            for (Map.Entry<Integer, ItemStack> entry : toTake.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                count += item.getAmount();
                if (count < amount) {
                    inv.clear(slot);
                    toDrop.addAll(to.addItem(item).values());
                } else {
                    int remaining = count - amount;
                    if (remaining > 0) {
                        ItemStack split = item.clone();
                        split.setAmount(item.getAmount() - remaining);
                        item.setAmount(remaining);
                        toDrop.addAll(to.addItem(split).values());
                    } else {
                        inv.clear(slot);
                        toDrop.addAll(to.addItem(item).values());
                    }
                    break;
                }
            }
            if (!toDrop.isEmpty()) {
                World world = player.getWorld();
                Location loc = player.getLocation();
                for (ItemStack drop : toDrop) {
                    world.dropItem(loc, drop);
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
