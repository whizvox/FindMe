package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RemoveBlockCommandHandler extends CommandHandler {

  public static final String
      TLK_DESCRIPTION = "remove.block.description",
      TLK_UNKNOWN = "remove.block.unknown",
      TLK_SUCCESS = "remove.block.success";

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
    return "[<x> <y> <z> [<world>]]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 5) {
      return SuggestionHelper.worlds(context.arg(4));
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    Location loc = ArgumentHelper.getBlockLocation(context, 1, true);
    Findable<Block> block = FindMe.inst().getFindables().getBlock(loc);
    if (block.isEmpty()) {
      context.sendTranslated(TLK_UNKNOWN);
      return;
    }
    FindMe.inst().getFoundItems().removeFindable(block.id());
    FindMe.inst().getFindables().remove(block.id());
    context.sendTranslated(TLK_SUCCESS);
  }

}
