package me.whizvox.findme.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FMUtils {

  @Nullable
  public static Block getLookingAtBlock(Player player) {
    RayTraceResult hit = player.rayTraceBlocks(5.0);
    return hit != null ? hit.getHitBlock() : null;
  }

  @Nullable
  public static Entity getLookingAtEntity(Player player, Predicate<Entity> filter) {
    RayTraceResult hit = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), 5.0,
        entity -> entity != player && filter.test(entity)
    );
    return hit != null ? hit.getHitEntity() : null;
  }

  @Nullable
  public static RayTraceResult getLookingAt(Player player, Predicate<Entity> entityFilter) {
    Location pLoc = player.getEyeLocation();
    RayTraceResult bHit = player.rayTraceBlocks(5.0);
    RayTraceResult eHit = player.getWorld().rayTraceEntities(pLoc, pLoc.getDirection(), 5.0,
        entity -> entity != player && entityFilter.test(entity)
    );
    if (bHit == null) {
      return eHit;
    } else if (eHit == null) {
      return bHit;
    }
    return bHit.getHitPosition().distanceSquared(pLoc.toVector()) <
        eHit.getHitPosition().distanceSquared(pLoc.toVector()) ? bHit : eHit;
  }

  public static String format(String str, Map<String, Object> args) {
    String finalStr = str;
    for (String key : args.keySet()) {
      finalStr = finalStr.replaceAll(Pattern.quote("{" + key + "}"), String.valueOf(args.get(key)));
    }
    return finalStr;
  }

}
