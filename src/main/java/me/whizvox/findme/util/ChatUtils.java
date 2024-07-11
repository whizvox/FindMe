package me.whizvox.findme.util;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.findable.Findable;
import me.whizvox.findme.findable.FindableType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class ChatUtils {

  public static String colorString(String str) {
    return ChatColor.translateAlternateColorCodes('&', str);
  }

  public static String colorUsage(String args) {
    StringBuilder sb = new StringBuilder();
    for (char c : args.toCharArray()) {
      if (c == '<' || c == '>') {
        sb.append(ChatColor.LIGHT_PURPLE);
      } else if (c == '[' || c == ']') {
        sb.append(ChatColor.GREEN);
      } else if (c == '|') {
        sb.append(ChatColor.YELLOW);
      }
      sb.append(c);
      if (c == '>' || c == '[' || c == ']' || c == '|') {
        sb.append(ChatColor.RESET);
      }
    }
    return sb.toString();
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static String formatCollectionName(Optional<FindableCollection> collectionOp) {
    return collectionOp
        .map(col -> col.displayName)
        .orElse(ChatColor.RED + "???");
  }

  public static String formatCollectionName(int collectionId) {
    return formatCollectionName(FindMe.inst().getCollections().getCollection(collectionId));
  }

  public static String formatCollectionName(String collectionName) {
    return formatCollectionName(FindMe.inst().getCollections().getCollection(collectionName));
  }

  public static String formatFindable(Findable<?> findable) {
    String collectionName = formatCollectionName(findable.collectionId());
    ChatMessage msg;
    if (findable.type() == FindableType.BLOCK) {
      Block block = (Block) findable.object();
      Location loc = block.getLocation();
      if (Bukkit.getWorld(loc.getWorld().getUID()) != null) {
        msg = ChatMessage.translated(FMStrings.ENTRY_FINDABLE_BLOCK, findable.id(), collectionName, block.getType(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
      } else {
        msg = ChatMessage.translated(FMStrings.ENTRY_FINDABLE_UNKNOWN_BLOCK, findable.id(), collectionName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
      }
    } else {
      Entity entity = (Entity) findable.object();
      if (entity.isValid()) {
        Location loc = entity.getLocation();
        msg = ChatMessage.translated(FMStrings.ENTRY_FINDABLE_ENTITY, findable.id(), collectionName, entity.getType(), entity.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
      } else {
        msg = ChatMessage.translated(FMStrings.ENTRY_FINDABLE_UNKNOWN_ENTITY, findable.id(), collectionName, entity.getUniqueId());
      }
    }
    return msg.getString();
  }

}
