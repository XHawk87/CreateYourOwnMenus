/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.listeners;

import java.util.Random;
import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import me.xhawk87.CreateYourOwnMenus.Menu;
import me.xhawk87.CreateYourOwnMenus.utils.MenuScriptUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for menu-related events
 *
 * @author XHawk87
 */
public class MenuListener implements Listener {

    private static final Random random = new Random();
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
     * Fired when a player drops an item using the drop item key, or by throwing
     * items out of their inventory.
     *
     * We only want to cancel it if they are using the drop item key, as they
     * are already prevented from picking up locked items to throw out
     *
     * @param event The drop item event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDropFromLockedSlot(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getItemOnCursor() == null
                || player.getItemOnCursor().getType() == Material.AIR) {
            PlayerInventory inv = player.getInventory();
            int slot = inv.getHeldItemSlot();
            if (player.hasPermission("cyom.slot.lock." + slot)) {
                // Replace the item back where it was
                ItemStack item = event.getItemDrop().getItemStack();
                ItemStack slotItem = inv.getItem(slot);
                if (slotItem == null || slotItem.getTypeId() == 0) {
                    inv.setItem(slot, item.clone());
                } else {
                    slotItem.setAmount(slotItem.getAmount() + item.getAmount());
                }
                
                // Stop it from being dropped
                event.getItemDrop().remove();
                
                // Make sure player sees everything as normal
                player.updateInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickUpItemIntoLockedSlot(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();
        ItemStack pickup = event.getItem().getItemStack();
        int toAdd = pickup.getAmount() - event.getRemaining();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                if (player.hasPermission("cyom.slot.lock." + i)) {
                    event.setCancelled(true);
                    overrideItemPickup(player, event.getItem());
                }
                break;
            } else if (slot.isSimilar(pickup) && slot.getAmount() < slot.getMaxStackSize()) {
                toAdd -= slot.getMaxStackSize() - slot.getAmount();
                if (toAdd <= 0) {
                    break;
                }
            }
        }
    }

    private void overrideItemPickup(final Player player, final Item item) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerInventory inv = player.getInventory();
                ItemStack pickup = item.getItemStack();
                boolean dosound = false;
                for (int i = 0; i < inv.getSize(); i++) {
                    if (player.hasPermission("cyom.slot.lock." + i)) {
                        continue;
                    }
                    ItemStack slot = inv.getItem(i);
                    if (slot == null || slot.getType() == Material.AIR) {
                        inv.setItem(i, pickup.clone());
                        item.remove();
                        dosound = true;
                        break;
                    } else if (slot.isSimilar(pickup)) {
                        int maxStackSize = slot.getMaxStackSize();
                        if (maxStackSize == -1) {
                            maxStackSize = 64;
                        }
                        int spaceInSlot = maxStackSize - slot.getAmount();
                        if (spaceInSlot > 0) {
                            dosound = true;
                            if (pickup.getAmount() > spaceInSlot) {
                                pickup.setAmount(pickup.getAmount() - spaceInSlot);
                                slot.setAmount(maxStackSize);
                                continue;
                            } else {
                                item.remove();
                                slot.setAmount(slot.getAmount() + pickup.getAmount());
                                break;
                            }
                        }
                    }
                }
                if (dosound) {
                    player.playSound(item.getLocation(), Sound.ITEM_PICKUP, 0.2f, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            }
        }.runTask(plugin);
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
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("i.have.every.permission")) {
            player.sendMessage("CreateYourOwnMenus has detected that you may have been granted every permission node. This is generally a bad idea because it means that you will also be granted permissions that you don't necessarily want, such as the 'cyom.slot.lock.*' permissions, which lock your inventory slots causing them to act like menus. If you wish to remove this warning, please remove the 'i.have.every.permission' node from yourself, or stop using the '*' node. It really isn't needed, Ops automatically get all permissions that default to Op anyway.");
        }
    }
}
