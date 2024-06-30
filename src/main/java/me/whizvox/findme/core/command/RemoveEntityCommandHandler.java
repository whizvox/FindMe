package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class RemoveEntityCommandHandler extends CommandHandler {

  public static final String
      TLK_DESCRIPTION = "remove.entity.description",
      TLK_SUCCESS = "remove.entity.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(RemoveFindableCommandHandler.PERMISSION);
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "[<entityId>]";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Entity entity = ArgumentHelper.getEntity(context, 1, true);
    Findable<Entity> findable = FindMe.inst().getFindables().getEntity(entity);
    if (findable.isEmpty()) {
      context.sendTranslated(FMStrings.ERR_ENTITY_NOT_FINDABLE);
      return;
    }
    FindMe.inst().getFoundItems().removeFindable(findable.id());
    FindMe.inst().getFindables().remove(findable.id());
    context.sendTranslated(TLK_SUCCESS);
  }

}
