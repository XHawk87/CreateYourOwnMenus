/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.commands;

import java.util.ArrayList;
import java.util.List;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptCommand implements IMenuCommand {

    private static final String hiddenCommand = ChatColor.COLOR_CHAR + "/";
    private static final String hiddenPlayerCommand = ChatColor.COLOR_CHAR + "@"
            + ChatColor.COLOR_CHAR + "p" + ChatColor.COLOR_CHAR + "/";
    private CreateYourOwnMenus plugin;

    public MenuScriptCommand(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Entering a sub-command without parameters is assumed to be a request 
        // for information. So display some detailed help.
        if (args.length == 0) {
            sender.sendMessage("/menu script command - Adds this command onto the end of the command list for the held menu item. Commands must start with a / otherwise it is interpretted as a comment. If any command fails to execute, none of the proceeding commands will execute. The @p symbol gets replaced with the player clicking the menu item on use. The special command /requirepermission [permission-node] will check if the clicking player has the given permission and if not, none of the proceeding commands will execute. The special command /close will close the current menu");
            sender.sendMessage("/menu script clear - Clears the commands list for the held menu item");
            return true;
        }

        // Expecting one or more parameters that make up the command or comment to add
        String commandString;
        {
            StringBuilder sb = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                sb.append(" ").append(args[i]);
            }
            commandString = sb.toString();
        }

        // Only players can hold items
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Check the player is holding the item
            ItemStack held = player.getItemInHand();
            if (held == null || held.getTypeId() == 0) {
                player.sendMessage("You must be holding a menu item");
                return true;
            }

            // Get or create the lore
            ItemMeta meta = held.getItemMeta();
            List<String> loreStrings;
            if (meta.hasLore()) {
                loreStrings = meta.getLore();
            } else {
                loreStrings = new ArrayList<>();
            }

            // Check if this is the special clear command
            if (commandString.equalsIgnoreCase("clear")) {
                loreStrings.clear();
                sender.sendMessage("The command list for this menu item has been cleared");
            } else if (commandString.equalsIgnoreCase("show")) {
                // Strip all color chars used to hide the command
                for (int i = 0; i < loreStrings.size(); i++) {
                    String loreString = loreStrings.get(i);
                    if (loreString.startsWith(hiddenCommand)
                            || loreString.startsWith(hiddenPlayerCommand)) {
                        StringBuilder sb = new StringBuilder();
                        for (char c : loreString.toCharArray()) {
                            if (c != ChatColor.COLOR_CHAR) {
                                sb.append(c);
                            }
                        }
                        loreStrings.set(i, sb.toString());
                    }
                }
                sender.sendMessage("All commands on this menu item should now be visible");
            } else if (commandString.equalsIgnoreCase("hide")) {
                // Place a color char in front of each char in order to hide the commands
                for (int i = 0; i < loreStrings.size(); i++) {
                    String loreString = loreStrings.get(i);
                    if (loreString.startsWith("/") || loreString.startsWith("@p/")) {
                        StringBuilder sb = new StringBuilder();
                        for (char c : loreString.toCharArray()) {
                            sb.append(ChatColor.COLOR_CHAR).append(c);
                        }
                        loreStrings.set(i, sb.toString());
                    }
                }
                sender.sendMessage("All commands on this menu item should now be hidden");
            } else {
                if (!commandString.startsWith("/")) {
                    // Support for colour codes in non-commands
                    commandString = commandString.replace('&', ChatColor.COLOR_CHAR);
                }
                // Otherwise append this to the lore
                loreStrings.add(commandString);
                sender.sendMessage(commandString + " was added to the command list of this menu item");
            }

            // Update the item
            meta.setLore(loreStrings);
            held.setItemMeta(meta);
            return true;
        } else {
            sender.sendMessage("You must be logged in to modify a menu item script");
            return true;
        }
    }

    @Override
    public String getUsage() {
        return "/menu script [clear|command] - Add a command to a held menu item or clear all commands from it";
    }

    @Override
    public String getPermission() {
        return "cyom.commands.menu.script";
    }
}
