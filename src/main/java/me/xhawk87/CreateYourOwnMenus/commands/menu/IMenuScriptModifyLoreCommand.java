package me.xhawk87.CreateYourOwnMenus.commands.menu;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.utils.ItemStackRef;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class IMenuScriptModifyLoreCommand extends IMenuScriptCommand {

    public IMenuScriptModifyLoreCommand(CreateYourOwnMenus plugin) {
        super(plugin);
    }

    public abstract boolean onCommand(CommandSender sender, ItemStack held, ItemMeta meta, List<String> loreStrings, Command command, String label, String[] args);

    @Override
    public boolean onCommand(CommandSender sender, ItemStackRef itemStackRef, Command command, String label, String[] args) {
        ItemStack held = itemStackRef.get();
        ItemMeta meta = held.getItemMeta();
        List<String> loreStrings = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
        return onCommand(sender, held, meta, loreStrings, command, label, args);
    }
}
