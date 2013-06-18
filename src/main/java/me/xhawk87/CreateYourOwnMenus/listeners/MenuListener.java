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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for menu-related events
 *
 * @author XHawk87
 */
public class MenuListener implements Listener {

    private CreateYourOwnMenus plugin;

    /**
     * Register all events in this listener for the plugin
     *
     * @param plugin The plugin
     */
    public void registerEvents(CreateYourOwnMenus plugin) {
        this.plugin = plugin;
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
