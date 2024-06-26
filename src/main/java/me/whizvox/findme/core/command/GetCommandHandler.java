package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;

public class GetCommandHandler extends CommandHandler {

  public static final String
        TLK_SUCCESS = "findme.get.success";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.get");
  }

  @Override
  public String getUsageArguments() {
    return "<collection> <property>";
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, false);
    String property = ArgumentHelper.getLimitedString(context, 2, FindableCollection.FIELDS.keySet());
    Field field = FindableCollection.FIELDS.get(property);
    try {
      Object value = field.get(collection);
      String valueStr;
      if (value instanceof Sound sound) {
        valueStr = sound.getKey().toString();
      } else if (value == null) {
        valueStr = ChatColor.YELLOW + "null";
      } else {
        valueStr = String.valueOf(value);
      }
      context.sendMessage(FindMe.inst().translate(TLK_SUCCESS, collection.displayName, property, valueStr));
    } catch (IllegalAccessException e) {
      // something's gone horribly wrong
      throw new RuntimeException(e);
    }
  }

}
