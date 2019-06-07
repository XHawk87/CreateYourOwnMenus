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
package me.xhawk87.CreateYourOwnMenus.commands;

import org.bukkit.command.CommandExecutor;

/**
 * Provides permissions and usage message support for menu subcommands
 *
 * @author XHawk87
 */
public interface IMenuCommand extends CommandExecutor {

    /**
     * @return The usage message for this subcommand
     */
    String getUsage();

    /**
     * @return The permission-node required to use this subcommand, or null if
     * none is required
     */
    String getPermission();
}
