package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TeleportCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.teleport",
      TLK_DESCRIPTION = "teleport.description",
      TLK_NONE_NEAR = "teleport.noneNear",
      TLK_NOT_SAFE = "teleport.notSafe",
      TLK_SUCCESS = "teleport.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION);
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "nearest|<findableId> [force]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 2) {
      if ("nearest".startsWith(context.arg(1).toLowerCase())) {
        return List.of("nearest");
      }
    } else if (context.argCount() == 3) {
      if ("force".startsWith(context.arg(2).toLowerCase())) {
        return List.of("force");
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    if (context.argCount() < 2) {
      InterruptCommandException.showUsage();
    }
    Player player = context.getPlayer();
    Findable<?> findable;
    if (context.arg(1).equalsIgnoreCase("nearest")) {
      findable = FindMe.inst().getFindables().getNearest(context.getPlayer().getLocation());
      if (findable == null) {
        context.sendTranslated(TLK_NONE_NEAR);
        return;
      }
    } else {
      findable = ArgumentHelper.getFindable(context, 1, false);
    }
    boolean forceIfUnsafe = ArgumentHelper.getEnum(context, 2, () -> "", List.of("force", "")).equals("force");
    Location loc = findable.type() == FindableType.BLOCK ?
        ((Block) findable.object()).getLocation() :
        ((Entity) findable.object()).getLocation();
    Location tpLoc = null;
    double nearestDist = 99999;
    for (int xOff = -2; xOff <= 2; xOff++) {
      for (int yOff = -2; yOff <= 2; yOff++) {
        for (int zOff = -2; zOff <= 2; zOff++) {
          Location pLoc = new Location(loc.getWorld(), xOff + loc.getBlockX() + 0.5, yOff + loc.getBlockY(), zOff + loc.getBlockZ() + 0.5);
          double dist = pLoc.distanceSquared(loc);
          // check pLoc is not solid, 1 above pLoc is not solid, 1 below pLoc is solid, and this is the closest point
          if (!pLoc.getBlock().getBlockData().getMaterial().isSolid() &&
              !pLoc.clone().add(0, 1, 0).getBlock().getBlockData().getMaterial().isSolid() &&
              pLoc.clone().add(0, -1, 0).getBlock().getBlockData().getMaterial().isSolid() &&
              dist < nearestDist) {
            tpLoc = pLoc;
            nearestDist = dist;
          }
        }
      }
    }
    if (tpLoc == null && !forceIfUnsafe) {
      context.sendTranslated(TLK_NOT_SAFE, context.label(), context.localLabel(), context.arg(1));
      return;
    }
    if (tpLoc == null) {
      tpLoc = loc.clone().add(0.5, 1, 0.5);
    }
    player.teleport(tpLoc.setDirection(player.getLocation().getDirection()), PlayerTeleportEvent.TeleportCause.COMMAND);
    String collectionName = FindMe.inst().getCollections().getCollection(findable.collectionId())
        .map(col -> col.displayName)
        .orElse(ChatColor.RED + "???");
    context.sendTranslated(TLK_SUCCESS, findable.id(), collectionName);
  }

}
