package me.xhawk87.CreateYourOwnMenus.PlaceholderApi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.xhawk87.CreateYourOwnMenus.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaceholderManager {

    public PlaceholderManager(final Menu menu){
        this.menu = menu;
    }

    private final Menu menu;
    private List<Placeholder> placeholderList = new ArrayList<>();

    /**
     * This function is needed for saving the non replaced placeholders (eg. %asdf%) into the menu files.
     * Therefore we need to recover the replaced inventory items. And this is working by just copying the saved
     * placeholder data into the inventory variable again
     */
    public void undoPlaceholders() {
        if (placeholderList.isEmpty()) return;

        for (Placeholder p : placeholderList) {

            //check if item still exists, needed when the edit command was used
            if (menu.getInventory().getItem(p.getSlot()) == null) continue;

            //create a copy of our item, so we don't override something important
            ItemStack item = p.getItem();
            ItemMeta meta = item.getItemMeta();

            //restore the original Title
            if (p.getTitle() != null) {
                meta.setDisplayName(p.getTitle());
            }

            //get the whole Lorelist
            List<String> metaLore = meta.getLore();
            if (!p.getLorePlaceholders().isEmpty()) {
                for (Map.Entry<Integer, String> entry : p.getLorePlaceholders().entrySet()) {
                    Integer slot = entry.getKey();
                    String lore = entry.getValue();
                    metaLore.set(slot, lore);
                }
            }
            //setting meta, and lore
            meta.setLore(metaLore);
            item.setItemMeta(meta);
            menu.getInventory().setItem(p.getSlot(), item);
        }
    }

    /**
     * Checks if there is a placeholder in either the Title of the Item, or in one of the lores,
     * if there is a placeholder the item gets added to placeholderList, there also the slot position is saved,
     * this list is used afterwards when a player opens the menu
     */
    public void addPlaceholdersToList(int slot, ItemStack item) {
        //check if placeholders are enabled!
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            return;

        //create a new temporary Placeholder item
        Placeholder placeholder = new Placeholder(slot, item);
        boolean retVal = false;

        //check and add if title has an placeholder in it
        if (PlaceholderAPI.containsPlaceholders(item.getItemMeta().getDisplayName())) {
            placeholder.setTitle(item.getItemMeta().getDisplayName());
            retVal = true;
        }

        //check if placeholders can be found in a lore
        int index = 0;
        if (item.getItemMeta().getLore() != null)
            for (String s : item.getItemMeta().getLore()) {
                if (PlaceholderAPI.containsPlaceholders(s)) {
                    //save all placeholders as string, and also save the inventory slot
                    placeholder.getLorePlaceholders().put(index, s);
                    retVal = true;
                }
                index++;
            }

        //if we have found a placeholder lets add it
        if (retVal) {
            placeholderList.add(placeholder);
        }
    }

    /**
     * This function is called when a menu is used, therefore it changes every time the menu is opened
     */
    public void replacePlaceholders(final Player player) {
        if (placeholderList.isEmpty()) return;

        //when Player is editing, display the normal lore and title, therefore %asdf% is displayed
        if (menu.isEditing(player)) {
            undoPlaceholders();
            return;
        }

        for (Placeholder p : placeholderList) {
            //create a copy of our item, so we don't override something important
            ItemStack item = p.getItem();
            ItemMeta meta = item.getItemMeta();

            if (p.getItem() != null) {
                String title = PlaceholderAPI.setPlaceholders(player, p.getTitle());
                meta.setDisplayName(title);
            }

            List<String> metaLore = meta.getLore();
            if (!p.getLorePlaceholders().isEmpty()) {
                for (Map.Entry<Integer, String> entry : p.getLorePlaceholders().entrySet()) {
                    Integer slot = entry.getKey();
                    String lore = entry.getValue();
                    metaLore.set(slot, PlaceholderAPI.setPlaceholders(player, lore));
                }
            }
            meta.setLore(metaLore);
            item.setItemMeta(meta);
            menu.getInventory().setItem(p.getSlot(), item);
        }
    }

    /**
     * Method for regenerating the entries of the placeholderList
     * let's the garbage collector do the thing ;)
     */
    public void generateNewPlaceholderList(){
        placeholderList = new ArrayList<>();
    }

}
