package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

public class ReloadCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.reload",
      TLK_DESCRIPTION = "reload.description",
      TLK_SUCCESS = "reload.success";

  public ReloadCommandHandler() {
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION);
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindMe.inst().reloadPlugin();
    context.sendTranslated(TLK_SUCCESS);
  }

}
