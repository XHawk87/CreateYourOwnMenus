/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.utils.InventoryReport;
import me.xhawk87.CreateYourOwnMenus.utils.ValidationUtils;
import org.bukkit.Location;
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
@Deprecated
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
            Inventory inv = chest.getInventory();
            InventoryReport chestData;
            try {
                chestData = InventoryReport.getReport(inv, slots);
            } catch (IllegalArgumentException ex) {
                player.sendMessage(ex.getLocalizedMessage());
                return false;
            }
            if (!chestData.hasType()) {
                player.sendMessage("This item is no longer being stocked");
                return false;
            }
            if (chestData.getCount() <= amount) {
                player.sendMessage("There are not enough " + chestData.toString() + " in stock");
                return false;
            }
            int count = 0;
            PlayerInventory to = player.getInventory();
            List<ItemStack> toDrop = new ArrayList<>();
            for (Map.Entry<Integer, ItemStack> entry : chestData.getItems().entrySet()) {
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
