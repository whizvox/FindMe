package me.whizvox.findme.listener;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FindMePlayerListener implements Listener {

  public static final String
      TLK_UNKNOWN_COLLECTION = "error.unknownCollection",
      TLK_ALREADY_FOUND = "error.alreadyFound";

  private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

  @EventHandler
  public void onPlayerClick(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    long now = System.currentTimeMillis();
    // spam prevention, can only click at most once per second
    if (now - COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) < 1000) {
      return;
    }
    COOLDOWNS.put(player.getUniqueId(), now);
    Location location = event.getClickedBlock().getLocation();
    Findable<Block> findable = FindMe.inst().getFindables().getBlock(location);
    if (!findable.isEmpty()) {
      if (FindMe.inst().getFoundItems().hasBeenFound(player, findable.id())) {
        player.sendMessage(FindMe.inst().translate(TLK_ALREADY_FOUND));
      } else {
        FindMe.inst().getCollections().getCollection(findable.collectionId()).ifPresentOrElse(collection -> {
          // TODO More advanced messaging
          FindMe.inst().getFoundItems().setFound(player, findable);
          Map<String, Object> args = Map.of(
              "p", player.getDisplayName(),
              "d", collection.displayName,
              "n", collection.name,
              "c", FindMe.inst().getFoundItems().getFindCount(player, collection.id),
              "t", FindMe.inst().getFindables().getCount(collection.id)
          );
          player.sendMessage(FMUtils.format(collection.findMsg, args));
          player.playSound(player, collection.findSound, 1, 1);
        }, () -> {
          player.sendMessage(FindMe.inst().translate(TLK_UNKNOWN_COLLECTION, findable.collectionId()));
        });
      }
    }
  }

}
