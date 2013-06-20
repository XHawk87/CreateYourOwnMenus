/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.xhawk87.CreateYourOwnMenus.commands.MenuCommand;
import me.xhawk87.CreateYourOwnMenus.listeners.MenuListener;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A plugin to allow server owners to design and create their own menus in-game
 *
 * @author XHawk87
 */
public class CreateYourOwnMenus extends JavaPlugin {

    // Store menus by their ID
    private Map<String, Menu> menus = new HashMap<>();
    private Set<String> commandBlacklist = new HashSet<>();
    private Set<String> commandWhitelist = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Load in blacklisted and whitelisted commands from config
        for (String commandName : getConfig().getStringList("blacklist-commands")) {
            if (commandName.startsWith("/")) {
                commandName = commandName.substring(1);
            }
            commandBlacklist.add(commandName.toLowerCase());
        }
        for (String commandName : getConfig().getStringList("whitelist-commands")) {
            if (commandName.startsWith("/")) {
                commandName = commandName.substring(1);
            }
            commandWhitelist.add(commandName.toLowerCase());
        }

        // Load menus
        reloadMenus();

        // Register commands
        getCommand("menu").setExecutor(new MenuCommand(this));

        // Register listeners
        new MenuListener().registerEvents(this);
    }

    /**
     * Create a new menu with the given id, display title and number of rows for
     * the inventory.
     *
     * @param id A unique identifier for the menu
     * @param title The display title, may contain colour codes and spaces
     * @param rows The number of rows for the inventory
     * @return The menu created
     */
    public Menu createMenu(String id, String title, int rows) {
        Menu menu = new Menu(this, id, title, rows);
        menus.put(id, menu);

        // Register the specific-opening permission for the new menu
        getServer().getPluginManager().addPermission(
                new Permission("cyom.menu." + id,
                "Allows the given player to use the /menu open command for the "
                + id + " menu", PermissionDefault.FALSE));
        return menu;
    }

    /**
     * Gets a menu by its id
     *
     * @param id The id of the menu
     * @return The menu, or null if no menu by this id exists
     */
    public Menu getMenu(String id) {
        return menus.get(id);
    }

    /**
     * Deletes a menu from the list of menus. Menu.delete() should be used to
     * properly delete a menu. This method should only be called from inside
     * Menu.delete()
     *
     * @param menu The menu to delete
     */
    protected void deleteMenu(Menu menu) {
        menus.remove(menu.getId());
        // De-register the specific-opening permission for the deleted menu
        getServer().getPluginManager().removePermission("cyom.menus." + menu.getId());
    }

    /**
     * Load or reload all menus from their .yml files
     */
    public void reloadMenus() {
        // De-register the specific-opening permission for all menus
        for (Menu menu : menus.values()) {
            getServer().getPluginManager().removePermission("cyom.menus." + menu.getId());
        }

        // Delete all menus
        menus.clear();

        // Ensure that the plugin folder has been created
        File menusFolder = new File(getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        // Load all .yml files in the plugin folder as menus
        // This may need to change if a config.yml is added
        for (File file : menusFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".yml");
            }
        })) {
            String id = file.getName().substring(0, file.getName().length() - ".yml".length());
            Menu menu = new Menu(this, id);
            menu.load();
            menus.put(id, menu);

            // Register the specific-opening permission for the loaded menu
            getServer().getPluginManager().addPermission(
                    new Permission("cyom.menu." + id,
                    "Allows the given player to use the /menu open command for the "
                    + id + " menu", PermissionDefault.FALSE));
        }
    }

    /**
     * Show a formatted list of all menus to the command sender
     *
     * @param sender The command sender
     */
    public void displayMenuList(CommandSender sender) {
        if (menus.isEmpty()) {
            sender.sendMessage("There are currently no menus. Use /menu create to create one");
            return;
        }
        sender.sendMessage("There are " + menus.size() + " menus:");
        for (Map.Entry<String, Menu> entry : menus.entrySet()) {
            String id = entry.getKey();
            Menu menu = entry.getValue();
            sender.sendMessage("    " + id + ": " + menu.getTitle());
        }
    }

    /**
     * Checks the command blacklist and whitelist to see if this command can be
     * used in a menu script.
     *
     * @param commandName The command name
     * @return True, if the command is on the whitelist or there is no whitelist
     * and it is not on the blacklist. False, if it is on the blacklist or if
     * there is a whitelist and this command is not on it
     */
    public boolean isValidMenuScriptCommand(String commandName) {
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        if (commandWhitelist.isEmpty()) {
            return !commandBlacklist.contains(commandName.toLowerCase());
        } else {
            return commandWhitelist.contains(commandName.toLowerCase());
        }
    }
}
