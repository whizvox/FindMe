package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.CollectionDbo;
import me.whizvox.findme.core.findable.FindableDbo;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class RemoveCommandHandler extends CommandHandler {

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.remove");
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ANY_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "<id>";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    int id = ArgumentHelper.getInt(context, 1, 1, Integer.MAX_VALUE);
    Optional<FindableDbo> findableOp = FindMe.inst().getFindables().getRepo().findById(id);
    if (findableOp.isEmpty()) {
      context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ANY_NOT_FOUND));
      return;
    }
    FindMe.inst().getFindables().getRepo().deleteById(id);
    FindableDbo findable = findableOp.get();
    String collectionName = FindMe.inst().getCollections().getCollection(findable.collectionId())
        .map(CollectionDbo::displayName)
        .orElse(ChatColor.RED + "???");
    if (findable.isBlock()) {
      World world = Bukkit.getWorld(findable.uuid());
      if (world == null) {
        context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ANY_UNKNOWN_BLOCK, id, collectionName));
      } else {
        context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ANY_BLOCK,
            world.getBlockAt(findable.x(), findable.y(), findable.z()).getBlockData().getMaterial(), id, collectionName,
            world.getName(), findable.x(), findable.y(), findable.z())
        );
      }
    } else {
      Entity entity = Bukkit.getEntity(findable.uuid());
      if (entity == null) {
        context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ANY_UNKNOWN_ENTITY, id, collectionName));
      } else {
        Location loc = entity.getLocation();
        context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_REMOVE_ANY_ENTITY, entity.getType(), id,
            collectionName, entity.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
      }
    }
  }

}
