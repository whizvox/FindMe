package me.whizvox.findme.core.command;

import me.whizvox.findme.command.*;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.List;

public class GetCommandHandler extends CommandHandler {

  public static final String
        PERMISSION = "findme.get",
        TLK_DESCRIPTION = "get.description",
        TLK_SUCCESS = "get.success";

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
    return "<collection> <property>";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    return switch (context.argCount()) {
      case 2 -> SuggestionHelper.collections(context.arg(1));
      case 3 -> SuggestionHelper.fromStream(FindableCollection.FIELDS.keySet().stream(), context.arg(2));
      default -> super.listSuggestions(context);
    };
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, false);
    String property = ArgumentHelper.getEnum(context, 2, FindableCollection.FIELDS.keySet());
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
      context.sendTranslated(TLK_SUCCESS, collection.displayName, property, valueStr);
    } catch (IllegalAccessException e) {
      // something's gone horribly wrong
      throw new RuntimeException(e);
    }
  }

}
