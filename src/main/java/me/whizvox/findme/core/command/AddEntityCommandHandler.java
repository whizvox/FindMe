package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.CollectionDbo;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class AddEntityCommandHandler extends CommandHandler {

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.add");
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_ADD_ENTITY_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "[<collection> [<entityId>]]";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    CollectionDbo collection = ArgumentHelper.getCollection(context, 1);
    Entity entity = ArgumentHelper.getEntity(context, 2);
    if (!FindMe.inst().getFindables().getEntity(entity).isEmpty()) {
      context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_ADD_ERROR, collection.displayName()));
      return;
    }
    Findable<Entity> findable = FindMe.inst().getFindables().addEntity(collection.id(), entity);
    context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_ADD_ENTITY_SUCCESS, entity.getType(), findable.id(), collection.displayName()));
  }

}
