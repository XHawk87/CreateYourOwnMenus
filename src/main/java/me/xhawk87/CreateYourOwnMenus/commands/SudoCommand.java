/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author XHawk87
 */
public class SudoCommand implements CommandExecutor {

    private CreateYourOwnMenus plugin;

    public SudoCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cyom.commands.sudo")) {
            sender.sendMessage(plugin.translate(sender, "command-no-perms", "You do not have permission to use this command"));
            return true;
        }

        if (args.length == 0 || args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(plugin.translate(sender, "sudo-usage", "/sudo [player] [command...]. Send a command as the specified player. This can be used in place of @p/ in menu scripts e.g. '/sudo @p kill' in place of '@p/kill'"));
            return true;
        }
        if (args.length < 2) {
            return false;
        }

        String targetName = args[0];
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.translate(sender, "player-not-online", "{0} is not online", targetName));
            return true;
        }

        StringBuilder sb = new StringBuilder("/");
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length) {
                sb.append(" ");
            }
        }

        target.chat(sb.toString());
        return true;
    }
}
