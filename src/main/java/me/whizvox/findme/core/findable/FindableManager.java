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
  private final Map<Location, Findable<Block>> blockIds;
  private final Map<UUID, Findable<Entity>> entityIds;

  public FindableManager(Connection conn) {
    repo = new FindableRepository(conn);
    blockIds = new HashMap<>();
    entityIds = new HashMap<>();
    repo.initialize();
    refresh();
  }

  public FindableRepository getRepo() {
    return repo;
  }

  public Findable<Block> getBlock(Location location) {
    return blockIds.getOrDefault(location, Findable.NULL_BLOCK);
  }

  public Findable<Block> getBlock(World world, int x, int y, int z) {
    return getBlock(new Location(world, x, y, z));
  }

  public Findable<Entity> getEntity(Entity entity) {
    return entityIds.getOrDefault(entity.getUniqueId(), Findable.NULL_ENTITY);
  }

  public Findable<Block> addBlock(int collectionId, Block block) {
    FindableDbo findable = repo.addBlock(collectionId, block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    Findable<Block> findableBlock = Findable.ofBlock(findable.id(), collectionId, block);
    blockIds.put(block.getLocation(), findableBlock);
    return findableBlock;
  }

  public Findable<Entity> addEntity(int collectionId, Entity entity) {
    FindableDbo findable = repo.addEntity(collectionId, entity.getUniqueId());
    Findable<Entity> findableEntity = Findable.ofEntity(findable.id(), findable.collectionId(), entity);
    entityIds.put(entity.getUniqueId(), findableEntity);
    return findableEntity;
  }

  public void refresh() {
    blockIds.clear();
    entityIds.clear();
    repo.findAll().forEach(findable -> {
      if (findable.isBlock()) {
        World world = Bukkit.getWorld(findable.uuid());
        if (world == null) {
          FindMe.inst().getLogger().log(Level.WARNING, "Could not load block findable (id=%d) from database, unknown world ID: %s", new Object[] {findable.id(), findable.uuid()});
        } else {
          Location loc = new Location(world, findable.x(), findable.y(), findable.z());
          Findable<Block> block = Findable.ofBlock(findable.id(), findable.collectionId(), world.getBlockAt(loc));
          blockIds.put(loc, block);
        }
      } else {
        Entity entity = Bukkit.getEntity(findable.uuid());
        if (entity == null) {
          FindMe.inst().getLogger().log(Level.WARNING, "Could not load entity findable (id={}) from database, unknown entity ID: {}", new Object[] { findable.id(), findable.uuid() });
        } else {
          entityIds.put(findable.uuid(), Findable.ofEntity(findable.id(), findable.collectionId(), entity));
        }
      }
    });
  }

}
