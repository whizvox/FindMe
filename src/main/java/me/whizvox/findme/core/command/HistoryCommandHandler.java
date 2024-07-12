package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.founditem.FoundItemDbo;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.repo.Page;
import me.whizvox.findme.util.ChatUtils;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.history",
      TLK_DESCRIPTION = "history.description",
      TLK_HEADER = "history.header",
      TLK_ENTRY = "history.entry";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION);
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION);
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() > 1) {
      String arg = context.arg(context.argCount() - 1);
      int indexOf = arg.indexOf(':');
      if (indexOf == -1) {
        return SuggestionHelper.fromStream(Arrays.stream(new String[] {
            "player:", "collection:", "findable:", "whenAfter:", "whenBefore:", "sort:", "order:", "page:", "world:"}), arg);
      } else {
        String selector = arg.substring(0, indexOf);
        if (selector.equals("player") || selector.equals("r")) {
          return SuggestionHelper.fromStream(Arrays.stream(Bukkit.getOfflinePlayers()).map(p -> selector + ":" + p.getName()), arg);
        }
        if (selector.equals("collection") || selector.equals("c")) {
          return SuggestionHelper.fromStream(FindMe.inst().getCollections().stream().map(c -> selector + ":" + c.name), arg);
        }
        if (selector.equals("sort") || selector.equals("s")) {
          return SuggestionHelper.fromStream(Arrays.stream(new String[]{"id", "when"}).map(s -> selector + ":" + s), arg);
        }
        if (selector.equals("order") || selector.equals("o")) {
          return SuggestionHelper.fromStream(Arrays.stream(new String[]{"asc", "desc"}).map(s -> selector + ":" + s), arg);
        }
        if (selector.equals("world") || selector.equals("w")) {
          return SuggestionHelper.fromStream(Bukkit.getWorlds().stream().map(w -> selector + ":" + w.getName()), arg);
        }
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public String getUsageArguments() {
    return "[player|collection|findable|whenAfter|whenBefore|world|sort|order|page:<value>...]";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Map<String, Object> args = new HashMap<>();
    for (int i = 1; i < context.args().size(); i++) {
      String arg = context.arg(i);
      int indexOf = arg.indexOf(':');
      if (indexOf < 0) {
        InterruptCommandException.showUsage();
      }
      String selector = arg.substring(0, indexOf);
      String value = arg.substring(indexOf + 1);
      switch (selector) {
        case "player", "l" -> args.put("player", MetaArgumentHelper.parseOfflinePlayer(value).getUniqueId());
        case "collection", "c" -> args.put("collection", MetaArgumentHelper.parseCollection(value).id);
        case "findable", "f" -> args.put("findable", MetaArgumentHelper.parseFindable(value).id());
        case "whenAfter", "a" -> args.put("whenAfter", MetaArgumentHelper.parseTimestamp(value));
        case "whenBefore", "b" -> args.put("whenBefore", MetaArgumentHelper.parseTimestamp(value));
        case "world", "w" -> args.put("world", MetaArgumentHelper.parseWorld(value).getUID());
        case "sort", "s" -> args.put("sort", MetaArgumentHelper.checkEnum(value, List.of("id", "when")));
        case "order", "o" -> args.put("order", MetaArgumentHelper.checkEnum(value, List.of("asc", "desc")));
        case "page", "p" -> args.put("page", MetaArgumentHelper.parseInt(value, 1));
        default -> InterruptCommandException.showUsage();
      }
    }
    Page<FoundItemDbo> page = FindMe.inst().getFoundItems().search(context.sender(), args);
    ChatMessages msg = new ChatMessages();
    msg.addTranslated(TLK_HEADER, page.page(), page.totalPages());
    for (FoundItemDbo item : page.items()) {
      Findable<?> findable = FindMe.inst().getFindables().get(item.findableId());
      String playerName = Bukkit.getOfflinePlayer(item.playerId()).getName();
      String collectionName = ChatUtils.formatCollectionName(item.collectionId());
      String timestamp = FMUtils.formatDurationOrTimestamp(item.whenFound());
      String findableId;
      if (findable.isEmpty()) {
        findableId = ChatColor.RED + "" + item.findableId();
      } else {
        findableId = String.valueOf(item.findableId());
      }
      msg.addTranslated(TLK_ENTRY, playerName, findableId, collectionName, timestamp);
    }
    context.sendMessage(msg);
  }

}
