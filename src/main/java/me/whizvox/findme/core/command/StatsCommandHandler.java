package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class StatsCommandHandler extends CommandHandler {

  public static final String
      TLK_HEADER = "stats.header",
      TLK_ENTRY = "stats.entry",
      TLK_NO_COLLECTIONS = "stats.noCollections";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.stats");
  }

  @Override
  public String getUsageArguments() {
    return "[<player>]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 2 && context.sender().hasPermission("findme.stats.other")) {
      return SuggestionHelper.offlinePlayers(context.arg(1));
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    OfflinePlayer player = ArgumentHelper.getOfflinePlayer(context, 1, context::getPlayer);
    List<CollectionCount> counts = new ArrayList<>();
    FindMe.inst().getFoundItems().countPlayerItems(player.getUniqueId()).forEach((collectionId, count) -> {
      FindMe.inst().getCollections().getCollection(collectionId).ifPresent(collection -> {
        int total = FindMe.inst().getFindables().getCount(collectionId);
        counts.add(new CollectionCount(collection, count, total, (float) count / total));
      });
    });
    if (counts.isEmpty()) {
      context.sendTranslated(TLK_NO_COLLECTIONS);
    } else {
      var msg = new TranslatedMessages();
      msg.add(TLK_HEADER, player.getName());
      counts.stream()
          // sort by progress in descending order
          .sorted((o1, o2) -> Float.compare(o2.count, o1.count))
          .forEach(count -> msg.add(TLK_ENTRY, count.collection.displayName, count.count, count.total, count.progress * 100));
      context.sendMessage(msg);
    }
  }

  private record CollectionCount(FindableCollection collection, int count, int total, float progress) {}

}
