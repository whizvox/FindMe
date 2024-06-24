package me.whizvox.findme.core.founditem;

import me.whizvox.findme.core.findable.FindableDbo;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.*;

public class FoundItemManager {

  private final FoundItemRepository repo;
  private final Map<UUID, Set<Integer>> foundItems;

  public FoundItemManager(Connection conn) {
    repo = new FoundItemRepository(conn);
    repo.initialize();
    foundItems = new HashMap<>();
    refresh();
  }

  private void setFound(UUID playerId, int findableId) {
    foundItems.computeIfAbsent(playerId, uuid -> new HashSet<>()).add(findableId);
  }

  public boolean hasBeenFound(Player player, int findableId) {
    return foundItems.getOrDefault(player.getUniqueId(), Set.of()).contains(findableId);
  }

  public void setFound(Player player, FindableDbo findable) {
    repo.insert(new FoundItemDbo(player.getUniqueId(), findable.collectionId(), findable.id()));
  }

  public void removePlayer(Player player) {
    repo.deleteByPlayer(player.getUniqueId());
  }

  public void removeCollection(int collectionId) {
    repo.deleteByCollection(collectionId);
  }

  public void removeFindable(int findableId) {
    repo.deleteByFindable(findableId);
  }

  public void refresh() {
    foundItems.clear();
    repo.findAll().forEach(item -> setFound(item.playerId(), item.findableId()));
  }

}
