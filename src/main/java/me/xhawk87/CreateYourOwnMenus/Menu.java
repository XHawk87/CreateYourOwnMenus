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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandSender;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import static org.bukkit.conversations.Prompt.END_OF_CONVERSATION;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
    private long saveCount = 0;

    /**
     * Create a new menu with the given id, title and number of rows.
     *
     * @param plugin A reference to the plugin
     * @param id The id of the menu
     * @param title The display title of the menu inventory
     * @param rows The number of rows for the menu inventory
     */
    public Menu(CreateYourOwnMenus plugin, String id, String title, int rows) {
        this(plugin, id);
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
        this.id = id.toLowerCase();
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        this.file = new File(menusFolder, id + ".yml");
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
        saveCount++;
        final long currentSave = saveCount;
        new BukkitRunnable() {
            @Override
            public void run() {
                // Putting a lock on the file to ensure no other async thread 
                // can work on it at the same time
                synchronized (file) {
                    // If multiple updates are made at the same time, only allow 
                    // the most recent to be saved
                    if (saveCount == currentSave) {
                        // Try-with-resources, automatically cleans up after we're done
                        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                            out.write(toWrite);
                        } catch (IOException ex) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to write to " + file.getPath(), ex);
                        }
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
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                onLoad(data);
                            }
                        }.runTask(plugin);
                    } catch (FileNotFoundException ex) {
                        plugin.getLogger().warning(file.getPath() + " no longer exists");
                    } catch (IOException | InvalidConfigurationException ex) {
                        plugin.getLogger().log(Level.WARNING, "Error reading " + file.getPath(), ex);
                    }
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
     * @param targetPlayer The player being right-clicked with this menu item,
     * if any
     * @param targetBlock The block being right-clicked with this menu item, if
     * any
     */
    public void select(Player player, ItemStack menuItem, Player targetPlayer, Block targetBlock) {
        if (menuItem.hasItemMeta()) {
            ItemMeta meta = menuItem.getItemMeta();

            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (!lore.isEmpty()) {
                    List<String> commands = new ArrayList<>();
                    // Unpack any hidden commands
                    String firstLine = lore.get(0);
                    commands.addAll(MenuScriptUtils.unpackHiddenLines(firstLine));
                    commands.addAll(lore.subList(1, lore.size()));
                    parseCommands(commands.iterator(), player, menuItem, targetPlayer, targetBlock);
                    return;
                }
            }
        }
        // The item doesn't have metadata or lore
        player.sendMessage("This is not a valid menu item");
    }

    private void parseCommands(final Iterator<String> commands, final Player player, final ItemStack menuItem, Player targetPlayer, Block targetBlock) {
        MenuCommandSender consoleSender = new MenuCommandSender(player, plugin.getServer().getConsoleSender());
        while (commands.hasNext()) {
            String command = commands.next();

            // Legacy - Check if the command is hidden
            if (command.startsWith(MenuScriptUtils.hiddenCommand)
                    || command.startsWith(MenuScriptUtils.hiddenPlayerCommand)) {
                command = MenuScriptUtils.unpackHiddenText(command);
            }

            // If a command is prefixed with @p then execute it as the player not the console
            final CommandSender sender;
            if (command.startsWith("@p")) {
                sender = player;
                command = command.substring(2);
            } else {
                sender = consoleSender;
            }

            // Only pay attention to commands
            if (command.startsWith("/")) {
                // Replace @p with the clicking player's name
                command = command.replaceAll("@p", player.getName());
                if (targetPlayer != null) {
                    command = command.replaceAll("@t", targetPlayer.getName());
                } else {
                    if (command.contains("@t")) {
                        return;
                    }
                }
                if (targetBlock != null) {
                    command = command.replaceAll("@x", Integer.toString(targetBlock.getX()));
                    command = command.replaceAll("@y", Integer.toString(targetBlock.getY()));
                    command = command.replaceAll("@z", Integer.toString(targetBlock.getZ()));
                } else {
                    Location loc = player.getLocation();
                    command = command.replaceAll("@x", Integer.toString(loc.getBlockX()));
                    command = command.replaceAll("@y", Integer.toString(loc.getBlockY()));
                    command = command.replaceAll("@z", Integer.toString(loc.getBlockZ()));
                }

                if (command.contains("@a") || command.contains("@w")) {
                    int range = -1;
                    World world = null;
                    Location from = player.getLocation();

                    StringBuilder sb = null;
                    for (int i = 0; i < command.length(); i++) {
                        char c = command.charAt(i);
                        if (c == '@') {
                            sb = new StringBuilder();
                            sb.append(c);
                        } else if (sb != null) {
                            if (c == ' ' || i == command.length() - 1) {
                                String targetString = sb.toString();
                                boolean match = false;
                                if (targetString.equalsIgnoreCase("@a")) {
                                    match = true;
                                } else if (targetString.equalsIgnoreCase("@w")) {
                                    world = player.getWorld();
                                    match = true;
                                } else if (targetString.startsWith("@a:")) {
                                    try {
                                        range = Integer.parseInt(targetString.substring(3));
                                    } catch (NumberFormatException ex) {
                                        player.sendMessage("Error in menu script line (expected @a:range as an integer number): " + command);
                                        return;
                                    }
                                    match = true;
                                } else if (targetString.startsWith("@w:")) {
                                    String worldName = targetString.substring(3);
                                    world = plugin.getServer().getWorld(worldName);
                                    if (world == null) {
                                        player.sendMessage("Error in menu script line (@w:" + worldName + " unknown world): " + command);
                                        return;
                                    }
                                    match = true;
                                }
                                if (match) {
                                    command = command.replaceFirst(targetString, "@t");
                                    i -= targetString.length();
                                    i += 2;
                                }
                                sb = null;
                            } else {
                                sb.append(c);
                            }
                        }
                    }

                    for (Player target : plugin.getServer().getOnlinePlayers()) {
                        if (range != -1) {
                            if (from.distanceSquared(target.getLocation()) > range * range) {
                                continue;
                            }
                        }
                        if (world != null && !target.getWorld().equals(world)) {
                            continue;
                        }
                        String targettedCommand = command.replaceAll("@t", target.getName());
                        if (!parseCommand(sender, player, targettedCommand, commands, menuItem, targetPlayer, targetBlock)) {
                            return;
                        }
                    }
                } else {
                    if (!parseCommand(sender, player, command, commands, menuItem, targetPlayer, targetBlock)) {
                        return;
                    }
                }
            }
        }
    }

    private boolean parseCommand(final CommandSender sender, final Player player, 
            String command, final Iterator<String> commands, final ItemStack menuItem, 
            final Player targetPlayer, final Block targetBlock) {
        // Handle the special menu script commands
        String[] args = command.split(" ");
        String specialCommand = args[0];
        if (!plugin.isValidMenuScriptCommand(specialCommand)
                && !player.hasPermission("cyom.script.command." + specialCommand.toLowerCase())) {
            player.sendMessage("Error in menu script line (command is not allowed): " + command);
            return false;
        }
        if (specialCommand.equalsIgnoreCase("/requirepermission")) {
            if (args.length != 2) {
                player.sendMessage("Error in menu script line (expected permission node): " + command);
                return false;
            }
            String permission = args[1];
            if (!player.hasPermission(permission)) {
                player.sendMessage("You do not have permission to use this menu item");
                return false;
            }
        } else if (specialCommand.equalsIgnoreCase("/close")) {
            if (args.length != 1) {
                player.sendMessage("Error in menu script line (expected no arguments): " + command);
                return false;
            }
            player.closeInventory();
        } else if (specialCommand.equalsIgnoreCase("/consume")) {
            if (args.length != 1) {
                player.sendMessage("Error in menu script line (expected no arguments): " + command);
                return false;
            }
            int amount = menuItem.getAmount() - 1;
            if (amount > 0) {
                menuItem.setAmount(amount);
            } else {
                PlayerInventory inv = player.getInventory();
                ItemStack held = inv.getItemInHand();
                if (held.equals(menuItem)) {
                    inv.clear(inv.getHeldItemSlot());
                } else {
                    player.sendMessage("Cannot locate menu item to remove it. Was it moved?");
                    return false;
                }
            }
            player.updateInventory();
        } else if (specialCommand.equalsIgnoreCase("/delay")) {
            if (args.length != 2) {
                player.sendMessage("Error in menu script line (expected delay in ticks): " + command);
                return false;
            }
            try {
                int delay = Integer.parseInt(args[1]);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        parseCommands(commands, player, menuItem, targetPlayer, targetBlock);
                    }
                }.runTaskLater(plugin, delay);
                return false;
            } catch (NumberFormatException ex) {
                player.sendMessage("Error in menu script line (delay must be a whole number of ticks): " + command);
                return false;
            }
        } else if (specialCommand.equalsIgnoreCase("/requirecurrency")) {
            EconomyWrapper economy = plugin.getEconomy();
            if (economy == null) {
                player.sendMessage("The /requirecurrency special command requires Vault to work");
                return false;
            }
            if (args.length != 2) {
                player.sendMessage("Error in menu script line (expected currency amount): " + command);
                return false;
            }
            String amountString = args[1];
            try {
                double amount = Double.parseDouble(amountString);
                if (economy.getBalance(player.getName()) < amount) {
                    player.sendMessage("You must have at least " + economy.format(amount) + ChatColor.RESET + " to do this");
                    return false;
                }
            } catch (NumberFormatException ex) {
                player.sendMessage("Error in menu script line (expected currency amount): " + command);
                return false;
            }
        } else {
            // Otherwise, parse it as a normal command. 

            if (command.contains("{")) {
                // Parse for {dynamic arguments}
                StringBuilder commandString = new StringBuilder();
                StringBuilder promptString = null;
                final List<String> parts = new ArrayList<>();
                // The dispatchCommand method expects there to be no forward slash
                for (int i = 1; i < command.length(); i++) {
                    char c = command.charAt(i);
                    if (c == '{' && promptString == null) {
                        parts.add(commandString.toString());
                        promptString = new StringBuilder();
                    } else if (c == '}' && promptString != null) {
                        parts.add(promptString.toString());
                        promptString = null;
                        commandString = new StringBuilder();
                    } else if (promptString == null) {
                        commandString.append(c);
                    } else {
                        promptString.append(c);
                    }
                }
                final StringBuilder parsedCommand = new StringBuilder();
                player.beginConversation(
                        new Conversation(plugin, player,
                        parseDynamicArgs(parsedCommand,
                        parts.iterator(), player, new MessagePrompt() {
                    @Override
                    protected Prompt getNextPrompt(ConversationContext context) {
                        String command = parsedCommand.toString();
                        if (!plugin.getServer().dispatchCommand(sender,
                                command)) {
                            player.sendMessage("Error in menu script line (unknown command): " + command);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    parseCommands(commands, player, menuItem, targetPlayer, targetBlock);
                                }
                            }.runTask(plugin);
                        }
                        return END_OF_CONVERSATION;
                    }

                    @Override
                    public String getPromptText(ConversationContext context) {
                        return parsedCommand.toString();
                    }
                })));
                return false;
            } else {
                if (!plugin.getServer().dispatchCommand(sender,
                        command.substring(1))) {
                    player.sendMessage("Error in menu script line (unknown command): " + command);
                    return false;
                }
            }
        }
        return true;
    }

    private Prompt parseDynamicArgs(final StringBuilder parsedCommand,
            final Iterator<String> parts, final Player player, final Prompt message) {
        if (!parts.hasNext()) {
            return message;
        }
        String commandPart = parts.next();
        parsedCommand.append(commandPart);

        if (!parts.hasNext()) {
            return message;
        }
        final String promptPart = parts.next();

        return new StringPrompt() {
            @Override
            public String getPromptText(ConversationContext context) {
                return "Please enter " + promptPart + ":";
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                parsedCommand.append(input);
                return parseDynamicArgs(parsedCommand, parts, player, message);
            }
        };
    }

    /**
     * @return The title of this menu
     * @throws NullPointerException if the menu has not yet been loaded
     */
    public String getTitle() {
        return inventory.getTitle();
    }
}
