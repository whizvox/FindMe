package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CreateCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.create",
      TLK_DESCRIPTION = "create.description",
      TLK_CONFLICT = "create.conflict",
      TLK_SUCCESS = "create.success";

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
    return "<name> [<parent>]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 3) {
      return SuggestionHelper.collections(context.arg(2));
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    String name = ArgumentHelper.getString(context, 1);
    FindableCollection parent = ArgumentHelper.getCollection(context, 2, () -> null);
    if (FindMe.inst().getCollections().getCollection(name).isPresent()) {
      context.sendTranslated(TLK_CONFLICT, name);
      return;
    }
    FindMe.inst().getCollections().create(name, parent == null ? null : parent.name);
    FindMe.inst().saveCollections();
    context.sendTranslated(TLK_SUCCESS, name);
  }

}
