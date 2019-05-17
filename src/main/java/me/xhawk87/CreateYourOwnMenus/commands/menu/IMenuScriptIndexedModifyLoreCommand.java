package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class IMenuScriptIndexedModifyLoreCommand extends IMenuScriptModifyLoreCommand {
    public IMenuScriptIndexedModifyLoreCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    public abstract boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings,
                                      int index, Command command, String label, String[] args);

    @Override
    public boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        String indexString = args[0];
        int index = getIndex(indexString, loreStrings.size(), sender);
        if (index == -1) {
            return true;
        }
        return onCommand(sender, held, meta, loreStrings, index, command, label, args);
    }
}
