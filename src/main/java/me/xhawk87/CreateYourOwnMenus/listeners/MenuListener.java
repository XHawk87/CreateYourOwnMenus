/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.listeners;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
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

            // First check if the clicked slot is a locked inventory slot
            int rawSlot = event.getRawSlot();
            int numInTop = event.getView().getTopInventory().getSize();
            if (rawSlot >= numInTop) {
                // The clicked slot is the player's inventory
                int slot = event.getSlot();
                if (player.hasPermission("cyom.slot.lock." + slot)) {

                    // Prevent any modification to the locked slot
                    event.setCancelled(true);

                    // Activate it if its a left click
                    if (event.getClick() == ClickType.LEFT) {
                        final ItemStack selected = event.getCurrentItem();
                        if (selected != null) {

                            // To prevent glitches, its safer to wait one tick
                            // before executing any commands that might affect
                            // the player's inventory
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    defaultMenu.select(player, selected);
                                }
                            }.runTask(plugin);
                        }
                    }
                    return;
                }
            }

            // Since we set Menu as the holder for any menu inventory, we can 
            // easily check if any inventory relates to a menu and get the 
            // related menu
            Inventory top = event.getView().getTopInventory();
            if (top.getHolder() instanceof Menu) {
                final Menu menu = (Menu) top.getHolder();

                // Check that the player is using and not editing the menu
                if (!menu.isEditing(player)) {

                    // Prevent creative players from duping menu items
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        final InventoryView view = event.getView();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                view.setCursor(null);
                            }
                        }.runTask(plugin);
                    }

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
            int numInTop = top.getSize();
            
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot >= numInTop) {
                    int slot = event.getView().convertSlot(rawSlot);
                    if (player.hasPermission("cyom.slot.lock." + slot)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            
            
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
                final Player player = event.getPlayer();
                final ItemStack item = event.getItem();
                final Block clicked = event.getClickedBlock();
                // only bother messaging if its an item with lore
                if (MenuScriptUtils.isValidMenuItem(item)) {
                    // Schedule it for the next tick to avoid conflicts with the event action
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            defaultMenu.select(player, item, clicked);
                        }
                    }.runTask(plugin);
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Fired when a player right-clicks on an entity or player
     *
     * @param event The interact event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClickPlayerWithMenuItem(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            final Player player = event.getPlayer();
            final Player target = (Player) event.getRightClicked();
            final ItemStack item = player.getItemInHand();
            if (MenuScriptUtils.isValidMenuItem(item)) {
                // Schedule it for the next tick to avoid conflicts with the event action
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        defaultMenu.select(player, item, target);
                    }
                }.runTask(plugin);
                event.setCancelled(true);
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

                // Just to be sure that changes are saved if a player quits 
                // while they are editing a menu
                menu.doneEditing(player);
            }
        }
    }
}
