package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class RemovePlayerCommandHandler extends CommandHandler {

  public static final String
      TLK_SUCCESS_PLAYER = "remove.player.successAll",
      TLK_SUCCESS_COLLECTION = "remove.player.successCollection",
      TLK_SUCCESS_FINDABLE = "remove.player.successFindable",
      TLK_DEFAULT_CONFLICT = "remove.player.defaultConflict";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.remove.player");
  }

  @Override
  public String getUsageArguments() {
    return "<player> [c:<collection>|f:<findableId>]";
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
        if (FindMe.inst().getCollections().defaultCollection.id != collection.id) {
          FindMe.inst().getFoundItems().removeCollection(collection.id);
          FindMe.inst().getCollections().delete(collection.id);
          context.sendTranslated(TLK_SUCCESS_COLLECTION, collection.displayName);
        } else {
          context.sendTranslated(TLK_DEFAULT_CONFLICT, collectionName);
        }
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
