package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
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

import java.util.List;

public class ListCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.list",
      TLK_DESCRIPTION = "list.description",
      TLK_EMPTY = "list.empty",
      TLK_HEADER = "list.header",
      TLK_BLOCK = "list.block",
      TLK_ENTITY = "list.entity",
      TLK_UNKNOWN_BLOCK = "list.unknownBlock",
      TLK_UNKNOWN_ENTITY = "list.unknownEntity";

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
    return "[<page>]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 2) {
      return SuggestionHelper.pages(context.arg(1), FindMe.inst().getFindables().getTotalCount());
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    int pageNum = ArgumentHelper.getInt(context, 1, () -> 1, 1, Integer.MAX_VALUE);
    Page<FindableDbo> page = FindMe.inst().getFindables().getRepo().findAll(new Pageable(pageNum, 10));
    if (page.totalItems() == 0) {
      context.sendTranslated(TLK_EMPTY);
      return;
    }
    ChatMessages msg = new ChatMessages();
    msg.addTranslated(TLK_HEADER, pageNum, page.totalPages());
    page.items().forEach(findable -> {
      String collectionName = FindMe.inst().getCollections().getCollection(findable.collectionId()).map(col -> col.name).orElse(ChatColor.RED + "???");
      if (findable.isBlock()) {
        World world = Bukkit.getWorld(findable.uuid());
        if (world != null) {
          Block block = world.getBlockAt(findable.x(), findable.y(), findable.z());
          context.sendTranslated(TLK_BLOCK, findable.id(), collectionName, block.getBlockData().getMaterial(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        } else {
          context.sendTranslated(TLK_UNKNOWN_BLOCK, findable.id(), collectionName, findable.uuid(), findable.x(), findable.y(), findable.z());
        }
      } else {
        Entity entity = Bukkit.getEntity(findable.uuid());
        if (entity != null) {
          Location loc = entity.getLocation();
          context.sendTranslated(TLK_ENTITY, findable.id(), collectionName, entity.getType(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
        } else {
          context.sendTranslated(TLK_UNKNOWN_ENTITY, findable.id(), collectionName, findable.uuid());
        }
      }
    });
  }

}
