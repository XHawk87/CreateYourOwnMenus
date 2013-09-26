/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import java.util.Iterator;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author XHawk87
 */
public class RequireLevelCommand implements ScriptCommand {

    public RequireLevelCommand() {
    }

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem, Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        if (args.length != 1) {
            player.sendMessage("Error in menu script line (expected experience level): " + command);
            return false;
        }
        String levelString = args[0];
        try {
            int level = Integer.parseInt(levelString);
            if (level < 0) {
                player.sendMessage("Error in menu script line (expected positive experience level): " + level);
                return false;
            }
            return player.getLevel() >= level;
        } catch (NumberFormatException ex) {
            player.sendMessage("Error in menu script line (expected experience level amount): " + command);
            return false;
        }
    }
}
