package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import me.whizvox.findme.repo.Page;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.list",
      TLK_DESCRIPTION = "list.description",
      TLK_BAD_RADIUS = "list.badRadius",
      TLK_EMPTY = "list.empty",
      TLK_HEADER = "list.header",
      TLK_BLOCK = "list.block",
      TLK_ENTITY = "list.entity",
      TLK_UNKNOWN_BLOCK = "list.unknownBlock",
      TLK_UNKNOWN_ENTITY = "list.unknownEntity";

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
    return "[c|b|e|i|w|r|v|p:<value>...]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() > 1) {
      String lastArg = context.arg(context.argCount() - 1);
      if (lastArg.length() < 2) {
        return SuggestionHelper.fromStream(Arrays.stream(new String[] {"c:", "t:", "w:", "r:", "v:", "p:"}), lastArg);
      }
      if (lastArg.startsWith("c:")) {
        return SuggestionHelper.fromStream(FindMe.inst().getCollections().stream().map(col -> "c:" + col.name), lastArg);
      }
      if (lastArg.startsWith("t:")) {
        return SuggestionHelper.fromStream(Arrays.stream(new String[] {"t:block", "t:entity"}), lastArg);
      }
      if (lastArg.startsWith("w:")) {
        return SuggestionHelper.fromStream(Bukkit.getWorlds().stream().map(world -> "w:" + world.getName()), lastArg);
      }
      if (lastArg.startsWith("v:")) {
        return SuggestionHelper.fromStream(Arrays.stream(new String[]{"v:false", "v:true"}), lastArg);
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Map<String, Object> args = new HashMap<>();
    for (int i = 1; i < context.argCount(); i++) {
      String arg = context.arg(i);
      if (arg.startsWith("c:")) {
        String colName = arg.substring(2);
        FindableCollection collection = FindMe.inst().getCollections().getCollection(colName)
            .orElseGet(() -> InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_UNKNOWN_COLLECTION, colName)));
        args.put("collection", collection.id);
      } else if (arg.startsWith("t:")) {
        String type = arg.substring(2);
        if (type.equals("block") || type.equals("entity")) {
          args.put("type", type);
        } else {
          InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INVALID_ENUM, String.join(", ", "block", "entity")));
        }
      } else if (arg.startsWith("w")) {
        String worldName = arg.substring(2);
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
          InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_UNKNOWN_WORLD, worldName));
        } else {
          args.put("world", world.getUID());
        }
      } else if (arg.startsWith("r:")) {
        String radiusStr = arg.substring(2);
        try {
          double radius = Double.parseDouble(radiusStr);
          if (radius < 0) {
            InterruptCommandException.halt(ChatMessage.translated(TLK_BAD_RADIUS, radiusStr));
          } else {
            args.put("radius", radius);
          }
        } catch (NumberFormatException e) {
          InterruptCommandException.halt(ChatMessage.translated(TLK_BAD_RADIUS, radiusStr));
        }
      } else if (arg.startsWith("v:")) {
        boolean isValid = MetaArgumentHelper.parseBoolean(arg.substring(2));
        args.put("valid", isValid);
      } else if (arg.startsWith("p:")) {
        String pageStr = arg.substring(2);
        try {
          int page = Integer.parseInt(pageStr);
          if (page <= 0) {
            InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INT_OUT_OF_RANGE, pageStr, 1, Integer.MAX_VALUE));
          } else {
            args.put("page", page);
          }
        } catch (NumberFormatException e) {
          InterruptCommandException.halt(ChatMessage.translated(FMStrings.ERR_INVALID_INT, pageStr));
        }
      }
    }
    Page<Findable<?>> page = FindMe.inst().getFindables().search(context.sender(), args);
    if (page.totalItems() == 0) {
      context.sendTranslated(TLK_EMPTY);
      return;
    }
    ChatMessages msg = new ChatMessages();
    msg.addTranslated(TLK_HEADER, page.page(), page.totalPages());
    page.items().forEach(findable -> {
      String collectionName = FindMe.inst().getCollections().getCollection(findable.collectionId()).map(col -> col.name).orElse(ChatColor.RED + "???");
      if (findable.type() == FindableType.BLOCK) {
        Block block = ((Block) findable.object());
        if (block.getLocation().isWorldLoaded()) {
          msg.addTranslated(TLK_BLOCK, findable.id(), collectionName, block.getBlockData().getMaterial(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        } else {
          msg.addTranslated(TLK_UNKNOWN_BLOCK, findable.id(), collectionName, block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
      } else {
        Entity entity = (Entity) findable.object();
        if (entity.isValid()) {
          Location loc = entity.getLocation();
          msg.addTranslated(TLK_ENTITY, findable.id(), collectionName, entity.getType(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
        } else {
          msg.addTranslated(TLK_UNKNOWN_ENTITY, findable.id(), collectionName, entity.getUniqueId());
        }
      }
    });
    context.sendMessage(msg);
  }

}
