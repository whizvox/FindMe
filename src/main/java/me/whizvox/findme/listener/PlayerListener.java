package me.whizvox.findme.listener;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.core.FMConfig;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.Bukkit;
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
          int count = FindMe.inst().getFoundItems().getFindCount(player, collection.id);
          int total = FindMe.inst().getFindables().getCount(collection.id);
          Map<String, Object> args = Map.of(
              "p", player.getDisplayName(),
              "d", collection.displayName,
              "n", collection.name,
              "c", count,
              "t", total,
              "e", String.format("%.1f", (float) count / total)
          );
          if (count == 1) {
            if (!collection.findFirstMsg.isBlank()) {
              player.sendMessage(FMUtils.format(collection.findFirstMsg, args));
            }
            if (collection.findFirstSound != null) {
              player.playSound(player, collection.findFirstSound, 1, 1);
            }
          } else if (count >= FindMe.inst().getFindables().getCount(collection.id)) {
            if (!collection.completeMsg.isBlank()) {
              player.sendMessage(FMUtils.format(collection.completeMsg, args));
            }
            if (collection.completeSound != null) {
              player.playSound(player, collection.completeSound, 1, 1);
            }
            if (!collection.completeBroadcastMsg.isBlank()) {
              Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.getUniqueId().equals(player.getUniqueId())) {
                  p.sendMessage(FMUtils.format(collection.completeBroadcastMsg, args));
                  if (collection.completeBroadcastSound != null) {
                    p.playSound(player, collection.completeBroadcastSound, 1, 1);
                  }
                }
              });
            }
          } else {
            if (!collection.findMsg.isBlank()) {
              player.sendMessage(FMUtils.format(collection.findMsg, args));
            }
            if (collection.findSound != null) {
              player.playSound(player, collection.findSound, 1, 1);
            }
          }
          return FMConfig.INST.shouldCancelEventOnFind();
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
