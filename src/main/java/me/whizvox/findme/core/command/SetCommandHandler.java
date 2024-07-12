package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.util.ChatUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SetCommandHandler extends CommandHandler {

  public static final String
      PERMISSION = "findme.set",
      TLK_DESCRIPTION = "set.description",
      TLK_UNKNOWN_SOUND = "set.unknownSound",
      TLK_SET = "set.success",
      TLK_CANNOT_UNSET = "set.cannotUnset",
      TLK_UNSET = "set.unset";

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
    return "<collection> <property> [<value>]";
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    return switch (context.argCount()) {
      case 2 -> SuggestionHelper.collections(context.arg(1));
      case 3 -> SuggestionHelper.fromStream(FindableCollection.FIELDS.keySet().stream(), context.arg(2));
      case 4 -> {
        String prop = context.arg(2);
        if (prop.endsWith("Sound")) {
          yield SuggestionHelper.fromStream(Arrays.stream(Sound.values()).map(sound -> sound.getKey().toString()), context.arg(3));
        }
        yield super.listSuggestions(context);
      }
      default -> super.listSuggestions(context);
    };
  }

  private static final Set<String> possibleFields = FindableCollection.FIELDS.keySet().stream()
      .filter(key -> !"id".equals(key))
      .collect(Collectors.toSet());

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, false);
    String property = ArgumentHelper.getEnum(context, 2, possibleFields);
    boolean unset = context.argCount() == 3;
    if (unset) {
      if (property.equals("name")) {
        context.sendTranslated(TLK_CANNOT_UNSET, property);
        return;
      }
      if (property.equals("displayName")) {
        collection.displayName = collection.name;
      } else {
        collection.deserialize(Map.of(property, ""));
      }
      context.sendTranslated(TLK_UNSET, property, collection.displayName);
    } else {
      String valueStr = ArgumentHelper.getString(context, 3);
      Object value;
      if (property.endsWith("Msg") || property.equals("displayName")) {
        value = ChatUtils.colorString(valueStr);
      } else if (property.endsWith("Sound")) {
        if (Registry.SOUNDS.get(NamespacedKey.fromString(valueStr)) == null) {
          context.sendTranslated(TLK_UNKNOWN_SOUND, valueStr);
          return;
        } else {
          value = valueStr;
        }
      } else {
        value = valueStr;
      }
      collection.deserialize(Map.of(property, value));
      FindMe.inst().saveCollections();
      context.sendTranslated(TLK_SET, property, value, collection.displayName);
    }
  }

}
