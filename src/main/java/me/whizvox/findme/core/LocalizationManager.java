package me.whizvox.findme.core;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class LocalizationManager {

  private final Map<String, String> strings;

  public LocalizationManager() {
    strings = new HashMap<>();
  }

  public void read(FileConfiguration config) {
    strings.clear();
    config.getKeys(true).forEach(key -> {
      String value = config.getString(key);
      strings.put(key, ChatColor.translateAlternateColorCodes('&', value));
    });
  }

  public String translate(String key) {
    return strings.getOrDefault(key, ChatColor.RED + key);
  }

}
