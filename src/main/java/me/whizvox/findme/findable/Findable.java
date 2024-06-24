package me.whizvox.findme.findable;

import me.whizvox.findme.core.findable.FindableDbo;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public record Findable<T>(int id, int collectionId, FindableType type, T object) {

  public boolean isEmpty() {
    return object == null;
  }

  public static Findable<Block> ofBlock(int id, int collectionId, Block block) {
    return new Findable<>(id, collectionId, FindableType.BLOCK, block);
  }

  public static Findable<Entity> ofEntity(int id, int collectionId, Entity entity) {
    return new Findable<>(id, collectionId, FindableType.ENTITY, entity);
  }

  public static final Findable<Block> NULL_BLOCK = new Findable<>(0, 0, FindableType.BLOCK, null);

  public static final Findable<Entity> NULL_ENTITY = new Findable<>(0, 0, FindableType.ENTITY, null);

}
