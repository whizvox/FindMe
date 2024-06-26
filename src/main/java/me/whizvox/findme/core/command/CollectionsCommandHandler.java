package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CollectionsCommandHandler extends CommandHandler {

  public static final String
      TLK_HEADER = "findme.collections.header",
      TLK_ENTRY = "findme.collections.entry";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.collections");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    List<String> messages = new ArrayList<>();
    messages.add(FindMe.inst().translate(TLK_HEADER));
    FindMe.inst().getCollections().stream()
        .sorted(Comparator.comparing(o -> o.name))
        .forEach(collection -> messages.add(FindMe.inst().translate(TLK_ENTRY, collection.displayName, collection.name)));
    context.sendMessage(messages);
  }

}
