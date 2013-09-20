/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.Iterator;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author XHawk87
 */
public class DelayCommand implements ScriptCommand {

    private CreateYourOwnMenus plugin;

    public DelayCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final Menu menu, final Player player, String[] args, String command, final ItemStack menuItem, final Iterator<String> commands, final Player targetPlayer, final Block targetBlock) {
        if (args.length != 1) {
            player.sendMessage("Error in menu script line (expected delay in ticks): " + command);
            return false;
        }
        try {
            int delay = Integer.parseInt(args[0]);
            new BukkitRunnable() {
                @Override
                public void run() {
                    menu.parseCommands(commands, player, menuItem, targetPlayer, targetBlock);
                }
            }.runTaskLater(plugin, delay);
            return false;
        } catch (NumberFormatException ex) {
            player.sendMessage("Error in menu script line (delay must be a whole number of ticks): " + command);
            return false;
        }
    }
}
