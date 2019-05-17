/*
 * Copyright 2015 XHawk87.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandSender;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author XHawk87
 */
public class MenuSetCommand implements IMenuCommand {

    private final CreateYourOwnMenus plugin;

    public MenuSetCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage(plugin.translate(sender, "menu-set-usage-extended", "/menu set [target menu id] [target slot#] [source menu id] [source slot#] - Set the target slot of the given menu to the menu item in the given slot of the specified menu. This can be useful in making somewhat dynamic menus, changing individual items in a menu by command from a template menu. If there is an existing item it will be overwritten, if the slot it is to be set to is empty, any existing item will be removed."));
            return true;
        }

        if (args.length != 4) {
            return false;
        }

        String targetMenuId = args[0];
        String targetSlotStr = args[1];
        String toMenuId = args[2];
        String toSlotStr = args[3];

        Menu targetMenu = plugin.getMenu(targetMenuId);
        if (targetMenu == null) {
            sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", targetMenuId));
            return true;
        }

        int targetSlot;
        try {
            targetSlot = Integer.parseInt(targetSlotStr);
        } catch (NumberFormatException ex) {
            sender.sendMessage(plugin.translate(sender, "expected-integer-slot", "The slot should be an integer number: {0}", targetSlotStr));
            return true;
        }

        int size = targetMenu.getInventory().getSize();
        if (targetSlot < 0 || targetSlot >= size) {
            sender.sendMessage(plugin.translate(sender, "invalid-slot-number", "The menu {0} has {1} slots numbered from 0 to {2}, so {3} is out of range", targetMenuId, size, size - 1, targetSlotStr));
            return true;
        }

        Menu toMenu = plugin.getMenu(toMenuId);
        if (toMenu == null) {
            sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", toMenuId));
            return true;
        }

        int toSlot;
        try {
            toSlot = Integer.parseInt(toSlotStr);
        } catch (NumberFormatException ex) {
            sender.sendMessage(plugin.translate(sender, "expected-integer-slot", "The slot should be an integer number: {0}", toSlotStr));
            return true;
        }

        size = toMenu.getInventory().getSize();
        if (toSlot < 0 || toSlot >= size) {
            sender.sendMessage(plugin.translate(sender, "invalid-slot-number", "The menu {0} has {1} slots numbered from 0 to {2}, so {3} is out of range", toMenuId, size, size - 1, toSlotStr));
            return true;
        }

        ItemStack to = toMenu.getInventory().getItem(toSlot);
        if (to == null || to.getType() == Material.AIR) {
            targetMenu.getInventory().clear(targetSlot);
        } else {
            targetMenu.getInventory().setItem(targetSlot, to.clone());
        }
        targetMenu.save();
        for (HumanEntity viewer : targetMenu.getInventory().getViewers()) {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                player.updateInventory();
            }
        }

        if (sender instanceof MenuCommandSender) {
            // If this is running in a menu, it is assumed that the clicking player does not want to receive this message
            plugin.getServer().getConsoleSender().sendMessage(plugin.translate(sender, "menu-set-success", "Slot {0} of {1} was set to item {2} in {3}", targetSlot, targetMenuId, toSlot, toMenuId));
        } else {
            sender.sendMessage(plugin.translate(sender, "menu-set-success", "Slot {0} of {1} was set to item {2} in {3}", targetSlot, targetMenuId, toSlot, toMenuId));
        }
        return true;
    }

    @Override
    public String getUsage() {
        return "/menu set [target menu id] [target slot#] [source menu id] [source slot#] - Set the target slot of the given menu to the menu item in the given slot of the specified menu";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.set";
    }
}
