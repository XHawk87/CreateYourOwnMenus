/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.IMenuCommand;
import me.xhawk87.CreateYourOwnMenus.utils.TextCallback;
import me.xhawk87.CreateYourOwnMenus.utils.TextFileLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptImportCommand implements IMenuCommand {

    private CreateYourOwnMenus plugin;

    public MenuScriptImportCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getUsage() {
        return "/menu script import [filename] - Replaces all lore in the held item with the text from the given file. Ampersand (&) can be used as an alternate colour code";
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
            File scriptsFolder = new File(plugin.getDataFolder(), "scripts");
            final File scriptFile = new File(scriptsFolder, filename);
            new TextFileLoader(plugin, scriptFile, new TextCallback() {
                @Override
                public void onLoad(List<String> lines) {
                    ItemStack held = player.getItemInHand();
                    if (held == null || held.getTypeId() == 0) {
                        player.sendMessage("You must be holding a menu item");
                        return;
                    }
                    ItemMeta meta = held.getItemMeta();
                    meta.setLore(lines);
                    held.setItemMeta(meta);
                    player.sendMessage("Import successful");
                }

                @Override
                public void fail(Exception ex) {
                    if (ex instanceof FileNotFoundException) {
                        player.sendMessage("Could not find " + scriptFile.getPath());
                    } else {
                        player.sendMessage("An error occurred while attempting to read " + scriptFile.getPath() + ". Please see console for details.");
                        plugin.getLogger().log(Level.WARNING, "Error occured while attempting to read " + scriptFile.getPath(), ex);
                    }
                }
            }).load();
            return true;
        } else {
            sender.sendMessage("You must be logged in to import a menu script");
            return true;
        }
    }
}
