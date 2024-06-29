package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SuggestionHelper {

  public static List<String> fromStream(Stream<String> stream, String query) {
    return stream
        .filter(str -> str.toLowerCase().startsWith(query.toLowerCase()))
        .sorted()
        .toList();
  }

  public static List<String> worlds(String query) {
    return fromStream(Bukkit.getWorlds().stream().map(WorldInfo::getName), query);
  }

  public static List<String> onlinePlayers(String query) {
    return fromStream(Bukkit.getOnlinePlayers().stream().map(Player::getName), query);
  }

  public static List<String> offlinePlayers(String query) {
    return fromStream(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName), query);
  }

  public static List<String> collections(String query) {
    return fromStream(FindMe.inst().getCollections().stream().map(col -> col.name), query);
  }

  public static List<String> pages(String query, int totalPages) {
    List<String> suggestions = new ArrayList<>();
    for (int i = 1; i <= totalPages; i++) {
      String str = String.valueOf(i);
      if (str.startsWith(query)) {
        suggestions.add(str);
      }
    }
    return suggestions;
  }

  public static List<String> pages(String query, int totalItems, int pageSize) {
    return pages(query, (int) Math.ceil((float) totalItems / pageSize));
  }

}
