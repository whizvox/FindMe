package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import me.whizvox.findme.repo.Page;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
    return "[collection|type|world|radius|valid|page|sort|found|notfound|order|sort:<value>...]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() > 1) {
      String arg = context.arg(context.argCount() - 1);
      int index = arg.indexOf(':');
      if (index == -1) {
        return SuggestionHelper.fromStream(Arrays.stream(new String[] {"collection:", "type:", "world:", "radius:", "valid:", "page:", "sort:", "found:", "notfound:", "order:"}), arg);
      } else {
        String selector = arg.substring(0, index);
        return switch (selector) {
          case "collection", "c" -> SuggestionHelper.fromStream(FindMe.inst().getCollections().stream().map(c -> selector + ":" + c.name), arg);
          case "type", "t" -> SuggestionHelper.fromStream(Arrays.stream(new String[] {selector + ":block", selector + ":entity"}), arg);
          case "world", "w" -> SuggestionHelper.fromStream(Bukkit.getWorlds().stream().map(world -> selector + ":" + world.getName()), arg);
          case "valid", "v" -> SuggestionHelper.fromStream(Arrays.stream(new String[] {selector + ":false", selector + ":true"}), arg);
          case "sort", "s" -> SuggestionHelper.fromStream(Arrays.stream(new String[] {selector + ":asc", selector + ":desc"}), arg);
          case "found", "f", "notfound", "n" -> SuggestionHelper.fromStream(Arrays.stream(Bukkit.getOfflinePlayers()).map(p -> selector + ":" + p.getName()), arg);
          case "order", "o" -> SuggestionHelper.fromStream(Arrays.stream(new String[] {selector + ":id", selector + ":nearest"}), arg);
          default -> super.listSuggestions(context);
        };
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Map<String, Object> args = new HashMap<>();
    for (int i = 1; i < context.argCount(); i++) {
      String arg = context.arg(i);
      int indexOf = arg.indexOf(':');
      if (indexOf < 0) {
        InterruptCommandException.showUsage();
      }
      String selector = arg.substring(0, indexOf);
      String value = arg.substring(indexOf + 1);
      switch (selector) {
        case "c", "collection" -> args.put("collection", MetaArgumentHelper.parseCollection(value));
        case "t", "type" -> args.put("type", MetaArgumentHelper.checkEnum(value, List.of("block", "entity")));
        case "w", "world" -> args.put("world", MetaArgumentHelper.parseWorld(value));
        case "r", "radius" -> args.put("radius", MetaArgumentHelper.parseInt(value, 1));
        case "v", "valid" -> args.put("valid", MetaArgumentHelper.parseBoolean(value));
        case "p", "page" -> args.put("page", MetaArgumentHelper.parseInt(value, 1));
        case "o", "order" -> args.put("order", MetaArgumentHelper.checkEnum(value, List.of("id", "nearest")));
        case "s", "sort" -> args.put("sort", MetaArgumentHelper.checkEnum(value, List.of("asc", "desc")));
        case "f", "found" -> args.put("found", MetaArgumentHelper.parseOfflinePlayer(value));
        case "n", "notfound" -> args.put("notFound", MetaArgumentHelper.parseOfflinePlayer(value));
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
