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
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
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
public class MenuScriptImportCommand extends IMenuScriptCommand {

    private CreateYourOwnMenus plugin;

    public MenuScriptImportCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getUsage() {
        return "/menu script ([player]) import [filename] - Replaces all lore in the held item with the text from the given file. Ampersand (&) can be used as an alternate colour code";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Player target, Command command, String label, String[] args) {
        // Check the player is holding the item
        ItemStack held = target.getItemInHand();
        if (held == null || held.getTypeId() == 0) {
            sender.sendMessage("You must be holding a menu item");
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
                ItemStack held = target.getItemInHand();
                if (held == null || held.getTypeId() == 0) {
                    sender.sendMessage("You must be holding a menu item");
                    return;
                }
                ItemMeta meta = held.getItemMeta();
                meta.setDisplayName(lines.get(0));
                meta.setLore(lines.subList(1, lines.size()));
                held.setItemMeta(meta);
                sender.sendMessage("Import successful");
            }

            @Override
            public void fail(Exception ex) {
                if (ex instanceof FileNotFoundException) {
                    sender.sendMessage("Could not find " + scriptFile.getPath());
                } else {
                    sender.sendMessage("An error occurred while attempting to read " + scriptFile.getPath() + ". Please see console for details.");
                    plugin.getLogger().log(Level.WARNING, "Error occured while attempting to read " + scriptFile.getPath(), ex);
                }
            }
        }).load();
        return true;
    }
}
