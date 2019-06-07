/*
 * Copyright (C) 2013-2019 XHawk87
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.xhawk87.CreateYourOwnMenus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * @author XHawk87
 */
public class DummyMenu extends Menu {

    public DummyMenu(CreateYourOwnMenus plugin) {
        super(plugin, "item-in-hand");
    }

    @Override
    public Inventory getInventory() {
        throw new UnsupportedOperationException("A DummyMenu has no inventory");
    }

    @Override
    public void save() {
    }

    @Override
    public void reload() {
    }

    @Override
    public void load() {
    }

    @Override
    public void delete() {
    }

    @Override
    public void open(final Player player) {
    }

    @Override
    public void edit(Player player) {
    }

    @Override
    public void doneEditing(Player player) {
    }

    @Override
    public boolean isEditing(Player player) {
        return false;
    }

    @Override
    public String getTitle() {
        return "Menu Item Outside of Menu";
    }
}
