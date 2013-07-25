/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.Location;
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

    private CreateYourOwnMenus plugin;

    public MenuGrabCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getUsage() {
        return "/menu grab ([player]) [menu id]. Gives copies of all items in the given menu to the specified player (or the sender if no player is given)";
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
                sender.sendMessage("Console must specify a player");
                return true;
            }
        } else {
            String playerName = args[index++];
            target = plugin.getServer().getPlayer(playerName);
            if (target == null) {
                sender.sendMessage("No player matching " + playerName);
                return true;
            }
        }
        String menuId = args[index++];
        Menu menu = plugin.getMenu(menuId);
        if (menu == null) {
            sender.sendMessage("There is no menu matching " + menuId);
            return true;
        }
        PlayerInventory inv = target.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : menu.getInventory().getContents()) {
            if (item != null && item.getTypeId() != 0) {
                items.add(item.clone());
            }
        }
        HashMap<Integer, ItemStack> toDrop = inv.addItem(items.toArray(new ItemStack[items.size()]));
        World world = target.getWorld();
        Location location = target.getLocation();
        for (ItemStack drop : toDrop.values()) {
            world.dropItem(location, drop);
        }
        sender.sendMessage(menu.getTitle() + " was grabbed for " + target.getDisplayName());
        return true;
    }
}
