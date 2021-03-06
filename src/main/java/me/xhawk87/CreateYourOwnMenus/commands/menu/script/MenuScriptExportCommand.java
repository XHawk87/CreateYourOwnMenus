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
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author XHawk87
 */
public class MenuScriptExportCommand extends IMenuScriptCommand {

    public MenuScriptExportCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) export [filename] - Saves all lore in the held item to the given file. Ampersand (&) is used as an alternate colour code";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.export";
    }

    @Override
    public boolean onCommand(final CommandSender sender, ItemStackRef itemStackRef, Command command, String label, String[] args) {
        ItemMeta meta = itemStackRef.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore().isEmpty()) {
            sender.sendMessage(plugin.translate(sender, "export-no-lore", "This item has no lore to export"));
            return true;
        }
        List<String> lore = meta.getLore();
        final List<String> lines = new ArrayList<>();
        lines.add(meta.hasDisplayName() ? meta.getDisplayName() : "");
        lines.addAll(MenuScriptUtils.unpackHiddenLines(lore.get(0)));
        lines.addAll(lore.subList(1, lore.size()));

        if (args.length < 1) {
            return false;
        }

        String filename = StringUtils.join(args, " ") + ".txt";
        final File scriptsFolder = new File(plugin.getDataFolder(), "scripts");
        final File scriptFile = new File(scriptsFolder, filename);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!scriptsFolder.exists()) {
                    if (!scriptsFolder.mkdirs()) {
                        sender.sendMessage(plugin.translate(sender, "export-save-error", "An error occurred while attempting to write to {0}. Please see console for details", scriptFile.getPath()));
                        plugin.getLogger().log(Level.SEVERE, "An error occurred while attempting to write to " + scriptFile.getPath(), "Unable to create directory");
                        return;
                    }
                }
                try (BufferedWriter out = new BufferedWriter(new FileWriter(scriptFile))) {
                    for (String line : lines) {
                        out.write(line.replace(ChatColor.COLOR_CHAR, '&'));
                        out.newLine();
                    }
                } catch (IOException ex) {
                    sender.sendMessage(plugin.translate(sender, "export-save-error", "An error occurred while attempting to write to {0}. Please see console for details", scriptFile.getPath()));
                    plugin.getLogger().log(Level.SEVERE, "An error occurred while attempting to write to " + scriptFile.getPath(), ex);
                    return;
                }
                sender.sendMessage(plugin.translate(sender, "script-exported", "Script was successfully exported to {0}", scriptFile.getPath()));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
