package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.UUID;

public class MetaArgumentHelper {

  public static boolean parseBoolean(String str) {
    if (str.equals("true") || str.equals("1")) {
      return true;
    }
    if (str.equals("false") || str.equals("0")) {
      return false;
    }
    return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INVALID_BOOLEAN, str));
  }

  public static int parseInt(String str, int min, int max) {
    try {
      int value = Integer.parseInt(str);
      if (value < min || value > max) {
        return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INT_OUT_OF_RANGE, value, min, max));
      }
      return value;
    } catch (NumberFormatException e) {
      return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INVALID_INT, str));
    }
  }

  public static int parseInt(String str, int min) {
    return parseInt(str, min, Integer.MAX_VALUE);
  }

  public static FindableCollection parseCollection(String str) {
    return FindMe.inst().getCollections()
        .getCollection(str)
        .orElseGet(() -> InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_UNKNOWN_COLLECTION, str)));
  }

  public static Findable<?> parseFindable(String str) {
    try {
      int id = Integer.parseInt(str);
      Findable<?> findable = FindMe.inst().getFindables().get(id);
      if (findable == null) {
        return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_UNKNOWN_FINDABLE, str));
      }
      return findable;
    } catch (NumberFormatException e) {
      return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_UNKNOWN_FINDABLE, str));
    }
  }

  public static World parseWorld(String query) {
    World world;
    // first try to interpret it as a UUID
    if (query.length() == 36) {
      try {
        UUID worldId = UUID.fromString(query);
        world = Bukkit.getWorld(worldId);
      } catch (IllegalArgumentException e) {
        world = Bukkit.getWorld(query);
      }
    } else {
      world = Bukkit.getWorld(query);
    }
    if (world == null) {
      return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_UNKNOWN_WORLD, query));
    }
    return world;
  }

  public static OfflinePlayer parseOfflinePlayer(String str) {
    if (str.length() == 36) {
      try {
        UUID id = UUID.fromString(str);
        return Bukkit.getOfflinePlayer(id);
      } catch (IllegalArgumentException ignored) {}
    }
    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
      if (player.getName().equalsIgnoreCase(str)) {
        return player;
      }
    }
    return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_UNKNOWN_PLAYER, str));
  }

  private static Duration parseDuration(String str) {
    long seconds = 0L;
    int last = 0;
    char c;
    for (int i = 0; i < str.length(); i++) {
      c = str.charAt(i);
      if (c == 'd') {
        seconds = Integer.parseInt(str.substring(last, i)) * 86400L;
        last = i + 1;
      } else if (c == 'h') {
        seconds = Integer.parseInt(str.substring(last, i)) * 3600L;
        last = i + 1;
      } else if (c == 'm') {
        seconds = Integer.parseInt(str.substring(last, i)) * 60L;
        last = i + 1;
      } else if (c == 's') {
        seconds = Integer.parseInt(str.substring(last, i));
        last = i + 1;
      }
    }
    return Duration.ofSeconds(seconds);
  }

  public static LocalDateTime parseTimestamp(String str) {
    try {
      Duration duration = parseDuration(str);
      return LocalDateTime.now().minus(duration);
    } catch (NumberFormatException e) {
      try {
        LocalDate date = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(str));
        return date.atStartOfDay();
      } catch (DateTimeParseException e2) {
        return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INVALID_TIME));
      }
    }
  }

  public static String checkEnum(String str, Collection<String> possibleValues) {
    if (possibleValues.contains(str)) {
      return str;
    }
    return InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INVALID_ENUM, String.join(", ", possibleValues)));
  }

}
