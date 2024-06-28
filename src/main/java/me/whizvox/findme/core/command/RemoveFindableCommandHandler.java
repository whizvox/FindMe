package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.command.CommandSender;

public class RemoveFindableCommandHandler extends CommandHandler {

  public static final String
      TLK_SUCCESS = "remove.findable.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.remove.findable");
  }

  @Override
  public String getUsageArguments() {
    return "<findableId>";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Findable<?> findable = ArgumentHelper.getFindable(context, 1);
    FindMe.inst().getFoundItems().removeFindable(findable.id());
    FindMe.inst().getFindables().remove(findable.id());
    context.sendTranslated(TLK_SUCCESS, findable.id());
  }

}
