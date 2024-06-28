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

public class SetDefaultCommandHandler extends CommandHandler {

  public static final String
      TLK_NO_CHANGE = "findme.command.setDefault.noChange",
      TLK_SUCCESS = "findme.command.setDefault.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.setdefault");
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
    if (collection == FindMe.inst().getCollections().getDefaultCollection()) {
      InterruptCommandException.halt(FindMe.inst().translate(TLK_NO_CHANGE));
      return;
    }
    FindMe.inst().getCollections().defaultCollection = collection;
    FindMe.inst().saveCollections();
    context.sendMessage(FindMe.inst().translate(TLK_SUCCESS, collection.displayName));
  }

}
