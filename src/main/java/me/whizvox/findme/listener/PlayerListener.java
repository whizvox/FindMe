package me.whizvox.findme.listener;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerListener implements Listener {

  private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

  private static boolean attemptFind(Player player, Supplier<Findable<?>> supplier) {
    long now = System.currentTimeMillis();
    // spam prevention, can only click at most once per second
    if (now - COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) < 1000) {
      return false;
    }
    COOLDOWNS.put(player.getUniqueId(), now);
    Findable<?> findable = supplier.get();
    if (!findable.isEmpty()) {
      if (FindMe.inst().getFoundItems().hasBeenFound(player, findable.id())) {
        ChatMessage.sendTranslated(player, FMStrings.ERR_ALREADY_FOUND);
        return false;
      } else {
        return FindMe.inst().getCollections().getCollection(findable.collectionId()).map(collection -> {
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
          return FindMe.inst().getConfig().getBoolean("cancelEventOnFind", false);
        }).orElseGet(() -> {
          ChatMessage.sendTranslated(player, FMStrings.ERR_UNKNOWN_COLLECTION, findable.collectionId());
          return false;
        });
      }
    }
    return false;
  }

  @EventHandler
  public void onPlayerClick(PlayerInteractEvent event) {
    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    if (attemptFind(event.getPlayer(), () -> FindMe.inst().getFindables().getBlock(block.getLocation()))) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityInteract(PlayerInteractEntityEvent event) {
    if (attemptFind(event.getPlayer(), () -> FindMe.inst().getFindables().getEntity(event.getRightClicked()))) {
      event.setCancelled(true);
    }
  }

}
