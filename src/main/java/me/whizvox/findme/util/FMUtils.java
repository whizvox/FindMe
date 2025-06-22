package me.whizvox.findme.util;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.core.FMConfig;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.util.RayTraceResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FMUtils {

  private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

  /**
   * Attempt to find a findable object. Will handle cooldown-checking, sending messages to players, and updating caches
   * and databases if necessary.
   * @param player The player who is attempting to find something
   * @param supplier The supplier for the findable object
   * @param cancellable The event handling this call
   * @return <code>true</code> if an object was found, <code>false</code> otherwise
   */
  public static boolean attemptFind(Player player, Supplier<Findable<?>> supplier, @Nullable Cancellable cancellable) {
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
          if (cancellable != null && FMConfig.INST.shouldCancelEventOnFind()) {
            cancellable.setCancelled(true);
          }
          return true;
        }).orElseGet(() -> {
          ChatMessage.sendTranslated(player, FMStrings.ERR_UNKNOWN_COLLECTION, findable.collectionId());
          return false;
        });
      }
    }
    return false;
  }

  @Nullable
  public static Block getLookingAtBlock(Player player) {
    RayTraceResult hit = player.rayTraceBlocks(5.0);
    return hit != null ? hit.getHitBlock() : null;
  }

  @Nullable
  public static Entity getLookingAtEntity(Player player, Predicate<Entity> filter) {
    RayTraceResult hit = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), 5.0,
        entity -> entity != player && filter.test(entity)
    );
    return hit != null ? hit.getHitEntity() : null;
  }

  @Nullable
  public static RayTraceResult getLookingAt(Player player) {
    return getLookingAt(player, null);
  }

  @Nullable
  public static RayTraceResult getLookingAt(Player player, @Nullable Predicate<Entity> entityFilter) {
    Location pLoc = player.getEyeLocation();
    RayTraceResult bHit = player.rayTraceBlocks(5.0);
    RayTraceResult eHit = player.getWorld().rayTraceEntities(pLoc, pLoc.getDirection(), 5.0,
        entity -> entity != player && (entityFilter == null || entityFilter.test(entity))
    );
    if (bHit == null) {
      return eHit;
    } else if (eHit == null) {
      return bHit;
    }
    return bHit.getHitPosition().distanceSquared(pLoc.toVector()) <
        eHit.getHitPosition().distanceSquared(pLoc.toVector()) ? bHit : eHit;
  }

  public static String format(String format, Map<String, Object> args) {
    StringBuilder sb = new StringBuilder();
    final int n = format.length();
    boolean escape = false;
    char c;
    for (int i = 0; i < n; i++) {
      c = format.charAt(i);
      if (c == '\\' && !escape) {
        escape = true;
        continue;
      }
      if (c == '{') {
        if (escape) {
          sb.append('{');
        } else {
          int last = -1;
          for (int j = i + 1; j < n; j++) {
            c = format.charAt(j);
            if (c == '}') {
              last = j;
              break;
            }
          }
          if (last != -1) {
            String key = format.substring(i + 1, last);
            if (args.containsKey(key)) {
              sb.append(args.get(key));
              i = last;
            } else {
              sb.append('{');
            }
          } else {
            sb.append('{');
          }
        }
      } else {
        sb.append(c);
      }
      escape = false;
    }
    return sb.toString();
  }

  public static Location getLocation(Findable<?> findable) {
    if (findable.type() == FindableType.BLOCK) {
      return ((Block) findable.object()).getLocation().clone().add(0.5, 0.5, 0.5);
    }
    return ((Entity) findable.object()).getLocation();
  }

  public static double getDistanceSqr(Location from, Findable<?> findable) {
    Location to = getLocation(findable);
    if (!from.getWorld().getUID().equals(to.getWorld().getUID())) {
      return Double.MAX_VALUE;
    }
    return from.distanceSquared(to);
  }

  public static <T> Comparator<T> compareDistance(Location from, Function<T, Findable<?>> getFindable) {
    return Comparator.comparingDouble(obj -> getDistanceSqr(from, getFindable.apply(obj)));
  }

  public static <T> Comparator<T> reverse(Comparator<T> comparator, boolean reverse) {
    return reverse ? comparator.reversed() : comparator;
  }

  public static String formatDuration(Duration duration) {
    if (duration.isZero()) {
      return "0s";
    }
    StringBuilder sb = new StringBuilder();
    int sec = duration.toSecondsPart();
    int min = duration.toMinutesPart();
    int hrs = duration.toHoursPart();
    long day = duration.toDaysPart();
    if (day != 0) {
      sb.append(day).append('d');
    }
    if (hrs != 0 && day < 31) {
      sb.append(hrs).append('h');
    }
    if (min != 0 && day == 0) {
      sb.append(min).append('m');
    }
    if (sec != 0 && day == 0 && hrs == 0) {
      sb.append(sec).append('s');
    }
    return sb.toString();
  }

  public static String formatShortTimestamp(LocalDateTime ldt) {
    return DateTimeFormatter.ISO_DATE.format(ldt);
  }

  public static String formatDurationOrTimestamp(LocalDateTime ldt) {
    LocalDateTime now = LocalDateTime.now();
    String timestamp;
    if (ldt.isAfter(now.minusHours(48))) {
      // if less than 24 hours ago, display duration
      return formatDuration(Duration.between(ldt, now));
    }
    // otherwise, display plain timestamp
    return formatShortTimestamp(ldt);
  }

}
