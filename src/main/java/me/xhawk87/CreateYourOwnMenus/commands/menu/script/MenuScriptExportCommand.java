/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
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
    public boolean onCommand(final CommandSender sender, Player target, Command command, String label, String[] args) {
        // Check the player is holding the item
        ItemStack held = target.getItemInHand();
        if (held == null || held.getTypeId() == 0) {
            sender.sendMessage(plugin.translate(sender, "error-no-item-in-hand", "You must be holding a menu item"));
            return true;
        }

        ItemMeta meta = held.getItemMeta();
        if (!meta.hasLore()) {
            sender.sendMessage(plugin.translate(sender, "export-no-lore", "This item has no lore to export"));
            return true;
        }

        List<String> lore = meta.getLore();
        if (lore.isEmpty()) {
            sender.sendMessage(plugin.translate(sender, "export-no-lore", "This item has no lore to export"));
            return true;
        }
        final List<String> lines = new ArrayList<>();
        lines.add(meta.hasDisplayName() ? meta.getDisplayName() : "");
        lines.addAll(MenuScriptUtils.unpackHiddenLines(lore.get(0)));
        lines.addAll(lore.subList(1, lore.size()));

        if (args.length < 1) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i + 1 < args.length) {
                sb.append(' ');
            }
        }
        sb.append(".txt");
        String filename = sb.toString();
        final File scriptsFolder = new File(plugin.getDataFolder(), "scripts");
        final File scriptFile = new File(scriptsFolder, filename);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!scriptsFolder.exists()) {
                    scriptsFolder.mkdirs();
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
