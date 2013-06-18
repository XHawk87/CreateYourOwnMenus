/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    public String getUsage();

    /**
     * @return The permission-node required to use this subcommand, or null if
     * none is required
     */
    public String getPermission();
}
