package me.whizvox.findme.findable;

import me.whizvox.findme.FindMe;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class FindableUtils {

  public static Findable<Block> getLookingAtBlock(Player player) {
    RayTraceResult hit = player.rayTraceBlocks(5.0);
    if (hit != null) {
      Block block = hit.getHitBlock();
      if (block != null) {
        return FindMe.inst().getFindables().getBlock(block.getLocation());
      }
    }
    return Findable.NULL_BLOCK;
  }

  public static Findable<Entity> getLookingAtEntity(Player player) {
    RayTraceResult hit = player.rayTraceBlocks(5.0);
    if (hit != null) {
      Entity entity = hit.getHitEntity();
      if (entity != null) {
        return FindMe.inst().getFindables().getEntity(entity);
      }
    }
    return Findable.NULL_ENTITY;
  }

}
