package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SetDefaultCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.setDefault",
      TLK_DESCRIPTION = "setDefault.description",
      TLK_NO_CHANGE = "setDefault.noChange",
      TLK_SUCCESS = "setDefault.success";

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
      context.sendTranslated(TLK_NO_CHANGE);
      return;
    }
    FindMe.inst().getCollections().defaultCollection = collection;
    FindMe.inst().saveCollections();
    context.sendTranslated(TLK_SUCCESS, collection.displayName);
  }

}
