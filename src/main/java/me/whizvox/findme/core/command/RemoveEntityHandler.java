package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class RemoveEntityHandler extends CommandHandler {

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.remove");
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ENTITY_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "[<entityId>]";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Entity entity = ArgumentHelper.getEntity(context, 1);
    Findable<Entity> findable = FindMe.inst().getFindables().getEntity(entity);
    if (findable.isEmpty()) {
      context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ENTITY_NOT_FOUND));
    }
    FindMe.inst().getFindables().getRepo().deleteEntity(entity.getUniqueId());
    context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ENTITY_SUCCESS));
  }

}
