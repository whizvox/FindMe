package me.whizvox.findme.util;

import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class FMUtils {

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
