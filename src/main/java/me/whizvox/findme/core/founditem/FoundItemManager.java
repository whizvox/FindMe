package me.whizvox.findme.core.founditem;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.repo.Page;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class FoundItemManager {

  private final FoundItemRepository repo;
  private final Set<Key> items;
  private final Map<UUID, Map<Integer, Count>> collectionCounts;
  private final Map<Key, Props> props;
  private final Set<Integer> anyoneCollected;

  public FoundItemManager(Connection conn) {
    repo = new FoundItemRepository(conn);
    repo.initialize();
    items = new HashSet<>();
    collectionCounts = new HashMap<>();
    props = new HashMap<>();
    anyoneCollected = new HashSet<>();
    refresh();
  }

  private void setFound0(UUID playerId, int collectionId, int findableId, LocalDateTime whenFound) {
    Key key = new Key(playerId, findableId);
    items.add(key);
    anyoneCollected.add(collectionId);
    props.put(key, new Props(collectionId, whenFound));
    collectionCounts.computeIfAbsent(playerId, uuid -> new HashMap<>())
        .computeIfAbsent(collectionId, id -> new Count()).increase();
  }

  public boolean hasBeenFound(Player player, int findableId) {
    return items.contains(Key.of(player, findableId));
  }

  public boolean hasBeenFound(UUID playerId, int findableId) {
    return items.contains(new Key(playerId, findableId));
  }

  public boolean hasAnyoneCollected(int collectionId) {
    return anyoneCollected.contains(collectionId);
  }

  public int getFindCount(Player player, int collectionId) {
    return collectionCounts.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(collectionId, Count.ZERO).count;
  }

  public Map<Integer, Integer> countPlayerItems(UUID player) {
    return repo.countPlayerItems(player);
  }

  /**
   * Search through all found item entries.
   * @param sender The sender of whoever is searching through this database
   * @param args Query arguments. These include:
   *             <ul>
   *               <li><code>player</code> ({@link UUID}): the ID of a player</li>
   *               <li><code>collection</code> (int): the ID of a collection</li>
   *               <li><code>findable</code> (int): the ID of a findable</li>
   *               <li><code>whenFound</code> ({@link LocalDateTime}): the basis of searching when a findable was found</li>
   *               <li><code>whenFoundAfter</code> (boolean): whether to find entries after <code>whenFound</code></li>
   *               <li><code>whenFoundBefore</code> (boolean): whether to find entries before <code>whenFound</code></li>
   *               <li><code>sort</code> (String): one of <code>when</code> or <code>id</code> (default)</li>
   *               <li><code>asc</code>|<code>desc</code> (boolean): whether to sort in ascending or descending order</li>
   *               <li><code>page</code> (int): the page</li>
   *               <li><code>limit</code> (int): how many items to return</li>
   *             </ul>
   * @return A page of the results
   */
  public Page<FoundItemDbo> search(CommandSender sender, Map<String, Object> args) {
    Predicate<FoundItemDbo> filter = item -> true;
    if (args.containsKey("player")) {
      UUID playerId = (UUID) args.get("player");
      filter = filter.and(item -> item.playerId().equals(playerId));
    }
    if (args.containsKey("collection")) {
      int collectionId = (int) args.get("collection");
      filter = filter.and(item -> item.collectionId() == collectionId);
    }
    if (args.containsKey("findable")) {
      int findableId = (int) args.get("findable");
      filter = filter.and(item -> item.findableId() == findableId);
    }
    if (args.containsKey("whenAfter")) {
      LocalDateTime whenFoundQuery = (LocalDateTime) args.get("whenAfter");
      filter = filter.and(item -> item.whenFound().isAfter(whenFoundQuery));
    } else if (args.containsKey("whenBefore")) {
      LocalDateTime whenFoundQuery = (LocalDateTime) args.get("whenBefore");
      filter = filter.and(item -> item.whenFound().isBefore(whenFoundQuery));
    }
    if (args.containsKey("world")) {
      UUID worldId = (UUID) args.get("world");
      filter = filter.and(item -> {
        Findable<?> findable = FindMe.inst().getFindables().get(item.findableId());
        if (findable.isEmpty()) {
          return false;
        }
        Location loc = FMUtils.getLocation(findable);
        if (loc.getWorld() == null) {
          return false;
        }
        return worldId.equals(loc.getWorld().getUID());
      });
    }
    boolean asc = false;
    Comparator<FoundItemDbo> sort;
    if (args.containsKey("sort")) {
      String sortAlgo = (String) args.get("sort");
      sort = switch (sortAlgo) {
        case "when" -> {
          asc = true;
          yield Comparator.comparing(FoundItemDbo::whenFound);
        }
        case "id" -> Comparator.comparing(FoundItemDbo::findableId);
        default -> InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INVALID_ENUM, List.of("closest", "when", "id")));
      };
    } else {
      sort = Comparator.comparing(FoundItemDbo::whenFound);
    }
    if (args.containsKey("order")) {
      String order = (String) args.get("order");
      asc = switch (order) {
        case "asc" -> true;
        case "desc" -> false;
        default -> throw new IllegalArgumentException("Unknown order: " + order);
      };
    }
    int page;
    if (args.containsKey("page")) {
      page = (int) args.get("page");
    } else {
      page = 1;
    }
    int limit;
    if (args.containsKey("limit")) {
      limit = (int) args.get("limit");
    } else {
      limit = 10;
    }
    Predicate<FoundItemDbo> finalFilter = filter;
    AtomicInteger countRef = new AtomicInteger(0);
    List<FoundItemDbo> items = props.entrySet().stream()
        .map(entry -> new FoundItemDbo(entry.getKey().playerId(), entry.getValue().collectionId, entry.getKey().findableId, entry.getValue().whenFound()))
        .filter(item -> {
          if (finalFilter.test(item)) {
            countRef.addAndGet(1);
            return true;
          }
          return false;
        })
        .sorted(asc ? sort : sort.reversed())
        .skip((page - 1) * limit)
        .limit(limit)
        .toList();
    int count = countRef.get();
    return new Page<>(page, (int) Math.ceil((double) count / 10), count, items);
  }

  public void setFound(Player player, Findable<?> findable) {
    repo.insert(player.getUniqueId(), findable.collectionId(), findable.id());
    setFound0(player.getUniqueId(), findable.collectionId(), findable.id(), LocalDateTime.now());
  }

  public void removePlayer(UUID playerId) {
    repo.deleteByPlayer(playerId);
    refresh();
  }

  public void removeCollection(int collectionId) {
    repo.deleteByCollection(collectionId);
    refresh();
  }

  public void removeFindable(int findableId) {
    repo.deleteByFindable(findableId);
    refresh();
  }

  public void removePlayerCollection(UUID playerId, int collectionId) {
    repo.deleteByPlayerCollection(playerId, collectionId);
    refresh();
  }

  public void removePlayerFindable(UUID playerId, int findableId) {
    repo.deleteOne(playerId, findableId);
    refresh();
  }

  public void refresh() {
    items.clear();
    collectionCounts.clear();
    anyoneCollected.clear();
    props.clear();
    repo.findAll().forEach(item -> {
      setFound0(item.playerId(), item.collectionId(), item.findableId(), item.whenFound());
    });
  }

  public record Key(UUID playerId, int findableId) {

    public static Key of(Player player, int findableId) {
      return new Key(player.getUniqueId(), findableId);
    }

  }

  public static class Count {
    public int count;

    public Count() {
      count = 0;
    }

    public void increase() {
      count++;
    }

    public void reset() {
      count = 0;
    }

    public static final Count ZERO = new Count();
  }

  public record Props(int collectionId, LocalDateTime whenFound) {
  }

}
