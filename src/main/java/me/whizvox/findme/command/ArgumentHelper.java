package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class ArgumentHelper {

  public static <T> T getArgument(CommandContext context, int index, Supplier<T> defaultValue, Function<String, T> parser) {
    if (index >= 0 && index < context.args().size()) {
      return parser.apply(context.args().get(index));
    }
    return defaultValue.get();
  }

  public static <T> T getArgument(CommandContext context, int index, Function<String, T> parser) {
    return getArgument(context, index, InterruptCommandException::showUsage, parser);
  }

  public static String getString(CommandContext context, int index, Supplier<String> defaultValue) {
    return getArgument(context, index, defaultValue, s -> s);
  }

  public static String getString(CommandContext context, int index) {
    return getString(context, index, InterruptCommandException::showUsage);
  }

  public static int getInt(CommandContext context, int index, Supplier<Integer> defaultValue, int min, int max) {
    try {
      int value = getArgument(context, index, defaultValue, Integer::parseInt);
      if (value < min || value > max) {
        throw new InterruptCommandException(FindMe.inst().translate(FMStrings.ERROR_INT_OUT_OF_RANGE, value), false);
      }
      return value;
    } catch (NumberFormatException e) {
      throw new InterruptCommandException(FindMe.inst().translate(FMStrings.ERROR_INVALID_INT), false);
    }
  }

  public static int getInt(CommandContext context, int index, int min, int max) {
    return getInt(context, index, InterruptCommandException::showUsage, min, max);
  }

  public static String getLimitedString(CommandContext context, int index, Supplier<String> defaultValue, Collection<String> possibleValues) {
    String str = getString(context, index, defaultValue);
    if (possibleValues.contains(str)) {
      return str;
    }
    return InterruptCommandException.halt("Invalid argument, must be one of [" + String.join(", ", possibleValues) + "]");
  }

  public static String getLimitedString(CommandContext context, int index, Collection<String> possibleValues) {
    String str = getString(context, index, InterruptCommandException::showUsage);
    if (possibleValues.contains(str)) {
      return str;
    }
    return InterruptCommandException.halt("Invalid argument, must be one of [" + String.join(", ", possibleValues) + "]");
  }

  public static FindableCollection getCollection(CommandContext context, int index, Supplier<FindableCollection> defaultValue) {
    return getArgument(context, index,
        defaultValue,
        name -> FindMe.inst().getCollections().getCollection(name).orElseGet(() -> InterruptCommandException.halt(FindMe.inst().translate(FMStrings.ERR_UNKNOWN_COLLECTION, name)))
    );
  }

  public static FindableCollection getCollection(CommandContext context, int index, boolean defaultIfMissing) {
    return getCollection(context, index,
        () -> defaultIfMissing ? FindMe.inst().getCollections().getDefaultCollection() : InterruptCommandException.showUsage()
    );
  }

  public static Findable<?> getFindable(CommandContext context, int index, Supplier<Findable<?>> defaultValue) {
    return getArgument(context, index, defaultValue, str -> {
      try {
        int id = Integer.parseInt(str);
        Findable<?> findable = FindMe.inst().getFindables().get(id);
        if (findable == null) {
          return InterruptCommandException.halt(FindMe.inst().translate(FMStrings.ERR_UNKNOWN_FINDABLE, str));
        }
        return findable;
      } catch (NumberFormatException e) {
        return InterruptCommandException.halt(FindMe.inst().translate(FMStrings.ERR_UNKNOWN_FINDABLE, str));
      }
    });
  }

  public static Findable<?> getFindable(CommandContext context, int index) {
    return getFindable(context, index, InterruptCommandException::showUsage);
  }

  public static Entity getEntity(CommandContext context, int index, Supplier<Entity> defaultValue) {
    return getArgument(context, index, defaultValue, arg -> {
      try {
        UUID entityId = UUID.fromString(arg);
        Entity entity = Bukkit.getEntity(entityId);
        if (entity == null) {
          return InterruptCommandException.halt("Unknown entity ID: " + arg);
        }
        return entity;
      } catch (IllegalArgumentException e) {
        return InterruptCommandException.halt("Unknown entity ID: " + arg);
      }
    });
  }

  public static Entity getEntity(CommandContext context, int index) {
    return getEntity(context, index, () -> {
      Entity entity = FMUtils.getLookingAtEntity(context.getPlayer(), e -> true);
      if (entity == null) {
        return InterruptCommandException.halt("No entity found");
      }
      return entity;
    });
  }

  public static Location getBlockLocation(CommandContext context, int index, Supplier<Location> defaultValue) {
    if (context.argCount() < index + 3) {
      return defaultValue.get();
    }
    int x = getInt(context, index, Integer.MIN_VALUE, Integer.MAX_VALUE);
    int y = getInt(context, index + 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
    int z = getInt(context, index + 2, Integer.MIN_VALUE, Integer.MAX_VALUE);
    World world;
    if (context.argCount() < 4) {
      world = context.getPlayer().getWorld();
    } else {
      String worldName = getString(context, index + 3);
      // first try to interpret it as a UUID
      if (worldName.length() == 36) {
        try {
          UUID worldId = UUID.fromString(worldName);
          world = Bukkit.getWorld(worldId);
        } catch (IllegalArgumentException e) {
          world = Bukkit.getWorld(worldName);
        }
      } else {
        world = Bukkit.getWorld(worldName);
      }
      if (world == null) {
        context.sendMessage("Could not find world with name or ID " + worldName);
      }
    }
    return new Location(world, x, y, z);
  }

  public static Location getBlockLocation(CommandContext context, int index, boolean defaultPlayerLookingAt) {
    return getBlockLocation(context, index, () -> {
      if (!defaultPlayerLookingAt) {
        return InterruptCommandException.showUsage();
      }
      Block block = FMUtils.getLookingAtBlock(context.getPlayer());
      if (block == null) {
        return InterruptCommandException.halt(FindMe.inst().translate(FMStrings.ERROR_NO_BLOCK_FOUND));
      }
      return block.getLocation();
    });
  }

  public static OfflinePlayer getOfflinePlayer(CommandContext context, int index, Supplier<OfflinePlayer> defaultValue) {
    return getArgument(context, index, defaultValue, str -> {
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
      return InterruptCommandException.halt(FindMe.inst().translate(FMStrings.ERR_UNKNOWN_PLAYER, str));
    });
  }

  public static OfflinePlayer getOfflinePlayer(CommandContext context, int index) {
    return getOfflinePlayer(context, index, InterruptCommandException::showUsage);
  }

}
