package me.whizvox.findme;

import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.FindMeCommandDelegator;
import me.whizvox.findme.core.LocalizationManager;
import me.whizvox.findme.core.collection.CollectionManager;
import me.whizvox.findme.core.findable.FindableManager;
import me.whizvox.findme.core.founditem.FoundItemManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public final class FindMe extends JavaPlugin {

  private Connection conn;
  private CollectionManager collections;
  private FindableManager findables;
  private FoundItemManager foundItems;
  private LocalizationManager l10n;

  private File stringsFile;
  private File collectionsFile;

  public FindMe() {
    instance = this;
    conn = null;
    findables = null;
    l10n = null;
  }

  public CollectionManager getCollections() {
    return collections;
  }

  public FindableManager getFindables() {
    return findables;
  }

  public FoundItemManager getFoundItems() {
    return foundItems;
  }

  public LocalizationManager getLocalizationManager() {
    return l10n;
  }

  public String translate(String key) {
    return l10n.translate(key);
  }

  public String translate(String key, Object... args) {
    return l10n.translate(key).formatted(args);
  }

  private void loadOtherConfigurations() {
    boolean loadStrings = stringsFile.exists();
    FileConfiguration stringsConfig = YamlConfiguration.loadConfiguration(stringsFile);
    if (loadStrings) {
      l10n.read(stringsConfig);
      getLogger().info("Strings loaded");
    } else {
      FMStrings.getDefaults().forEach(stringsConfig::set);
      l10n.read(stringsConfig);
      try {
        stringsConfig.save(stringsFile);
        getLogger().info("Loaded and saved default strings");
      } catch (IOException e) {
        getLogger().log(Level.WARNING, "Could not save strings configuration", e);
      }
    }
    boolean loadCollections = collectionsFile.exists();
    FileConfiguration collectionsConfig = YamlConfiguration.loadConfiguration(collectionsFile);
    collections.load(collectionsConfig);
    if (loadCollections) {
      getLogger().info("Collections loaded");
    } else {
      collections.save(collectionsConfig);
      try {
        collectionsConfig.save(collectionsFile);
      } catch (IOException e) {
        getLogger().log(Level.WARNING, "Could not generate default collections config", e);
      }
      getLogger().info("Created default collection");
    }
  }

  public void reloadPlugin() {
    reloadConfig();
    loadOtherConfigurations();
  }

  public void saveCollections() {
    FileConfiguration collectionsConfig = new YamlConfiguration();
    collections.save(collectionsConfig);
    try {
      collectionsConfig.save(collectionsFile);
      getLogger().fine("Collections saved");
    } catch (IOException e) {
      getLogger().log(Level.WARNING, "Could not save collections file", e);
    }
  }

  @Override
  public void onEnable() {
    getDataFolder().mkdirs();
    File dbFile = new File(getDataFolder(), "findme.db");
    try {
      conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    } catch (SQLException e) {
      getLogger().log(Level.SEVERE, "Could not connect to SQLite database file", e);
      getPluginLoader().disablePlugin(this);
      return;
    }
    collections = new CollectionManager();
    findables = new FindableManager(conn);
    foundItems = new FoundItemManager(conn);
    l10n = new LocalizationManager();
    stringsFile = new File(getDataFolder(), "strings.yml");
    collectionsFile = new File(getDataFolder(), "collections.yml");
    loadOtherConfigurations();

    FindMeCommandDelegator commandDelegator = new FindMeCommandDelegator();
    PluginCommand command = getCommand("findme");
    command.setExecutor(commandDelegator);
    command.setTabCompleter(commandDelegator);
  }

  @Override
  public void onDisable() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        getLogger().log(Level.WARNING, "Could not close SQLite database connection", e);
      }
    }
  }

  private static FindMe instance = null;

  public static FindMe inst() {
    return instance;
  }

}
