/*
 * ItemJoin
 * Copyright (C) CraftationGaming <https://www.craftationgaming.com/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.RockinChaos.itemjoin.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class VaultAPI {
    private Economy econ = null;
    private boolean isEnabled = false;
    
    private static VaultAPI vault;
    
   /**
	* Creates a new VaultAPI instance.
	* 
	*/
    public VaultAPI() {
    	this.setVaultStatus(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null);
    }
    
   /**
	* Attempts to enable the economy.
	* 
	*/
	private void enableEconomy() { 
		if (ItemJoin.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
			if (!this.setupEconomy()) {
				ServerHandler.getServer().logDebug("{VaultAPI} An error has occured while setting up enabling Vault-ItemJoin support!");
			}
		}
	}

   /**
	* Sets the Economy instance.
	* 
	* @return If the Economy instance was successfully enabled.
	*/
    private boolean setupEconomy() {
        if (ItemJoin.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) { return false; }
        RegisteredServiceProvider<Economy> rsp = ItemJoin.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) { return false; }
        this.econ = rsp.getProvider();
        return this.econ != null;
    }
    
   /**
	* Gets the Vault Economy instance.
	* 
	* @return Gets the current economy instance.
	*/
    public Economy getEconomy() {
        return this.econ;
    }
    
   /**
	* Checks if Vault is enabled.
	* 
	* @return If Vault is enabled.
	*/
    public boolean vaultEnabled() {
    	return this.isEnabled;
    }
    
   /**
	* Sets the status of the VaultAPI.
	* 
	* @param bool - If Vault is enabled.
	*/
    private void setVaultStatus(final boolean bool) {
    	if (bool) { this.enableEconomy(); }
    	this.isEnabled = bool;
    }
    
   /**
	* Gets the balance of the Player.
	* 
	* @param player - The Player having their balance found.
	* @return The balance of the Player.
	*/
	public double getBalance(final Player player) {
		return this.econ.getBalance(player);
	}
	
   /**
	* Withdrawls the specified cost from the Players balance.
	* 
	* @param player - The Player being transacted.
	* @param cost - The cost to be charged to the Player.
	* @return If the transaction was successful.
	*/
	public EconomyResponse withdrawBalance(final Player player, final int cost) {
		return this.econ.withdrawPlayer(player, cost);
	}
	
   /**
    * Gets the instance of the VaultAPI.
    * 
    * @param regen - If the VaultAPI should have a new instance created.
    * @return The VaultAPI instance.
    */
    public static VaultAPI getVault(final boolean regen) { 
        if (vault == null || regen) { vault = new VaultAPI(); }
        return vault; 
    } 
}