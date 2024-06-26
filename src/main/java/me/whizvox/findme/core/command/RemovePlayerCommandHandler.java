package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RemovePlayerCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.remove.player",
      TLK_DESCRIPTION = "remove.player.description",
      TLK_SUCCESS_PLAYER = "remove.player.successSingle",
      TLK_SUCCESS_COLLECTION = "remove.player.successCollection",
      TLK_SUCCESS_FINDABLE = "remove.player.successFindable";

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
    return "<player> [c:<collection>|f:<findableId>]";
  }

  private static final List<String> OPTIONS = List.of("c:", "f:");

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 2) {
      return SuggestionHelper.offlinePlayers(context.arg(1));
    } else if (context.argCount() == 3) {
      String query = context.arg(2);
      if (query.startsWith("c:")) {
        return SuggestionHelper.fromStream(FindMe.inst().getCollections().stream().map(col -> "c:" + col.name), query);
      }
      List<String> suggestions = SuggestionHelper.fromStream(OPTIONS.stream(), query);
      if (!suggestions.isEmpty()) {
        return suggestions;
      }
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    OfflinePlayer player = ArgumentHelper.getOfflinePlayer(context, 1);
    String query = ArgumentHelper.getString(context, 2, () -> null);
    if (query == null) {
      FindMe.inst().getFoundItems().removePlayer(player.getUniqueId());
      context.sendTranslated(TLK_SUCCESS_PLAYER, player.getName());
    } else if (query.startsWith("c:")) {
      String collectionName = query.substring(2);
      FindMe.inst().getCollections().getCollection(collectionName).ifPresentOrElse(collection -> {
        FindMe.inst().getFoundItems().removeCollection(collection.id);
        context.sendTranslated(TLK_SUCCESS_COLLECTION, collection.displayName);
      }, () -> {
        context.sendTranslated(FMStrings.ERR_UNKNOWN_COLLECTION, collectionName);
      });
    } else if (query.startsWith("f:")) {
      String idStr = query.substring(2);
      try {
        int id = Integer.parseInt(idStr);
        FindMe.inst().getFindables().getRepo().findById(id).ifPresentOrElse(findable -> {
          FindMe.inst().getFoundItems().removePlayerFindable(player.getUniqueId(), id);
          context.sendTranslated(TLK_SUCCESS_FINDABLE, id);
        }, () -> {
          context.sendTranslated(FMStrings.ERR_UNKNOWN_FINDABLE, idStr);
        });
      } catch (NumberFormatException e) {
        context.sendTranslated(FMStrings.ERR_UNKNOWN_FINDABLE, query);
      }
    } else {
      InterruptCommandException.showUsage();
    }
  }

}
