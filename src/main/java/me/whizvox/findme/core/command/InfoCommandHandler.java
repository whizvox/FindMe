package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.List;
import java.util.stream.Stream;

public class InfoCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.info",
      TLK_DESCRIPTION = "info.description",
      TLK_BLOCK = "info.block",
      TLK_ENTITY = "info.entity";

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
    return "[<findableId>|block|entity (<x> <y> <z> [<world>])|<entityId>]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 2) {
      return SuggestionHelper.fromStream(Stream.of("block", "entity"), context.arg(1));
    }
    if (context.argCount() > 2 && context.arg(1).equals("block") && context.argCount() == 6) {
      return SuggestionHelper.worlds(context.arg(5));
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Findable<?> findable;
    if (context.argCount() < 2) {
      Player player = context.getPlayer();
      RayTraceResult hit = FMUtils.getLookingAt(player);
      if (hit == null) {
        context.sendTranslated(FMStrings.ERR_NOT_LOOKING);
        return;
      }
      if (hit.getHitEntity() != null) {
        findable = FindMe.inst().getFindables().getEntity(hit.getHitEntity());
        if (findable.isEmpty()) {
          context.sendTranslated(FMStrings.ERR_ENTITY_NOT_FINDABLE);
          return;
        }
      } else {
        //noinspection DataFlowIssue
        findable = FindMe.inst().getFindables().getBlock(hit.getHitBlock().getLocation());
        if (findable.isEmpty()) {
          context.sendTranslated(FMStrings.ERR_BLOCK_NOT_FINDABLE);
          return;
        }
      }
    } else {
      String arg1 = context.arg(1);
      if (arg1.equals("block")) {
        Location loc = ArgumentHelper.getBlockLocation(context, 2, false);
        findable = FindMe.inst().getFindables().getBlock(loc);
        if (findable.isEmpty()) {
          context.sendTranslated(FMStrings.ERR_BLOCK_NOT_FINDABLE);
          return;
        }
      } else if (arg1.equals("entity")) {
        Entity entity = ArgumentHelper.getEntity(context, 2, false);
        findable = FindMe.inst().getFindables().getEntity(entity);
        if (findable.isEmpty()) {
          context.sendTranslated(FMStrings.ERR_ENTITY_NOT_FINDABLE);
          return;
        }
      } else {
        findable = ArgumentHelper.getFindable(context, 1, true);
      }
    }
    String collectionName = FindMe.inst().getCollections().getCollection(findable.collectionId())
        .map(col -> col.name)
        .orElse(ChatColor.RED + "???");
    if (findable.type() == FindableType.BLOCK) {
      Location loc = ((Block) findable.object()).getLocation();
      String worldName;
      if (loc.getWorld() == null) {
        worldName = ChatColor.RED + "???";
      } else {
        worldName = loc.getWorld().getName();
      }
      context.sendTranslated(TLK_BLOCK, findable.id(), collectionName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), worldName);
    } else {
      Entity entity = (Entity) findable.object();
      Location loc = entity.getLocation();
      String worldName;
      if (loc.getWorld() == null) {
        worldName = ChatColor.RED + "???";
      } else {
        worldName = loc.getWorld().getName();
      }
      context.sendTranslated(TLK_ENTITY, findable.id(), collectionName, loc.getX(), loc.getY(), loc.getZ(), worldName, entity.getUniqueId());
    }
  }

}
