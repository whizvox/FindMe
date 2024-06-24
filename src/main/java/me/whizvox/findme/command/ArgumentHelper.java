package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.CollectionDbo;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

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

  public static CollectionDbo getCollection(CommandContext context, int index) {
    return getArgument(context, index,
        () -> FindMe.inst().getCollections().defaultCollection,
        name -> FindMe.inst().getCollections().getRepo().findByName(name).orElseThrow(() -> new InterruptCommandException("Unknown collection: " + name, false))
    );
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
      Player player = context.getPlayer();
      RayTraceResult hit = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), 5.0, entity -> !(entity instanceof Player));
      if (hit == null) {
        return InterruptCommandException.halt("No entity found");
      }
      Entity entity = hit.getHitEntity();
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
      try {
        UUID worldId = UUID.fromString(worldName);
        world = Bukkit.getWorld(worldId);
      } catch (IllegalArgumentException e) {
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
      RayTraceResult hit = context.getPlayer().rayTraceBlocks(5.0);
      if (hit == null) {
        return InterruptCommandException.halt("Not looking at a block");
      }
      Block block = hit.getHitBlock();
      if (block == null) {
        return InterruptCommandException.halt("Not looking at a block");
      }
      return block.getLocation();
    });
  }

}
