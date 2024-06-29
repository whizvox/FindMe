package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.command.ChatMessages;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.Comparator;

public class CollectionsCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.collections",
      TLK_DESCRIPTION = "collections.description",
      TLK_HEADER = "collections.header",
      TLK_ENTRY = "collections.entry";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION);
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    ChatMessages msg = new ChatMessages();
    msg.addTranslated(TLK_HEADER);
    FindMe.inst().getCollections().stream()
        .sorted(Comparator.comparing(o -> o.name))
        .forEach(collection -> msg.addTranslated(TLK_ENTRY, collection.displayName, collection.name));
    context.sendMessage(msg);
  }

}
