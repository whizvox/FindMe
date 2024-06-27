package me.whizvox.findme.core.founditem;

import me.whizvox.findme.findable.Findable;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.*;

public class FoundItemManager {

  private final FoundItemRepository repo;
  private final Map<UUID, Set<Integer>> foundItems;
  private final Map<UUID, Map<Integer, Integer>> foundItemCounts;
  private final Set<Integer> anyoneCollected;

  public FoundItemManager(Connection conn) {
    repo = new FoundItemRepository(conn);
    repo.initialize();
    foundItems = new HashMap<>();
    foundItemCounts = new HashMap<>();
    anyoneCollected = new HashSet<>();
    refresh();
  }

  private void setFound0(UUID playerId, int collectionId, int findableId) {
    foundItems.computeIfAbsent(playerId, uuid -> new HashSet<>()).add(findableId);
    anyoneCollected.add(collectionId);
    Map<Integer, Integer> counts = foundItemCounts.computeIfAbsent(playerId, uuid -> new HashMap<>());
    counts.put(collectionId, counts.getOrDefault(collectionId, 0) + 1);
  }

  public boolean hasBeenFound(Player player, int findableId) {
    return foundItems.getOrDefault(player.getUniqueId(), Set.of()).contains(findableId);
  }

  public boolean hasAnyoneCollected(int collectionId) {
    return anyoneCollected.contains(collectionId);
  }

  public int getFindCount(Player player, int collectionId) {
    return foundItemCounts.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(collectionId, 0);
  }

  public void setFound(Player player, Findable<?> findable) {
    repo.insert(new FoundItemDbo(player.getUniqueId(), findable.collectionId(), findable.id()));
    setFound0(player.getUniqueId(), findable.collectionId(), findable.id());
  }

  public void removePlayer(Player player) {
    repo.deleteByPlayer(player.getUniqueId());
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

  public void refresh() {
    foundItems.clear();
    foundItemCounts.clear();
    anyoneCollected.clear();
    repo.findAll().forEach(item -> {
      setFound0(item.playerId(), item.collectionId(), item.findableId());
    });
  }

}
