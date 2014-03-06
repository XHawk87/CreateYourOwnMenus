/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import me.xhawk87.CreateYourOwnMenus.script.ScriptCommand;
import me.xhawk87.CreateYourOwnMenus.utils.ElevatedCommandSender;
import me.xhawk87.CreateYourOwnMenus.utils.FileUpdater;
import me.xhawk87.CreateYourOwnMenus.utils.MenuCommandSender;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
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
    private FileUpdater fileUpdater;

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
        this.fileUpdater = new FileUpdater(file);
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
        fileUpdater.save(plugin, data.saveToString());
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
                    readMenuFile("UTF8");
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Called asynchronously to read the file data
     *
     * @param encoding The text-encoding to use for reading the file
     */
    private void readMenuFile(String encoding) {
        final FileConfiguration data = new YamlConfiguration();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(encoding)))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

            data.loadFromString(sb.toString());

            new BukkitRunnable() {
                @Override
                public void run() {
                    onLoad(data);
                }
            }.runTask(plugin);
        } catch (FileNotFoundException ex) {
            plugin.getLogger().warning(file.getPath() + " no longer exists");
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Error reading " + file.getPath(), ex);
        } catch (InvalidConfigurationException ex) {
            if (encoding.equals("UTF8")) {
                plugin.getLogger().warning("Outdated menu file detected, trying to read in ANSI: " + file.getName());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        synchronized (file) {
                            readMenuFile("Cp1252");
                        }
                    }
                }.runTaskAsynchronously(plugin);
            } else {
                plugin.getLogger().log(Level.WARNING, "Corrupted menu file detected: " + file.getPath(), ex);
            }
        }
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
        select(player, menuItem, null, null);
    }

    /**
     * A player selects a menu item for this menu
     *
     * @param player The player selecting the item
     * @param menuItem The item being selected
     * @param targetPlayer The player being right-clicked with this menu item
     */
    public void select(Player player, ItemStack menuItem, Player targetPlayer) {
        select(player, menuItem, targetPlayer, null);
    }

    /**
     * A player selects a menu item for this menu
     *
     * @param player The player selecting the item
     * @param menuItem The item being selected
     * @param targetBlock The block being right-clicked with this menu item
     */
    public void select(Player player, ItemStack menuItem, Block targetBlock) {
        select(player, menuItem, null, targetBlock);
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
    private void select(Player player, ItemStack menuItem, Player targetPlayer, Block targetBlock) {
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
        player.sendMessage(plugin.translate(player, "invalid-menu-item", "This is not a valid menu item"));
    }

    public void parseCommands(final Iterator<String> commands, final Player player, final ItemStack menuItem, Player targetPlayer, Block targetBlock) {
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
                if (command.charAt(2) == '+') {
                    sender = new ElevatedCommandSender(player, consoleSender);
                    command = command.substring(3);
                } else {
                    sender = player;
                    command = command.substring(2);
                }
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
                            if (i == command.length() - 1 && c != ' ') {
                                sb.append(c);
                                c = ' ';
                            }
                            if (c == ' ') {
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
                                        player.sendMessage(plugin.translate(player, "error-integer-area", "Error in menu script line (expected @a:range as an integer number): {0}", command));
                                        return;
                                    }
                                    match = true;
                                } else if (targetString.startsWith("@w:")) {
                                    String worldName = targetString.substring(3);
                                    world = plugin.getServer().getWorld(worldName);
                                    if (world == null) {
                                        player.sendMessage(plugin.translate(player, "error-unknown-world", "Error in menu script line (@w:{0} unknown world): {1}", worldName, command));
                                        return;
                                    }
                                    match = true;
                                }
                                if (match) {
                                    command = command.replaceFirst("\\Q" + targetString + "\\E", "@o");
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
                        String targettedCommand = command.replaceAll("@o", target.getName());
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
            player.sendMessage(plugin.translate(player, "error-illegal-command", "Error in menu script line (command is not allowed): {0}", command));
            return false;
        }
        ScriptCommand scriptCommand = plugin.getScriptCommand(specialCommand);
        if (scriptCommand != null) {
            return scriptCommand.execute(this, player, Arrays.copyOfRange(args, 1, args.length), command, menuItem, commands, targetPlayer, targetBlock);
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
                        commandString = null;
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
                if (promptString != null) {
                    player.sendMessage(plugin.translate(player, "error-incomplete-dynamic-arg", "Error in menu script line (incomplete dynamic argument): {0}", command));
                    return false;
                }
                if (commandString != null) {
                    parts.add(commandString.toString());
                }
                final StringBuilder parsedCommand = new StringBuilder();
                player.beginConversation(
                        new Conversation(plugin, player,
                        parseDynamicArgs(parsedCommand,
                        parts.iterator(), player, new MessagePrompt() {
                    @Override
                    protected Prompt getNextPrompt(ConversationContext context) {
                        final String command = parsedCommand.toString();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                // Execute the command
                                if (!plugin.getServer().dispatchCommand(sender,
                                        command)) {
                                    // If it fails to execute
                                    player.sendMessage(plugin.translate(player, "error-unknown-command", "Error in menu script line (unknown command): {0}", command));
                                } else {
                                    // If it succeeds, continue with the script execution
                                    parseCommands(commands, player, menuItem, targetPlayer, targetBlock);
                                }
                            }
                        }.runTask(plugin);
                        return END_OF_CONVERSATION;
                    }

                    @Override
                    public String getPromptText(ConversationContext context) {
                        return "";
                    }
                })));
                return false;
            } else {
                if (!plugin.getServer().dispatchCommand(sender,
                        command.substring(1))) {
                    player.sendMessage(plugin.translate(player, "error-unknown-command", "Error in menu script line (unknown command): {0}", command));
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
                return plugin.translate(player, "dynamic-arg-prompt", "Please enter {0}:", promptPart);
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

    public String translate(CommandSender forWhom, String key, String template, Object... params) {
        return plugin.translate(forWhom, key, template, params);
    }
}
