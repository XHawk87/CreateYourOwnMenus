/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.listeners;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for menu-related events
 *
 * @author XHawk87
 */
public class MenuListener implements Listener {

    private CreateYourOwnMenus plugin;
    /**
     * A dummy menu to be used for in-inventory and in-hand menu item clicks
     */
    private Menu defaultMenu;

    /**
     * Register all events in this listener for the plugin
     *
     * @param plugin The plugin
     */
    public void registerEvents(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
        defaultMenu = new Menu(plugin, "dummy");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Fired when a player clicks inside an inventory.
     *
     * @param event The click event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClickMenu(InventoryClickEvent event) {

        // afaik all HumanEntity are currently Player, but its safer to do a 
        // check for future compatibility
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();

            // Since we set Menu as the holder for any menu inventory, we can 
            // easily check if any inventory relates to a menu and get the 
            // related menu
            Inventory top = event.getView().getTopInventory();
            if (top.getHolder() instanceof Menu) {
                final Menu menu = (Menu) top.getHolder();

                // Check that the player is using and not editing the menu
                if (!menu.isEditing(player)) {

                    // Prevent them from making any changes
                    event.setCancelled(true);

                    // But still activate the menu item if its a left-click
                    if (event.getClick() == ClickType.LEFT) {
                        final ItemStack selected = event.getCurrentItem();
                        if (selected != null) {

                            // To prevent glitches, its safer to wait one tick
                            // before executing any commands that might affect
                            // the menu
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    menu.select(player, selected);
                                }
                            }.runTask(plugin);
                        }
                    }
                }
            } else {
                // Menu items can still be activated if right-clicked in inventory
                if (event.getClick() == ClickType.RIGHT) {
                    final ItemStack selected = event.getCurrentItem();
                    if (selected != null) {
                        // only bother messaging if its an item with lore
                        if (selected.hasItemMeta()) {
                            ItemMeta meta = selected.getItemMeta();
                            if (meta.hasLore()) {
                                // To prevent glitches, its safer to wait one tick
                                // before executing any commands that might affect
                                // the menu
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        defaultMenu.select(player, selected);
                                    }
                                }.runTask(plugin);
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fired when a player left or right clicks the air, or a block, or steps on
     * an interactive block such as a pressure plate or redstone ore.
     *
     * Also note, the event defaults to cancelled when right-clicking the air so
     * it is vital not to ignoreCancelled.
     *
     * @param event The interact event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onRightClickHeldMenuItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasItem()) {
                Player player = event.getPlayer();
                ItemStack item = event.getItem();
                // only bother messaging if its an item with lore
                if (item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasLore()) {
                        defaultMenu.select(player, item);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Fired when the new drag click is used on a menu
     *
     * @param event The drag click event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDragMenu(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Inventory top = event.getView().getTopInventory();
            if (top.getHolder() instanceof Menu) {
                Menu menu = (Menu) top.getHolder();

                // Just to be on the safe side, let's make sure any drags are
                // also cancelled, so non-editing players cannot make changes
                // to the menus
                if (!menu.isEditing(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Fired when a player closes an inventory
     *
     * @param event The inventory closing event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCloseMenu(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Inventory top = event.getView().getTopInventory();
            if (top.getHolder() instanceof Menu) {
                Menu menu = (Menu) top.getHolder();

                // If this is a menu, make sure to mark players as no longer 
                // editing
                menu.doneEditing(player);
            }
        }
    }

    /**
     * Fired when a player quits
     *
     * @param event The player quitting event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory != null) {
            if (inventory.getHolder() instanceof Menu) {
                Menu menu = (Menu) inventory.getHolder();

                // Just to be sure that changes are saved if a player quit's 
                // while they are editing a menu
                menu.doneEditing(player);
            }
        }
    }
}
