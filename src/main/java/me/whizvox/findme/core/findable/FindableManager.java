package me.whizvox.findme.core.findable;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMConfig;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import me.whizvox.findme.repo.Page;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.util.*;
import java.util.function.Predicate;
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

  /**
   * Search through all findables. This is quite an expensive operation, so this should only be reserved for admin
   * usage.
   * @param sender The sender requesting the items. If this is a {@link Player}, then the resulting findables will be
   *               sorted by how far away from the player they are. If not (most likely a console), then the resulting
   *               findables will be sorted by their ID.
   * @param args All arguments to be considered when filtering. These can include:
   *             <ul>
   *               <li><code>collection</code> (int): the ID of the collection of the findable</li>
   *               <li><code>type</code> (String): whether the findable is a <code>block</code> or <code>entity</code></li>
   *               <li><code>entityId</code> ({@link UUID}): the UUID of the findable if it's an entity</li>
   *               <li><code>world</code> ({@link UUID}): the UUID of the world the findable is in</li>
   *               <li><code>radius</code> (double): the maximum distance from the player the findable can be. If the
   *               sender is not a player, this is ignored.</li>
   *               <li><code>valid</code> (boolean): if the findable is "valid". for blocks, a findable is valid if its
   *               world is loaded. for entities, a findable is valid if it's still in the world.</li>
   *               <li><code>page</code> (int): the requested page of results, starting from 1. default: 1</li>
   *               <li><code>limit</code> (int): the number of items to return. default: 10</li>
   *             </ul>
   *             All arguments are AND'd with each other, so all conditions must be true for a findable to appear in
   *             the search results.
   * @return A page of the results
   */
  public Page<Findable<?>> search(CommandSender sender, Map<String, Object> args) {
    Predicate<Findable<?>> filter = findable -> true;
    if (args.containsKey("collection")) {
      int collection = (int) args.get("collection");
      filter = filter.and(findable -> findable.collectionId() == collection);
    }
    if (args.containsKey("type")) {
      String type = (String) args.get("type");
      if (type.equals("block")) {
        filter = filter.and(findable -> (findable.type() == FindableType.BLOCK));
      } else if (type.equals("entity")) {
        filter = filter.and(findable -> (findable.type() == FindableType.ENTITY));
      }
    }
    if (args.containsKey("world")) {
      UUID worldId = (UUID) args.get("world");
      filter = filter.and(findable -> {
        if (findable.type() == FindableType.BLOCK) {
          return ((Block) findable.object()).getLocation().getWorld().getUID().equals(worldId);
        } else {
          return ((Entity) findable.object()).getLocation().getWorld().getUID().equals(worldId);
        }
      });
    }
    if (args.containsKey("radius") && sender instanceof Player player) {
      double radius = (double) args.get("radius");
      double radiusSq = radius * radius;
      Location center = player.getLocation();
      filter = filter.and(findable -> {
        if (findable.type() == FindableType.BLOCK) {
          return ((Block) findable.object()).getLocation().clone().add(0.5, 0.5, 0.5).distanceSquared(center) <= radiusSq;
        } else {
          return ((Entity) findable.object()).getLocation().distanceSquared(center) <= radiusSq;
        }
      });
    }
    if (args.containsKey("valid")) {
      boolean valid = (boolean) args.get("valid");
      Predicate<Findable<?>> validFilter = findable -> {
        if (findable.type() == FindableType.BLOCK) {
          return ((Block) findable.object()).getLocation().isWorldLoaded();
        } else {
          return ((Entity) findable.object()).isValid();
        }
      };
      filter = filter.and(valid ? validFilter : validFilter.negate());
    }
    int page = 1;
    if (args.containsKey("page")) {
      page = (int) args.get("page");
    }
    int limit = 10;
    if (args.containsKey("limit")) {
      limit = (int) args.get("limit");
    }
    Comparator<Findable<?>> sort;
    if (sender instanceof Player player) {
      Location center = player.getLocation();
      sort = (o1, o2) -> {
        Location loc1 = o1.type() == FindableType.BLOCK ? ((Block) o1.object()).getLocation().clone().add(0.5, 0.5, 0.5) : ((Entity) o1.object()).getLocation();
        Location loc2 = o2.type() == FindableType.BLOCK ? ((Block) o2.object()).getLocation().clone().add(0.5, 0.5, 0.5) : ((Entity) o2.object()).getLocation();
        return Double.compare(loc1.distanceSquared(center), loc2.distanceSquared(center));
      };
    } else {
      sort = Comparator.comparingInt(Findable::id);
    }
    long count = byId.values().parallelStream()
        .filter(filter)
        .count();
    List<Findable<?>> items = byId.values().parallelStream()
        .filter(filter)
        .sorted(sort)
        .skip((page - 1) * limit)
        .limit(limit)
        .toList();
    return new Page<>(page, (int) Math.ceil((double) count / limit), (int) count, items);
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
    if (FMConfig.INST.shouldImmobilizeEntities()) {
      if (entity instanceof LivingEntity livEntity) {
        livEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, Short.MAX_VALUE, true, false));
        FindMe.inst().getLogger().info("Immobilized findable entity: " + entity.getUniqueId() + " (" + entity.getType() + ")");
      }
    }
    return findableEntity;
  }

  @Nullable
  public Findable<?> getNearest(Location to) {
    Location nearestBlock = blocks.keySet().stream()
        .filter(loc -> loc.getWorld() != null && loc.getWorld() == to.getWorld())
        .min(Comparator.comparingDouble(to::distanceSquared))
        .orElse(null);
    Findable<Entity> nearestEntity = entities.values().stream()
        .filter(f -> f.object().getLocation().getWorld() != null && f.object().getLocation().getWorld() == to.getWorld())
        .min(Comparator.comparingDouble(findable -> to.distanceSquared(findable.object().getLocation())))
        .orElse(null);
    if (nearestBlock == null) {
      return nearestEntity;
    } else if (nearestEntity == null) {
      return blocks.get(nearestBlock);
    }
    if (to.distanceSquared(nearestBlock) < to.distanceSquared(nearestEntity.object().getLocation())) {
      return blocks.get(nearestBlock);
    }
    return nearestEntity;
  }

  private void attemptRemobilize(Findable<?> findable) {
    if (!findable.isEmpty() && findable.type() == FindableType.ENTITY) {
      Entity entity = (Entity) findable.object();
      if (entity instanceof LivingEntity livEntity) {
        boolean isImmobilized = livEntity.getActivePotionEffects().stream()
            .anyMatch(effect -> effect.isInfinite() && effect.getType() == PotionEffectType.SLOW);
        if (isImmobilized) {
          livEntity.removePotionEffect(PotionEffectType.SLOW);
          FindMe.inst().getLogger().info("Removed immobilization from previous findable entity: " + entity.getUniqueId() + " (" + entity.getType() + ")");
        }
      }
    }
  }

  public void remove(int id) {
    repo.deleteById(id);
    attemptRemobilize(byId.getOrDefault(id, Findable.NULL_ENTITY));
    refresh();
  }

  public void removeByCollection(int collectionId) {
    repo.deleteByCollection(collectionId);
    byId.values().forEach(findable -> {
      if (findable.collectionId() == collectionId) {
        attemptRemobilize(findable);
      }
    });
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
