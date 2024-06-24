package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

public class ReloadCommandHandler extends CommandHandler {

  public ReloadCommandHandler() {
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_RELOAD_DESCRIPTION);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.reload");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindMe.inst().reloadPlugin();
    context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_RELOAD_FINISH));
  }

}
