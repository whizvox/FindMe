package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.findable.Findable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AddBlockCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.add",
      TLK_DESCRIPTION = "add.block.description",
      TLK_CONFLICT = "add.block.conflict",
      TLK_SUCCESS = "add.block.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(PERMISSION);
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "[<collection> [<x> <y> <z> [<world>]]]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    return switch (context.argCount()) {
      case 2 -> SuggestionHelper.collections(context.arg(1));
      case 6 -> SuggestionHelper.worlds(context.arg(5));
      default -> super.listSuggestions(context);
    };
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, true);
    Location loc = ArgumentHelper.getBlockLocation(context, 2, true);
    Findable<Block> findable = FindMe.inst().getFindables().getBlock(loc);
    if (!findable.isEmpty()) {
      String conflictCollectionName = FindMe.inst().getCollections().getCollection(findable.collectionId())
          .map(col -> col.displayName)
          .orElse(ChatColor.RED + "???");
      context.sendTranslated(TLK_CONFLICT, findable.id(), conflictCollectionName);
      return;
    }
    Block block = loc.getBlock();
    findable = FindMe.inst().getFindables().addBlock(collection.id, block);
    context.sendTranslated(TLK_SUCCESS, block.getBlockData().getMaterial(), findable.id(), collection.displayName);
  }

}
