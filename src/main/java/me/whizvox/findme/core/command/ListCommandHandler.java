package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.findable.FindableDbo;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.repo.Page;
import me.whizvox.findme.repo.Pageable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class ListCommandHandler extends CommandHandler {

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.list");
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_LIST_DESCRIPTION);
  }

  @Override
  public String getUsageArguments() {
    return "[<page>]";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    int pageNum = ArgumentHelper.getInt(context, 1, () -> 1, 1, Integer.MAX_VALUE);
    Page<FindableDbo> page = FindMe.inst().getFindables().getRepo().findAll(new Pageable(pageNum, 10));
    page.items().forEach(findable -> {
      String collectionName = FindMe.inst().getCollections().getCollection(findable.collectionId()).map(col -> col.name).orElse(ChatColor.RED + "???");
      if (findable.isBlock()) {
        World world = Bukkit.getWorld(findable.uuid());
        if (world != null) {
          Block block = world.getBlockAt(findable.x(), findable.y(), findable.z());
          context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_LIST_BLOCK, findable.id(), collectionName, block.getBlockData().getMaterial(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
        } else {
          context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_LIST_UNKNOWN_BLOCK, findable.id(), collectionName, findable.uuid(), findable.x(), findable.y(), findable.z()));
        }
      } else {
        Entity entity = Bukkit.getEntity(findable.uuid());
        if (entity != null) {
          Location loc = entity.getLocation();
          context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_LIST_ENTITY, findable.id(), collectionName, entity.getType(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
        } else {
          context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_LIST_UNKNOWN_ENTITY, findable.id(), collectionName, findable.uuid()));
        }
      }
    });
  }

}
