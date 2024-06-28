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

public class CreateCommandHandler extends CommandHandler {

  public static final String
      TLK_NAME_CONFLICT = "command.create.nameConflict",
      TLK_SUCCESS = "command.create.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.create");
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
      InterruptCommandException.halt(FindMe.inst().translate(TLK_NAME_CONFLICT, name));
    }
    FindMe.inst().getCollections().create(name, parent == null ? null : parent.name);
    context.sendMessage(FindMe.inst().translate(TLK_SUCCESS, name));
  }

}
