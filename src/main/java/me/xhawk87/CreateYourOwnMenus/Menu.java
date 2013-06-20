/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles menu file IO, editing and use of the menu
 *
 * @author XHawk87
 */
public class Menu implements InventoryHolder {

    private CreateYourOwnMenus plugin;
    private String id;
    private Inventory inventory;
    private Set<String> editing = new HashSet<>();
    private File file;

    /**
     * Create a new menu with the given id, title and number of rows.
     *
     * @param plugin A reference to the plugin
     * @param id The id of the menu
     * @param title The display title of the menu inventory
     * @param rows The number of rows for the menu inventory
     */
    public Menu(CreateYourOwnMenus plugin, String id, String title, int rows) {
        this.plugin = plugin;
        this.id = id;
        this.file = new File(plugin.getDataFolder(), id + ".yml");
        this.inventory = plugin.getServer().createInventory(this, rows * 9, title);
    }

    /**
     * Creates a menu for loading. This only needs the id, the rest is pulled
     * from the .yml file.
     *
     * @param plugin A reference to the plugin
     * @param id The id of the menu
     */
    public Menu(CreateYourOwnMenus plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        this.file = new File(plugin.getDataFolder(), id + ".yml");
    }

    /**
     * @return the ID of this menu
     */
    public String getId() {
        return id;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Schedules an asynchronous save of this menu to its .yml file
     */
    public void save() {
        FileConfiguration data = new YamlConfiguration();
        data.set("title", inventory.getTitle());
        data.set("size", inventory.getSize());
        ConfigurationSection contentsData = data.createSection("contents");
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                contentsData.set(Integer.toString(i), item);
            }
        }
        final String toWrite = data.saveToString();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Putting a lock on the file to ensure no other async thread 
                // can work on it at the same time
                synchronized (file) {
                    // Try-with-resources, automatically cleans up after we're done
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                        out.write(toWrite);
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to write to " + file.getPath(), ex);
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Schedule an asynchronous load of this menu from its .yml file
     */
    public void load() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Only one thread must operate on the file at any one time to 
                // prevent conflicts
                synchronized (file) {
                    final FileConfiguration data = new YamlConfiguration();
                    try {
                        data.load(file);
                    } catch (FileNotFoundException ex) {
                        plugin.getLogger().warning(file.getPath() + " no longer exists");
                    } catch (IOException | InvalidConfigurationException ex) {
                        plugin.getLogger().log(Level.WARNING, "Error reading " + file.getPath(), ex);
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onLoad(data);
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Called internally when the menu has been loaded from file
     *
     * @param data The menu data
     */
    private void onLoad(FileConfiguration data) {
        String title = data.getString("title");
        int size = data.getInt("size");
        inventory = plugin.getServer().createInventory(this, size, title);
        ConfigurationSection contentsData = data.getConfigurationSection("contents");
        for (String key : contentsData.getKeys(false)) {
            int slot = Integer.parseInt(key);
            ItemStack item = contentsData.getItemStack(key);
            inventory.setItem(slot, item);
        }
    }

    /**
     * Deletes the menu file, and removes all references to the menu
     */
    public void delete() {
        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (file) {
                    file.delete();
                }
            }
        }.runTaskAsynchronously(plugin);
        plugin.deleteMenu(this);
    }

    /**
     * Opens this menu for using by the given player. If they click a menu item
     * it will be activated. It should be impossible for them to modify the menu
     * in any way
     *
     * @param player The player to open this menu for
     */
    public void open(final Player player) {
        // Check if the player already has an inventory open
        Inventory current = player.getOpenInventory().getTopInventory();
        if (current == null) {
            player.openInventory(inventory);
        } else {
            // Switching directly from one inventory to another causes glitches
            player.closeInventory();
            // So close it and wait one tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.openInventory(inventory);
                }
            }.runTask(plugin);
        }
    }

    /**
     * Opens this menu for editing by the given player. Multiple players can
     * modify a menu at the same time. Instead of activating on clicking, these
     * players will be able to modify the menu.
     *
     * @param player The player editing this menu
     */
    public void edit(Player player) {
        editing.add(player.getName());
        open(player);
    }

    /**
     * When a player is done editing, they are removed from the editing list so
     * they can use the menu again and any changes they made are saved.
     *
     * @param player The player done editing
     */
    public void doneEditing(Player player) {
        if (editing.remove(player.getName())) {
            // If the player was editing, make sure any changes they made were 
            // saved
            save();
        }
    }

    /**
     * Checks if a player is currently editing the menu.
     *
     * @param player The player to check
     * @return True if they are editing the menu, false if they are using it
     */
    public boolean isEditing(Player player) {
        return editing.contains(player.getName());
    }

    /**
     * A player selects a menu item for this menu
     *
     * @param player The player selecting the item
     * @param menuItem The item being selected
     */
    public void select(Player player, ItemStack menuItem) {
        if (menuItem.hasItemMeta()) {
            ItemMeta meta = menuItem.getItemMeta();
            if (meta.hasLore()) {
                List<String> commands = meta.getLore();
                parseCommands(commands.iterator(), player);
                return;
            }
        }
        // The item doesn't have metadata or lore
        player.sendMessage("This is not a valid menu item");
    }

    private void parseCommands(final Iterator<String> commands, final Player player) {
        ConsoleCommandSender consoleSender = plugin.getServer().getConsoleSender();
        while (commands.hasNext()) {
            String command = commands.next();

            // If a command is prefixed with @p then execute it as the player not the console
            CommandSender sender = consoleSender;
            if (command.startsWith("@p")) {
                sender = player;
                command = command.substring(2);
            }

            // Check if the command is hidden
            if (command.startsWith(ChatColor.COLOR_CHAR + "/")) {
                command = ChatColor.stripColor(command);
            }

            // Only pay attention to commands
            if (command.startsWith("/")) {
                // Replace @p with the clicking player's name
                command = command.replaceAll("@p", player.getName());

                // Handle the special menu script commands
                String[] args = command.split(" ");
                String specialCommand = args[0];
                if (specialCommand.equalsIgnoreCase("/requirepermission")) {
                    if (args.length != 2) {
                        player.sendMessage("Error in menu script line: " + command);
                        return;
                    }
                    String permission = args[1];
                    if (!player.hasPermission(permission)) {
                        player.sendMessage("You do not have permission to use this menu item");
                        return;
                    }
                } else if (specialCommand.equalsIgnoreCase("/close")) {
                    if (args.length != 1) {
                        player.sendMessage("Error in menu script line: " + command);
                        return;
                    }
                    player.closeInventory();
                } else if (specialCommand.equalsIgnoreCase("/delay")) {
                    if (args.length != 2) {
                        player.sendMessage("Error in menu script line: " + command);
                        return;
                    }
                    try {
                        int delay = Integer.parseInt(args[1]);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                parseCommands(commands, player);
                            }
                        }.runTaskLater(plugin, delay);
                        return;
                    } catch (NumberFormatException ex) {
                        player.sendMessage("Error in menu script line: " + command);
                        return;
                    }
                } else {
                    // Otherwise, parse it as a normal command. 
                    // The dispatchCommand method expects there to be no forward slash
                    if (!plugin.getServer().dispatchCommand(sender, command.substring(1))) {
                        player.sendMessage("Error in menu script line: " + command);
                        return;
                    }
                }
            }
        }
    }

    /**
     * @return The title of this menu
     * @throws NullPointerException if the menu has not yet been loaded
     */
    public String getTitle() {
        return inventory.getTitle();
    }
}
