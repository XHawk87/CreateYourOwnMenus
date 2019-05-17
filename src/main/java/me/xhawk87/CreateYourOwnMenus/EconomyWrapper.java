/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus;

import net.milkbowl.vault.economy.Economy;

/**
 * @author XHawk87
 */
public class EconomyWrapper {

    private Economy economy;

    public EconomyWrapper(Economy economy) {
        this.economy = economy;
    }

    public double getBalance(String playerName) {
        return economy.getBalance(playerName);
    }

    public String format(double amount) {
        return economy.format(amount);
    }
}
