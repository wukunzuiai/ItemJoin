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
package me.RockinChaos.itemjoin.handlers;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.HashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import me.RockinChaos.itemjoin.ChatExecutor;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.ChatTab;
import me.RockinChaos.itemjoin.item.ItemDesigner;
import me.RockinChaos.itemjoin.item.ItemMap;
import me.RockinChaos.itemjoin.listeners.Breaking;
import me.RockinChaos.itemjoin.listeners.Consumes;
import me.RockinChaos.itemjoin.listeners.Drops;
import me.RockinChaos.itemjoin.listeners.Interact;
import me.RockinChaos.itemjoin.listeners.Commands;
import me.RockinChaos.itemjoin.listeners.Clicking;
import me.RockinChaos.itemjoin.listeners.Crafting;
import me.RockinChaos.itemjoin.listeners.Menu;
import me.RockinChaos.itemjoin.listeners.Entities;
import me.RockinChaos.itemjoin.listeners.Pickups;
import me.RockinChaos.itemjoin.listeners.Placement;
import me.RockinChaos.itemjoin.listeners.Recipes;
import me.RockinChaos.itemjoin.listeners.Stackable;
import me.RockinChaos.itemjoin.listeners.Storable;
import me.RockinChaos.itemjoin.listeners.Offhand;
import me.RockinChaos.itemjoin.listeners.triggers.LimitSwitch;
import me.RockinChaos.itemjoin.listeners.triggers.PlayerGuard;
import me.RockinChaos.itemjoin.listeners.triggers.PlayerJoin;
import me.RockinChaos.itemjoin.listeners.triggers.PlayerLogin;
import me.RockinChaos.itemjoin.listeners.triggers.PlayerQuit;
import me.RockinChaos.itemjoin.listeners.triggers.Respawn;
import me.RockinChaos.itemjoin.listeners.triggers.WorldSwitch;
import me.RockinChaos.itemjoin.utils.LogFilter;
import me.RockinChaos.itemjoin.utils.DependAPI;
import me.RockinChaos.itemjoin.utils.LanguageAPI;
import me.RockinChaos.itemjoin.utils.LegacyAPI;
import me.RockinChaos.itemjoin.utils.Metrics;
import me.RockinChaos.itemjoin.utils.Reflection;
import me.RockinChaos.itemjoin.utils.Utils;
import me.RockinChaos.itemjoin.utils.enchants.Glow;
import me.RockinChaos.itemjoin.utils.FileData;
import me.RockinChaos.itemjoin.utils.protocol.ProtocolManager;
import me.RockinChaos.itemjoin.utils.sqlite.SQLite;

public class ConfigHandler {
	
	private HashMap < String, Boolean > noSource = new HashMap < String, Boolean > ();
	
	private boolean Generating = false;
	
	private YamlConfiguration itemsFile;
	private YamlConfiguration configFile;
	private YamlConfiguration langFile;
	
	private static ConfigHandler config;
	
   /**
    * Registers the command executors and events.
    * 
    */
	public void registerEvents() {
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerLogin(), ItemJoin.getInstance());
	    ItemJoin.getInstance().getCommand("itemjoin").setExecutor(new ChatExecutor());
	    ItemJoin.getInstance().getCommand("itemjoin").setTabCompleter(new ChatTab());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Menu(), ItemJoin.getInstance());
		if (((this.clearEnabled("Join") || this.triggerEnabled("Join") || this.triggerEnabled("First-Join")) || Utils.getUtils().containsIgnoreCase(this.getHotbarTriggers(), "JOIN")) && !Utils.getUtils().isRegistered(PlayerJoin.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerJoin(), ItemJoin.getInstance());
		}
		if (((this.clearEnabled("World-Switch") || this.triggerEnabled("World-Switch")) || Utils.getUtils().containsIgnoreCase(this.getHotbarTriggers(), "WORLD-SWITCH")) && !Utils.getUtils().isRegistered(WorldSwitch.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new WorldSwitch(), ItemJoin.getInstance());
		}
		if ((Utils.getUtils().containsIgnoreCase(this.getHotbarTriggers(), "RESPAWN") || this.triggerEnabled("Respawn")) && !Utils.getUtils().isRegistered(Respawn.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Respawn(), ItemJoin.getInstance());
		}
		if (Utils.getUtils().containsIgnoreCase(this.getHotbarTriggers(), "LIMIT-MODES") && !Utils.getUtils().isRegistered(LimitSwitch.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new LimitSwitch(), ItemJoin.getInstance());
		}
		if ((this.clearEnabled("Region-Enter") || Utils.getUtils().containsIgnoreCase(this.getHotbarTriggers(), "REGION-ENTER")) && !Utils.getUtils().isRegistered(PlayerGuard.class.getSimpleName()) && DependAPI.getDepends(false).getGuard().guardEnabled()) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerGuard(), ItemJoin.getInstance());
		}
		if (Utils.getUtils().containsIgnoreCase(this.getHotbarTriggers(), "REGION-LEAVE") && !Utils.getUtils().isRegistered(PlayerGuard.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerGuard(), ItemJoin.getInstance());
		}
		DependAPI.getDepends(false).sendUtilityDepends();
		int customItems = (ConfigHandler.getConfig(false).getConfigurationSection() != null ? ConfigHandler.getConfig(false).getConfigurationSection().getKeys(false).size() : 0);
		ServerHandler.getServer().logInfo(customItems + " Custom item(s) loaded!");
		this.registerGlow();
	}
	
   /**
    * Registers new instances of the plugin classes.
    * 
    */
	private void registerClasses() {
		this.copyFile("config.yml", "config-Version", 7);
		this.copyFile("items.yml", "items-Version", 7);
		this.copyFile(LanguageAPI.getLang(true).getFile(), LanguageAPI.getLang(false).getFile().split("-")[0] + "-Version", 8);
		this.registerPrevent();
		DependAPI.getDepends(true);
		LogFilter.getFilter(true);
		if (!PlayerLogin.hasStarted()) {
			SQLite.getLite(true);
			ItemDesigner.getDesigner(true);
			ServerHandler.getServer().runThread(main -> { PlayerLogin.startComplete(); }, 3L);
		} else {
			ServerHandler.getServer().runAsyncThread(async -> { SQLite.getLite(true); });
			ServerHandler.getServer().runThread(main -> { ItemDesigner.getDesigner(true); }, 2L);
		}
		ServerHandler.getServer().runThread(main -> { Metrics.getMetrics(true); }, 100L);
	}
	
   /**
    * Registers the GLOBAL prevent actions.
    * 
    */
	private void registerPrevent() {
		if ((!Utils.getUtils().containsIgnoreCase(this.getPrevent("Pickups"), "FALSE") && !Utils.getUtils().containsIgnoreCase(this.getPrevent("Pickups"), "DISABLED"))) {
			if (ServerHandler.getServer().hasSpecificUpdate("1_12") && Reflection.getReflection().getBukkitClass("event.entity.EntityPickupItemEvent") != null && !Utils.getUtils().isRegistered(Pickups.class.getSimpleName())) { 
				ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Pickups(), ItemJoin.getInstance()); 
			} else { LegacyAPI.getLegacy().registerPickups(); }
		}
		if ((!Utils.getUtils().containsIgnoreCase(this.getPrevent("itemMovement"), "FALSE") && !Utils.getUtils().containsIgnoreCase(this.getPrevent("itemMovement"), "DISABLED"))) {
			if (!Utils.getUtils().isRegistered(Clicking.class.getSimpleName())) { 
				ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Clicking(), ItemJoin.getInstance()); 
				if (ServerHandler.getServer().hasSpecificUpdate("1_8") && !ProtocolManager.getManager().isHandling()) { ProtocolManager.getManager().handleProtocols(); }
			}
		}
		if ((!Utils.getUtils().containsIgnoreCase(this.getPrevent("Self-Drops"), "FALSE") && !Utils.getUtils().containsIgnoreCase(this.getPrevent("Self-Drops"), "DISABLED"))
		|| (!Utils.getUtils().containsIgnoreCase(this.getPrevent("Death-Drops"), "FALSE") && !Utils.getUtils().containsIgnoreCase(this.getPrevent("Death-Drops"), "DISABLED"))) {
			if (!Utils.getUtils().isRegistered(Drops.class.getSimpleName())) { ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Drops(), ItemJoin.getInstance()); }
		}
	}
	
   /**
    * Registers the glow enchantment.
    * 
    */
    public void registerGlow() {
    	if (ServerHandler.getServer().hasSpecificUpdate("1_13")) {
	    	try {
	    		Field f = Enchantment.class.getDeclaredField("acceptingNew");
	    		f.setAccessible(true);
	    		f.set(null, true);
	    		Glow glow = new Glow();
	    		Enchantment.registerEnchantment(glow);
	    	} catch (IllegalArgumentException e) { } catch (Exception e) { ServerHandler.getServer().sendDebugTrace(e); }
    	}
    }

   /**
    * Gets the file from the specified path.
    * 
    * @param path - The File to be fetched.
    * @return The file.
    */
	public FileConfiguration getFile(final String path) {
		final File file = new File(ItemJoin.getInstance().getDataFolder(), path);
		if (this.configFile == null) { this.getSource(path); }
		try {
			return this.getLoadedConfig(file, false);
		} catch (Exception e) {
			ServerHandler.getServer().sendSevereTrace(e);
			ServerHandler.getServer().logSevere("Cannot load " + file.getName() + " from disk!");
		}
		return null;
	}
	
   /**
    * Gets the source file from the specified path.
    * 
    * @param path - The File to be loaded.
    * @return The source file.
    */
	public FileConfiguration getSource(final String path) {
		final File file = new File(ItemJoin.getInstance().getDataFolder(), path);
		if (!(file).exists()) {
			try {
				InputStream source;
				final File dataDir = ItemJoin.getInstance().getDataFolder();
				if (!dataDir.exists()) { dataDir.mkdir(); }
				if (!path.contains("lang.yml")) { source = ItemJoin.getInstance().getResource("files/configs/" + path); } 
				else { source = ItemJoin.getInstance().getResource("files/locales/" + path); }
        		if (!file.exists()) { Files.copy(source, file.toPath(), new CopyOption[0]); }
				if (path.contains("items.yml")) { this.Generating = true; }
			} catch (Exception e) {
				ServerHandler.getServer().sendSevereTrace(e);
				ServerHandler.getServer().logWarn("Cannot save " + path + " to disk!");
				this.noSource.put(path, true);
				return null;
			}
		}
		try {
			YamlConfiguration config = this.getLoadedConfig(file, true);
			this.noSource.put(path, false);
			return config;
		} catch (Exception e) {
			ServerHandler.getServer().sendSevereTrace(e);
			ServerHandler.getServer().logSevere("Cannot load " + file.getName() + " from disk!");
			this.noSource.put(file.getName(), true);
		}
		return null;
	}

   /**
    * Gets the file and loads it into memory if specified.
    * 
    * @param file - The file to be loaded.
    * @param commit - If the File should be committed to memory.
    * @return The Memory loaded config file.
    */
	public YamlConfiguration getLoadedConfig(final File file, final boolean commit) throws Exception {
		YamlConfiguration config = new YamlConfiguration();
		if (commit) { config.load(file); }
		if (file.getName().contains("items.yml")) {
			if (commit) { this.itemsFile = config; }
			return this.itemsFile;
		} else if (file.getName().contains("config.yml")) {
			if (commit) { this.configFile = config; }
			return this.configFile;
		} else if (file.getName().contains("lang.yml")) {
			if (commit) { this.langFile = config; }
			return this.langFile;
		}
		return null;
	}

   /**
    * Copies the specified config file to the data folder.
    * 
    * @param configFile - The name and extension of the config file to be copied.
    * @param version - The version String to be checked in the config file.
    * @param id - The expected version id to be found in the config file.
    */
	private void copyFile(final String configFile, final String version, final int id) {
		this.getSource(configFile);
		File File = new File(ItemJoin.getInstance().getDataFolder(), configFile);
		if (File.exists() && !this.noSource.get(configFile) && this.getFile(configFile).getInt(version) != id) {
			InputStream source;
			if (!configFile.contains("lang.yml")) { source = ItemJoin.getInstance().getResource("files/configs/" + configFile); } 
			else { source = ItemJoin.getInstance().getResource("files/locales/" + configFile); }
			if (source != null) {
				String[] namePart = configFile.split("\\.");
				String renameFile = namePart[0] + "-old-" + Utils.getUtils().getRandom(1, 50000) + namePart[1];
				File renamedFile = new File(ItemJoin.getInstance().getDataFolder(), renameFile);
				if (!renamedFile.exists()) {
					File.renameTo(renamedFile);
					File copyFile = new File(ItemJoin.getInstance().getDataFolder(), configFile);
					copyFile.delete();
					this.getSource(configFile);
					ServerHandler.getServer().logWarn("Your " + configFile + " is out of date and new options are available, generating a new one!");
				}
			}
		} else if (this.noSource.get(configFile)) {
			ServerHandler.getServer().logSevere("Your " + configFile + " is not using proper YAML Syntax and will not be loaded!");
			ServerHandler.getServer().logSevere("Check your YAML formatting by using a YAML-PARSER such as http://yaml-online-parser.appspot.com/");
		}
		if (!this.noSource.get(configFile)) { 
			if (this.Generating && configFile.equalsIgnoreCase("items.yml")) { 
				FileData.getData().generateItemsFile();
				this.getSource("items.yml");
				this.Generating = false;
			}
			this.getFile(configFile).options().copyDefaults(false);
			if (configFile.contains("lang.yml")) { LanguageAPI.getLang(false).setPrefix(); }
		}
	}
	
   /**
    * Gets the Hotbar slot that is defined to be set.
    * 
    * @return The Integer hotbar value.
    */
	public int getHotbarSlot() { 
		if (this.getFile("config.yml").getString("Settings.HeldItem-Slot") != null 
				&& !this.getFile("config.yml").getString("Settings.HeldItem-Slot").equalsIgnoreCase("DISABLED") 
				&& Utils.getUtils().isInt(this.getFile("config.yml").getString("Settings.HeldItem-Slot"))) {
			return this.getFile("config.yml").getInt("Settings.HeldItem-Slot");
		}
		return -1;
	}
	
   /**
    * Gets the Hotbar Triggers that is defined to be set.
    * 
    * @return The String list of hotbar triggers.
    */
	public String getHotbarTriggers() { 
		if (this.getFile("config.yml").getString("Settings.HeldItem-Triggers") != null 
				&& !this.getFile("config.yml").getString("Settings.HeldItem-Triggers").equalsIgnoreCase("DISABLED") 
				&& !this.getFile("config.yml").getString("Settings.HeldItem-Triggers").equalsIgnoreCase("FALSE")) {
			return this.getFile("config.yml").getString("Settings.HeldItem-Triggers");
		}
		return "";
	}
	
   /**
    * Checks if the specified clear items type is enabled.
    * 
    * @param type - The item clearing trigger.
    * @return If the clear type is enabled.
    */
	public boolean clearEnabled(String type) {
		if (this.getFile("config.yml").getString("Clear-Items." + type) != null 
		&& !this.getFile("config.yml").getString("Clear-Items." + type).equalsIgnoreCase("DISABLED") && !this.getFile("config.yml").getString("Clear-Items." + type).equalsIgnoreCase("FALSE")) {
			return true;
		}
		return false;
	}
	
   /**
    * Checks if the specified trigger commands is enabled.
    * 
    * @param type - The commands trigger.
    * @return If the trigger type is enabled.
    */
	public boolean triggerEnabled(String type) {
		if (this.getFile("config.yml").getString("Active-Commands.triggers") != null && Utils.getUtils().containsIgnoreCase(this.getFile("config.yml").getString("Active-Commands.triggers"), type)
			&& (!this.getFile("config.yml").getString("Active-Commands.enabled-worlds").equalsIgnoreCase("DISABLED") && !this.getFile("config.yml").getString("Active-Commands.enabled-worlds").equalsIgnoreCase("FALSE"))) {
			return true;
		}
		return false;
	}

   /**
    * Checks if Debugging is enabled.
    * 
    * @return If Debugging is enabled.
    */
	public boolean debugEnabled() {
		return this.getFile("config.yml").getBoolean("General.Debugging");
	}
	
   /**
    * Gets the defined Prevent.
    * 
    * @param name - The name of the Prevent.
    * @return The defined Prevent as a String to compare later.
    */
	public String getPrevent(final String name) {
		return this.getFile("config.yml").getString("Prevent." + name);
	}
	
   /**
    * Checks if OP Bypass is enabled for Prevent actions.
    * 
    * @return If OP is defined for the Prevent actions.
    */
	public boolean isPreventOP() {
		return Utils.getUtils().containsIgnoreCase(this.getFile("config.yml").getString("Prevent.Bypass"), "OP");
	}
	
   /**
    * Checks if CREATIVE Bypass is enabled for Prevent actions.
    * 
    * @return If CREATIVE is defined for the Prevent actions.
    */
	public boolean isPreventCreative() {
		return Utils.getUtils().containsIgnoreCase(this.getFile("config.yml").getString("Prevent.Bypass"), "CREATIVE");
	}
	
   /**
    * Checks if items are defined correctly in the items.yml.
    * 
    * @return If items exist in the items.yml.
    */
	public boolean itemsExist() {
		if (this.getConfigurationSection() != null) {
			return true;
		} else if (this.getConfigurationSection() == null) {
			ServerHandler.getServer().logSevere("{Config} There are no items detected in the items.yml.");
			ServerHandler.getServer().logWarn("{Config} Try adding an item to the items section in the items.yml.");
			return false;
		}
		return false;
	}
	
   /**
    * Gets the material defined for the custom item.
    * 
    * @param nodeLocation - The path to the custom items node.
    * @return The material set for the custom item.
    */
	public ConfigurationSection getMaterialSection(final ConfigurationSection nodeLocation) {
		return (this.getFile("items.yml") != null ? this.getFile("items.yml").getConfigurationSection(nodeLocation.getCurrentPath() + ".id") : null);
	}
	
   /**
    * Gets the list of commands defined for the custom item.
    * 
    * @param nodeLocation - The path to the custom items node.
    * @return The list of commands defined for the custom item.
    */
	public ConfigurationSection getCommandsSection(final ConfigurationSection nodeLocation) {
		return (this.getFile("items.yml") != null ? this.getFile("items.yml").getConfigurationSection(nodeLocation.getCurrentPath() + ".commands") : null);
	}
	
   /**
    * Gets the Options defined for the custom item.
    * 
    * @param nodeName - The node name of the custom item in the items.yml.
    * @return The list of options defined for the custom item.
    */
	public ConfigurationSection getItemSection(final String nodeName) {
		return (this.getConfigurationSection() != null ? this.getConfigurationSection().getConfigurationSection(nodeName) : null);
	}
	
   /**
    * Gets the ConfigurationSection for the list of custom items.
    * 
    * @return The list of custom items.
    */
	public ConfigurationSection getConfigurationSection() {
		return (this.getFile("items.yml") != null ? this.getFile("items.yml").getConfigurationSection("items") : null);
	}
	
   /**
    * Calculates the exact numer of arbitrary slots,
    * assigning numbers to each slot to uniquely identify each item.
    * 
    * @param slot - The numerical or custom slot value.
    * @return The exact slot ID to be set to the item.
    */
	private int ArbitraryID = 0;
	public String getItemID(String slot) {
		if (slot.equalsIgnoreCase("Arbitrary")) {
			this.ArbitraryID += 1;
			slot += this.ArbitraryID;
		}
		return slot;
	}
	
   /**
    * Registers Events that are utilized by the specified ItemMap.
    * 
    * @param itemMap - The ItemMap that needs its events registered.
    */
	public void registerListeners(final ItemMap itemMap) {
		if (((!itemMap.isGiveOnDisabled() && itemMap.isGiveOnJoin()) || itemMap.isAutoRemove() || (this.getFile("config.yml").getString("Active-Commands.enabled-worlds") != null 
			&& (!this.getFile("config.yml").getString("Active-Commands.enabled-worlds").equalsIgnoreCase("DISABLED") || !this.getFile("config.yml").getString("Active-Commands.enabled-worlds").equalsIgnoreCase("FALSE")))) 
			&& !Utils.getUtils().isRegistered(PlayerJoin.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerJoin(), ItemJoin.getInstance());
		}
		if (((!itemMap.isGiveOnDisabled() && itemMap.isGiveOnRespawn()) || itemMap.isAutoRemove()) && !Utils.getUtils().isRegistered(Respawn.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Respawn(), ItemJoin.getInstance());
		}
		if (((!itemMap.isGiveOnDisabled() && itemMap.isGiveOnWorldSwitch()) || itemMap.isAutoRemove()) && !Utils.getUtils().isRegistered(WorldSwitch.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new WorldSwitch(), ItemJoin.getInstance());
		}
		if (!itemMap.isGiveOnDisabled() && (itemMap.isGiveOnRegionEnter() || itemMap.isTakeOnRegionLeave()) 
			&& !Utils.getUtils().isRegistered(PlayerGuard.class.getSimpleName()) && DependAPI.getDepends(false).getGuard().guardEnabled()) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerGuard(), ItemJoin.getInstance());
		}
		if (!itemMap.isGiveOnDisabled() && itemMap.isUseOnLimitSwitch() && !Utils.getUtils().isRegistered(LimitSwitch.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new LimitSwitch(), ItemJoin.getInstance());
		}
		if ((itemMap.isAnimated() || itemMap.isDynamic()) && !Utils.getUtils().isRegistered(PlayerQuit.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerQuit(), ItemJoin.getInstance());
		}
		if (itemMap.mobsDrop() && !Utils.getUtils().isRegistered(Entities.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Entities(), ItemJoin.getInstance());
		}
		if (itemMap.blocksDrop() && !Utils.getUtils().isRegistered(Breaking.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Breaking(), ItemJoin.getInstance());
		}
		if (itemMap.isCraftingItem() && !Utils.getUtils().isRegistered(Crafting.class.getSimpleName())) {
			Crafting.cycleTask();
			ServerHandler.getServer().runThread(main -> {
				PlayerHandler.getPlayer().restoreCraftItems();
				if (ServerHandler.getServer().hasSpecificUpdate("1_8") && !ProtocolManager.getManager().isHandling()) { ProtocolManager.getManager().handleProtocols(); }
			}, 40L);
			if (!Utils.getUtils().isRegistered(PlayerQuit.class.getSimpleName())) {
				ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerQuit(), ItemJoin.getInstance());
			}
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Crafting(), ItemJoin.getInstance());
		}
		if ((itemMap.isMovement() || itemMap.isInventoryClose()) && !Utils.getUtils().isRegistered(Clicking.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Clicking(), ItemJoin.getInstance());
			if (ServerHandler.getServer().hasSpecificUpdate("1_8") && !ProtocolManager.getManager().isHandling()) { ProtocolManager.getManager().handleProtocols(); }
		}
		if (ServerHandler.getServer().hasSpecificUpdate("1_12") && itemMap.isStackable() && !Utils.getUtils().isRegistered(Stackable.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Stackable(), ItemJoin.getInstance());
		} else if (itemMap.isStackable()) {
			LegacyAPI.getLegacy().registerStackable();
		}
		if ((itemMap.isDeathDroppable() || itemMap.isSelfDroppable()) && !Utils.getUtils().isRegistered(Drops.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Drops(), ItemJoin.getInstance());
		}
		if (itemMap.getCommands() != null && itemMap.getCommands().length != 0) {
			if (ServerHandler.getServer().hasSpecificUpdate("1_8") && !Utils.getUtils().isRegistered(Commands.class.getSimpleName())) {
				ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Commands(), ItemJoin.getInstance());
			} else {
				LegacyAPI.getLegacy().registerCommands();
			}
		}
		if ((itemMap.isCancelEvents() || itemMap.isSelectable() || itemMap.getInteractCooldown() != 0)) {
			if (!Utils.getUtils().isRegistered(Interact.class.getSimpleName())) {
				ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Interact(), ItemJoin.getInstance());
			}
		}
		if ((itemMap.isPlaceable() || itemMap.isCountLock()) && !Utils.getUtils().isRegistered(Placement.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Placement(), ItemJoin.getInstance());
		}
		if (((Utils.getUtils().containsIgnoreCase(itemMap.getMaterial().name(), "TOTEM") && itemMap.isCountLock()) || itemMap.isCustomConsumable())) {
			if (ServerHandler.getServer().hasSpecificUpdate("1_11") && !Utils.getUtils().isRegistered(Consumes.class.getSimpleName())) {
				ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Consumes(), ItemJoin.getInstance());
			} else {
				LegacyAPI.getLegacy().registerConsumes();
			}
		}
		if ((itemMap.isItemRepairable() || itemMap.isItemCraftable()) && !Utils.getUtils().isRegistered(Recipes.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Recipes(), ItemJoin.getInstance());
		}
		if (itemMap.isItemStore() || itemMap.isItemModify()) {
			if (ServerHandler.getServer().hasSpecificUpdate("1_8") && !Utils.getUtils().isRegistered(Storable.class.getSimpleName())) {
				ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Storable(), ItemJoin.getInstance());
			} else {
				LegacyAPI.getLegacy().registerStorable();
			}
		}
		if (itemMap.isMovement() && ServerHandler.getServer().hasSpecificUpdate("1_9") && Reflection.getReflection().getBukkitClass("event.player.PlayerSwapHandItemsEvent") != null && !Utils.getUtils().isRegistered(Offhand.class.getSimpleName())) {
			ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Offhand(), ItemJoin.getInstance());
		}
	}
	
   /**
    * Gets the instance of the ConfigHandler.
    * 
    * @param regen - If the instance should be regenerated.
    * @return The ConfigHandler instance.
    */
    public static ConfigHandler getConfig(final boolean regen) { 
        if (config == null || regen) {
        	config = new ConfigHandler(); 
        	config.registerClasses();
        }
        return config; 
    } 
}