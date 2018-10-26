/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu;

import java.util.*;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author XHawk87
 */
public class MenuGrabCommand implements IMenuCommand {

    private static final Set<Material> boots = EnumSet.of(
            Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS);
    private static final Set<Material> leggings = EnumSet.of(
            Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
            Material.DIAMOND_LEGGINGS);
    private static final Set<Material> chestplates = EnumSet.of(
            Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE);
    private static final Set<Material> helmets = EnumSet.of(
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET, Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET, Material.PUMPKIN, Material.LEGACY_SKULL);
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
        if (args.length < 1 || args.length > 2) {
            return false;
        }
        Player target;
        int index = 0;
        if (args.length == 1) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(plugin.translate(sender, "console-no-target", "The console must specify a player"));
                return true;
            }
        } else {
            String playerName = args[index++];
            target = plugin.getServer().getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(plugin.translate(sender, "player-not-online", "{0} is not online", playerName));
                return true;
            }
        }
        String menuId = args[index++];
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
            if (item != null && item.getType().getId() != 0) {
                if (i < inv.getSize()
                        || (i == 36 && isBoots(item))
                        || (i == 37 && isLeggings(item))
                        || (i == 38 && isChestplate(item))
                        || (i == 39 && isHelmet(item))) {
                    ItemStack replaced = inv.getItem(i);
                    if (replaced != null && replaced.getType().getId() != 0) {
                        toAdd.add(replaced);
                    }
                    inv.setItem(i, item.clone());
                } else {
                    toAdd.add(item.clone());
                }
            }
        }
        HashMap<Integer, ItemStack> toDrop = inv.addItem(toAdd.toArray(new ItemStack[toAdd.size()]));
        World world = target.getWorld();
        Location location = target.getLocation();
        for (ItemStack drop : toDrop.values()) {
            world.dropItem(location, drop);
        }
        sender.sendMessage(plugin.translate(sender, "menu-grabbed-for-player", "{0} was grabbed for {1}", menu.getTitle(), target.getDisplayName()));

        // Force inventory to update
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
