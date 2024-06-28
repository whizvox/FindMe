package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.command.SuggestionHelper;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RemoveBlockCommandHandler extends CommandHandler {

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.remove");
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_REMOVE_BLOCK_DESCRIPTION);
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
      context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_BLOCK_NOT_FOUND));
      return;
    }
    FindMe.inst().getFindables().getRepo().deleteById(block.id());
    context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_BLOCK_SUCCESS));
  }

}
