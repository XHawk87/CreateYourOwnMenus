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
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandSender;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandUtils;
import me.xhawk87.CreateYourOwnMenus.utils.MenuSlotItemStackRef;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

        MenuSlotItemStackRef targetRef = MenuCommandUtils.getMenuSlotItemStack(plugin, sender, targetMenuId, targetSlotStr);
        if (targetRef == null) {
            return true;
        }
        MenuSlotItemStackRef toRef = MenuCommandUtils.getMenuSlotItemStack(plugin, sender, toMenuId, toSlotStr);
        if (toRef == null) {
            return true;
        }
        ItemStack to = toRef.get();
        if (to == null || to.getType() == Material.AIR) {
            targetRef.set(null);
        } else {
            targetRef.set(to.clone());
        }
        targetRef.update();
        if (sender instanceof MenuCommandSender) {
            // If this is running in a menu, it is assumed that the clicking player does not want to receive this message
            plugin.getServer().getConsoleSender().sendMessage(plugin.translate(sender, "menu-set-success",
                    "Slot {0} of {1} was set to item {2} in {3}", targetSlotStr, targetMenuId, toSlotStr, toMenuId));
        } else {
            sender.sendMessage(plugin.translate(sender, "menu-set-success", "Slot {0} of {1} was set to item {2} in {3}", targetSlotStr, targetMenuId, toSlotStr, toMenuId));
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
