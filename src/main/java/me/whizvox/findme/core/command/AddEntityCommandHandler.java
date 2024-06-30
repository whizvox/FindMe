package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.List;

public class AddEntityCommandHandler extends CommandHandler {

  public static final String
      TLK_DESCRIPTION = "add.entity.description",
      TLK_CONFLICT = "add.entity.conflict",
      TLK_SUCCESS = "add.entity.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(AddBlockCommandHandler.PERMISSION);
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "[<collection> [<entityId>]]";
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
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, true);
    Entity entity = ArgumentHelper.getEntity(context, 2, true);
    Findable<Entity> findable = FindMe.inst().getFindables().getEntity(entity);
    if (!findable.isEmpty()) {
      String conflictCollectionName = FindMe.inst().getCollections().getCollection(findable.collectionId())
          .map(col -> col.displayName)
          .orElse(ChatColor.RED + "???");
      context.sendTranslated(TLK_CONFLICT, findable.id(), conflictCollectionName);
      return;
    }
    findable = FindMe.inst().getFindables().addEntity(collection.id, entity);
    context.sendTranslated(TLK_SUCCESS, entity.getType(), findable.id(), collection.displayName);
  }

}
