package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.CollectionDbo;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.util.RayTraceResult;

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
    return "[<collection>]";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    CollectionDbo collection = ArgumentHelper.getCollection(context, 1);
    RayTraceResult hit = context.getPlayer().rayTraceBlocks(5.0);
    if (hit == null) {
      context.sendMessage(FindMe.inst().translate(FMStrings.ERROR_NO_BLOCK_FOUND));
      return;
    }
    Block block = hit.getHitBlock();
    if (!FindMe.inst().getFindables().getBlock(block.getLocation()).isEmpty()) {
      context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_ADD_ERROR, collection.displayName()));
      return;
    }
    Findable<Block> findable = FindMe.inst().getFindables().addBlock(collection.id(), block);
    context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_ADD_BLOCK_SUCCESS, block.getBlockData().getMaterial(), findable.id(), collection.displayName()));
  }

}
