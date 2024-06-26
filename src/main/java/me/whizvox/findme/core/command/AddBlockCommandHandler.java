package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

public class AddBlockCommandHandler extends CommandHandler {

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.add");
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_ADD_BLOCK_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "[<collection> [<x> <y> <z> [<world>]]]";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, true);
    Location loc = ArgumentHelper.getBlockLocation(context, 2, true);
    if (!FindMe.inst().getFindables().getBlock(loc).isEmpty()) {
      context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_ADD_CONFLICT, collection.displayName));
      return;
    }
    Block block = loc.getBlock();
    Findable<Block> findable = FindMe.inst().getFindables().addBlock(collection.id, block);
    context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_ADD_BLOCK_SUCCESS, block.getBlockData().getMaterial(), findable.id(), collection.displayName));
  }

}
