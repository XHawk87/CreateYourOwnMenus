/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.script;

import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

/**
 * @author XHawk87
 */
public class RequirePermissionCommand implements ScriptCommand {

    @Override
    public boolean execute(Menu menu, Player player, String[] args, String command, ItemStack menuItem, Iterator<String> commands, Player targetPlayer, Block targetBlock) {
        if (args.length != 1) {
            player.sendMessage(menu.translate(player, "error-expected-perm", "Error in menu script line (expected permission node): {0}", command));
            return false;
        }
        String permission = args[0];
        if (!player.hasPermission(permission)) {
            player.sendMessage(menu.translate(player, "no-item-perms", "You do not have permission to use this menu item"));
            return false;
        }
        return true;
    }
}
