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
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Field;

/**
 * @author XHawk87
 */
public class MenuGrabCommand implements IMenuCommand {


    private static Material getMaterial(String... names) {
        for (String name : names) {
            try {
                Field field = Material.class.getField(name);
                return (Material) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return null;
    }

    private static Set<Material> createMaterialSet(Material... materials) {
        Set<Material> materialSet = EnumSet.noneOf(Material.class);
        for (Material material : materials) {
            if (material != null) {
                materialSet.add(material);
            }
        }
        return materialSet;
    }

    private static final Set<Material> boots = createMaterialSet(
            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS,
            getMaterial("GOLD_BOOTS", "GOLDEN_BOOTS"),
            Material.DIAMOND_BOOTS
    );

    private static final Set<Material> leggings = createMaterialSet(
            Material.LEATHER_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS,
            getMaterial("GOLD_LEGGINGS", "GOLDEN_LEGGINGS"),
            Material.DIAMOND_LEGGINGS
    );

    private static final Set<Material> chestplates = createMaterialSet(
            Material.LEATHER_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            getMaterial("GOLD_CHESTPLATE", "GOLDEN_CHESTPLATE"),
            Material.DIAMOND_CHESTPLATE
    );

    private static final Set<Material> helmets = createMaterialSet(
            Material.LEATHER_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET,
            getMaterial("GOLD_HELMET", "GOLDEN_HELMET"),
            Material.DIAMOND_HELMET,
            Material.PUMPKIN,
            getMaterial("SKULL_ITEM", "SKELETON_SKULL"),
            getMaterial("WITHER_SKULL", "WITHER_SKELETON_SKULL")
    );



    private CreateYourOwnMenus plugin;

    public MenuGrabCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getUsage() {
        return "/menu grab ([player]) [menu id]. Gives copies of all items in the given menu to the specified player (or the sender if no player is given). This will attempt to place items in the same location in the player's inventory as in the menu, starting with the top row as the hotbar.";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.grab";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return onCommand(sender, args[0]);
        }
        if (args.length == 2) {
            return onCommand(sender, args[0], args[1]);
        }
        return false;
    }

    private boolean onCommand(CommandSender sender, String menuId) {
        if (sender instanceof Player) {
            return onCommand(sender, (Player) sender, menuId);
        } else {
            sender.sendMessage(plugin.translate(sender, "console-no-target", "The console must specify a player"));
            return true;
        }
    }

    private boolean onCommand(CommandSender sender, String playerName, String menuId) {
        Player target = MenuCommandUtils.getPlayerByName(playerName);
        if (target != null) {
            return onCommand(sender, target, menuId);
        } else {
            sender.sendMessage(plugin.translate(sender, "player-not-online", "{0} is not online", playerName));
            return true;
        }
    }

    private boolean onCommand(CommandSender sender, Player target, String menuId) {
        Menu menu = plugin.getMenu(menuId);
        if (menu == null) {
            sender.sendMessage(plugin.translate(sender, "unknown-menu-id", "There is no menu with id {0}", menuId));
            return true;
        }
        PlayerInventory inv = target.getInventory();
        List<ItemStack> toAdd = new ArrayList<>();
        ItemStack[] contents = menu.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                if (i < inv.getSize()
                        || (i == 36 && isBoots(item))
                        || (i == 37 && isLeggings(item))
                        || (i == 38 && isChestplate(item))
                        || (i == 39 && isHelmet(item))) {
                    ItemStack replaced = inv.getItem(i);
                    if (replaced != null && replaced.getType() != Material.AIR) {
                        toAdd.add(replaced);
                    }
                    inv.setItem(i, item.clone());
                } else {
                    toAdd.add(item.clone());
                }
            }
        }
        Map<Integer, ItemStack> toDrop = inv.addItem(toAdd.toArray(new ItemStack[0]));
        World world = target.getWorld();
        Location location = target.getLocation();
        for (ItemStack drop : toDrop.values()) {
            world.dropItem(location, drop);
        }
        sender.sendMessage(plugin.translate(sender, "menu-grabbed-for-player", "{0} was grabbed for {1}", menu.getTitle(), target.getDisplayName()));

        // Force inventory to update
        //noinspection deprecation
        target.updateInventory();
        return true;
    }

    private boolean isBoots(ItemStack item) {
        return boots.contains(item.getType());
    }

    private boolean isLeggings(ItemStack item) {
        return leggings.contains(item.getType());
    }

    private boolean isChestplate(ItemStack item) {
        return chestplates.contains(item.getType());
    }

    private boolean isHelmet(ItemStack item) {
        return helmets.contains(item.getType());
    }
}
