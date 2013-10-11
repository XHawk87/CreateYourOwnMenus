/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
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
public class MenuScriptExportCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuScriptExportCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getUsage() {
        return "/menu script export [filename] - Saves all lore in the held item to the given file. Ampersand (&) is used as an alternate colour code";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only players can hold items
        if (sender instanceof Player) {
            final Player player = (Player) sender;

            // Check the player is holding the item
            ItemStack held = player.getItemInHand();
            if (held == null || held.getTypeId() == 0) {
                player.sendMessage("You must be holding a menu item");
                return true;
            }

            ItemMeta meta = held.getItemMeta();
            if (!meta.hasLore()) {
                player.sendMessage("This item has no lore to export");
                return true;
            }
            
            List<String> lore = meta.getLore();
            if (lore.isEmpty()) {
                player.sendMessage("This item has no lore to export");
                return true;
            }
            final List<String> lines = MenuScriptUtils.unpackHiddenLines(lore.get(0));
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
                        player.sendMessage("An error occurred while attempting to write to " + scriptFile.getPath() + ". Please see console for details");
                        plugin.getLogger().log(Level.SEVERE, "An error occurred while attempting to write to " + scriptFile.getPath(), ex);
                        return;
                    }
                    player.sendMessage("Script was successfully exported to " + scriptFile.getPath());
                }
            }.runTaskAsynchronously(plugin);
            return true;
        } else {
            sender.sendMessage("You must be logged in to import a menu script");
            return true;
        }
    }
}
