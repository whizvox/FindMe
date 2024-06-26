package me.whizvox.findme.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Predicate;

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

}
