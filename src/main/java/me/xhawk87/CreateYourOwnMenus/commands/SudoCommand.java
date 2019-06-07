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
package me.xhawk87.CreateYourOwnMenus.commands;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
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

        target.chat(StringUtils.join(args, " "));
        return true;
    }
}
