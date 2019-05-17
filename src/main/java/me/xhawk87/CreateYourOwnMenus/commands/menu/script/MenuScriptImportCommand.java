/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands.menu.script;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.commands.menu.IMenuScriptCommand;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import me.xhawk87.CreateYourOwnMenus.utils.TextCallback;
import me.xhawk87.CreateYourOwnMenus.utils.TextFileLoader;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;

/**
 * @author XHawk87
 */
public class MenuScriptImportCommand extends IMenuScriptCommand {

    public MenuScriptImportCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    @Override
    public String getUsage() {
        return "/menu script ([player | menu#slot]) import [filename] - Replaces all lore in the held item with the text from the given file. Ampersand (&) can be used as an alternate colour code";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script.import";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final ItemStackRef itemStackRef, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        String filename = StringUtils.join(args, " ") + ".txt";
        File scriptsFolder = new File(plugin.getDataFolder(), "scripts");
        final File scriptFile = new File(scriptsFolder, filename);
        new TextFileLoader(plugin, scriptFile, new TextCallback() {
            @Override
            public void onLoad(List<String> lines) {
                if (MenuScriptUtils.isEmptyHand(itemStackRef, sender, plugin)) {
                    return;
                }
                ItemStack held = itemStackRef.get();
                ItemMeta meta = held.getItemMeta();
                meta.setDisplayName(lines.get(0));
                meta.setLore(lines.subList(1, lines.size()));
                held.setItemMeta(meta);
                sender.sendMessage(plugin.translate(sender, "script-imported", "Import successful"));
                itemStackRef.update();
            }

            @Override
            public void fail(Exception ex) {
                if (ex instanceof FileNotFoundException) {
                    sender.sendMessage(plugin.translate(sender, "import-file-not-found", "Could not find {0}", scriptFile.getPath()));
                } else {
                    sender.sendMessage(plugin.translate(sender, "import-load-error", "An error occurred while attempting to read {0}. Please see console for details.", scriptFile.getPath()));
                    plugin.getLogger().log(Level.WARNING, "Error occured while attempting to read " + scriptFile.getPath(), ex);
                }
            }
        }).load();
        return true;
    }
}
