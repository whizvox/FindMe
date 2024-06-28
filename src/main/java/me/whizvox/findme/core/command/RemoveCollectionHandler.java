package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.command.SuggestionHelper;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RemoveCollectionHandler extends CommandHandler {

  public static final String
      TLK_SUCCESS = "remove.collection.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.remove.collection");
  }

  @Override
  public String getUsageArguments() {
    return "<collection>";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 2) {
      return SuggestionHelper.collections(context.arg(1));
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, false);
    FindMe.inst().getFoundItems().removeCollection(collection.id);
    FindMe.inst().getCollections().delete(collection.id);
    context.sendTranslated(TLK_SUCCESS, collection.displayName);
  }

}
