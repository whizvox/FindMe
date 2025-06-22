package me.whizvox.findme.core.findable;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMConfig;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import me.whizvox.findme.repo.Page;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

  private Findable<?> validateCacheEntry(Findable<?> findable) {
    if (!FMConfig.INST.shouldCheckCaches()) {
      return findable;
    }
    int id = findable.id();
    return repo.findById(findable.id())
        .map(dbo -> {
          if (!findable.isEmpty()) {
            if ((findable.type() == FindableType.BLOCK) == dbo.isBlock()) {
              if (findable.collectionId() == dbo.collectionId()) {
                if (findable.type() == FindableType.BLOCK) {
                  Location loc = ((Block) findable.object()).getLocation();
                  if (loc.getBlockX() == dbo.x() && loc.getBlockY() == dbo.y() && loc.getBlockZ() == dbo.z() &&
                      loc.getWorld() != null && loc.getWorld().getUID().equals(dbo.uuid())) {
                    return findable;
                  }
                  FindMe.inst().getLogger().info(
                      "Needed to refresh caches! Findable block (%d) is in wrong location. Found=%d,%d,%d,%s, Expected=%d,%d,%d,%s".formatted(
                          id, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                          loc.getWorld() == null ? "null" : loc.getWorld().getUID(), dbo.x(), dbo.y(), dbo.z(),
                          dbo.uuid())
                  );
                }
                if (((Entity) findable.object()).getUniqueId().equals(dbo.uuid())) {
                  return findable;
                }
                FindMe.inst().getLogger().info("Needed to refresh caches! Findable entity (%d) has mismatched UUIDs. Found=%s, Expected=%s".formatted(id, ((Entity) findable.object()).getUniqueId(), dbo.uuid()));
              } else {
                FindMe.inst().getLogger().info("Needed to refresh caches! Findable object (%d) is in wrong collection. Found=%d, Expected=%d".formatted(id, findable.collectionId(), dbo.collectionId()));
              }
            } else {
              FindMe.inst().getLogger().info("Needed to refresh caches! Findable object (%d) has mismatched type. Found=%s, Expected=%s".formatted(id, findable.type(), dbo.isBlock() ? "BLOCK" : "ENTITY"));
            }
          } else {
            FindMe.inst().getLogger().info("Needed to refresh caches! Findable object (%d) found in database, but not found in cache".formatted(id));
          }
          refresh();
          return byId.getOrDefault(id, Findable.NULL_ENTITY);
        }).orElseGet(() -> {
          if (!findable.isEmpty()) {
            FindMe.inst().getLogger().info("Needed to refresh caches! Findable object (%d) found in cache, but not found in database!".formatted(id));
            refresh();
          }
          return Findable.NULL_ENTITY;
        });
  }

  public FindableRepository getRepo() {
    return repo;
  }

  public Findable<Block> getBlock(Location location) {
    //noinspection unchecked
    return (Findable<Block>) validateCacheEntry(blocks.getOrDefault(location, Findable.NULL_BLOCK));
  }

  public Findable<Block> getBlock(World world, int x, int y, int z) {
    return getBlock(new Location(world, x, y, z));
  }

  public Findable<Entity> getEntity(Entity entity) {
    //noinspection unchecked
    return (Findable<Entity>) validateCacheEntry(entities.getOrDefault(entity.getUniqueId(), Findable.NULL_ENTITY));
  }

  public Findable<?> get(int id) {
    return validateCacheEntry(byId.getOrDefault(id, Findable.NULL_ENTITY));
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
      World world = (World) args.get("world");
      filter = filter.and(findable -> {
        if (findable.type() == FindableType.BLOCK) {
          return ((Block) findable.object()).getLocation().getWorld().getUID().equals(world.getUID());
        } else {
          return ((Entity) findable.object()).getLocation().getWorld().getUID().equals(world.getUID());
        }
      });
    }
    if (args.containsKey("radius") && sender instanceof Player player) {
      int radius = (int) args.get("radius");
      int radiusSq = radius * radius;
      Location center = player.getLocation();
      filter = filter.and(findable -> {
        if (findable.type() == FindableType.BLOCK) {
          return ((Block) findable.object()).getLocation().clone().add(0.5, 0.5, 0.5).distanceSquared(center) <= radiusSq;
        } else {
          return ((Entity) findable.object()).getLocation().distanceSquared(center) <= radiusSq;
        }
      });
    }
    if (args.containsKey("found")) {
      OfflinePlayer player = (OfflinePlayer) args.get("found");
      filter = filter.and(findable -> FindMe.inst().getFoundItems().hasBeenFound(player.getUniqueId(), findable.id()));
    }
    if (args.containsKey("notFound")) {
      OfflinePlayer player = (OfflinePlayer) args.get("notFound");
      filter = filter.and(findable -> !FindMe.inst().getFoundItems().hasBeenFound(player.getUniqueId(), findable.id()));
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
    if (args.containsKey("order")) {
      String order = (String) args.get("order");
      if (order.equals("nearest") && sender instanceof Player player) {
        sort = FMUtils.compareDistance(player.getLocation(), findable -> findable);
      } else {
        sort = Comparator.comparingInt(Findable::id);
      }
    } else {
      if (sender instanceof Player player) {
        Location center = player.getLocation();
        sort = FMUtils.compareDistance(center, findable -> findable);
      } else {
        sort = Comparator.comparingInt(Findable::id);
      }
    }
    if (args.containsKey("sort")) {
      boolean asc = args.get("sort").equals("asc");
      if (asc) {
        sort = sort.reversed();
      }
    }
    Predicate<Findable<?>> finalFilter = filter;
    AtomicInteger countRef = new AtomicInteger(0);
    List<Findable<?>> items = byId.values().parallelStream()
        .filter(findable -> {
          if (finalFilter.test(findable)) {
            countRef.addAndGet(1);
            return true;
          }
          return false;
        })
        .sorted(sort)
        .skip((page - 1) * limit)
        .limit(limit)
        .toList();
    int count = countRef.get();
    return new Page<>(page, (int) Math.ceil((double) count / limit), count, items);
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
        livEntity.setCollidable(false);
        livEntity.setAI(false);
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
        boolean isImmobilized = !livEntity.hasAI() && !livEntity.isCollidable();
        if (isImmobilized) {
          livEntity.setAI(true);
          livEntity.setCollidable(true);
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
