package me.whizvox.findme.core.findable;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.findable.Findable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class FindableManager {

  private final FindableRepository repo;
  private final Map<Location, Findable<Block>> blocks;
  private final Map<UUID, Findable<Entity>> entities;
  private final Map<Integer, Findable<?>> byId;
  private final Map<Integer, Integer> collectionCounts;

  public FindableManager(Connection conn) {
    repo = new FindableRepository(conn);
    blocks = new HashMap<>();
    entities = new HashMap<>();
    byId = new HashMap<>();
    collectionCounts = new HashMap<>();
    repo.initialize();
    refresh();
  }

  public FindableRepository getRepo() {
    return repo;
  }

  public Findable<Block> getBlock(Location location) {
    return blocks.getOrDefault(location, Findable.NULL_BLOCK);
  }

  public Findable<Block> getBlock(World world, int x, int y, int z) {
    return getBlock(new Location(world, x, y, z));
  }

  public Findable<Entity> getEntity(Entity entity) {
    return entities.getOrDefault(entity.getUniqueId(), Findable.NULL_ENTITY);
  }

  public Findable<?> get(int id) {
    return byId.get(id);
  }

  public int getTotalCount() {
    return byId.size();
  }

  public int getCount(int collectionId) {
    return collectionCounts.getOrDefault(collectionId, 0);
  }

  public Findable<Block> addBlock(int collectionId, Block block) {
    FindableDbo findable = repo.addBlock(collectionId, block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    Findable<Block> findableBlock = Findable.ofBlock(findable.id(), collectionId, block);
    blocks.put(block.getLocation(), findableBlock);
    byId.put(findable.id(), findableBlock);
    collectionCounts.put(collectionId, collectionCounts.getOrDefault(collectionId, 0) + 1);
    return findableBlock;
  }

  public Findable<Entity> addEntity(int collectionId, Entity entity) {
    FindableDbo findable = repo.addEntity(collectionId, entity.getUniqueId());
    Findable<Entity> findableEntity = Findable.ofEntity(findable.id(), findable.collectionId(), entity);
    entities.put(entity.getUniqueId(), findableEntity);
    byId.put(findable.id(), findableEntity);
    collectionCounts.put(collectionId, collectionCounts.getOrDefault(collectionId, 0) + 1);
    return findableEntity;
  }

  public void remove(int id) {
    repo.deleteById(id);
    refresh();
  }

  public void removeByCollection(int collectionId) {
    repo.deleteByCollection(collectionId);
    refresh();
  }

  public void refresh() {
    blocks.clear();
    entities.clear();
    byId.clear();
    collectionCounts.clear();
    repo.findAll().forEach(findable -> {
      if (findable.isBlock()) {
        World world = Bukkit.getWorld(findable.uuid());
        if (world == null) {
          FindMe.inst().getLogger().log(Level.WARNING, "Could not load block findable (id=%d) from database, unknown world ID: %s", new Object[] {findable.id(), findable.uuid()});
        } else {
          Location loc = new Location(world, findable.x(), findable.y(), findable.z());
          Findable<Block> block = Findable.ofBlock(findable.id(), findable.collectionId(), world.getBlockAt(loc));
          blocks.put(loc, block);
          byId.put(findable.id(), block);
        }
      } else {
        Entity entity = Bukkit.getEntity(findable.uuid());
        if (entity == null) {
          FindMe.inst().getLogger().log(Level.WARNING, "Could not load entity findable (id={}) from database, unknown entity ID: {}", new Object[] { findable.id(), findable.uuid() });
        } else {
          Findable<Entity> findableEntity = Findable.ofEntity(findable.id(), findable.collectionId(), entity);
          entities.put(findable.uuid(), findableEntity);
          byId.put(findable.id(), findableEntity);
        }
      }
    });
    collectionCounts.putAll(repo.countCollections());
  }

}
