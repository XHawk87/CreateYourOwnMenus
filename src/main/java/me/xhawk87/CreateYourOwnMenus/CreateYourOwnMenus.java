/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import me.xhawk87.CreateYourOwnMenus.commands.MenuCommand;
import me.xhawk87.CreateYourOwnMenus.listeners.MenuListener;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A plugin to allow server owners to design and create their own menus in-game
 *
 * @author XHawk87
 */
public class CreateYourOwnMenus extends JavaPlugin {

    // Store menus by their ID
    private Map<String, Menu> menus = new HashMap<>();

    @Override
    public void onEnable() {
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
    }

    /**
     * Load or reload all menus from their .yml files
     */
    public void reloadMenus() {
        menus.clear();
        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs(); // Ensure that the plugin folder has been created
        }
        // Load all .yml files in the plugin folder as menus
        // This may need to change if a config.yml is added
        for (File file : pluginFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".yml");
            }
        })) {
            String id = file.getName().substring(0, file.getName().length() - ".yml".length());
            Menu menu = new Menu(this, id);
            menu.load();
            menus.put(id, menu);
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
}
