package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ArgumentHelper;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandHandler;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.util.ChatUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SetCommandHandler extends CommandHandler {

  public static final String
      TLK_UNKNOWN_SOUND = "findme.set.unknownSound",
      TLK_SET = "findme.set.success",
      TLK_CANNOT_UNSET = "findme.set.cannotUnset",
      TLK_UNSET = "findme.set.unset";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("findme.set");
  }

  @Override
  public String getUsageArguments() {
    return "<collection> <property> [value]";
  }

  private static final Set<String> possibleFields = FindableCollection.FIELDS.keySet().stream()
      .filter(key -> !"id".equals(key))
      .collect(Collectors.toSet());

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    FindableCollection collection = ArgumentHelper.getCollection(context, 1, false);
    String property = ArgumentHelper.getLimitedString(context, 2, possibleFields);
    boolean unset = context.argCount() == 3;
    if (unset) {
      if (property.equals("name")) {
        InterruptCommandException.halt(FindMe.inst().translate(TLK_CANNOT_UNSET, property));
        return;
      }
      if (property.equals("displayName")) {
        collection.displayName = collection.name;
      } else {
        collection.deserialize(Map.of(property, ""));
      }
      context.sendMessage(FindMe.inst().translate(TLK_UNSET, property, collection.displayName));
    } else {
      String valueStr = ArgumentHelper.getString(context, 3);
      Object value;
      if (property.endsWith("Msg") || property.equals("displayName")) {
        value = ChatUtils.colorString(valueStr);
      } else if (property.endsWith("Sound")) {
        value = Registry.SOUNDS.get(NamespacedKey.fromString(valueStr));
        if (value == null) {
          InterruptCommandException.halt(FindMe.inst().translate(TLK_UNKNOWN_SOUND, valueStr));
        }
      } else {
        value = valueStr;
      }
      collection.deserialize(Map.of(property, value));
      FindMe.inst().saveCollections();
      context.sendMessage(FindMe.inst().translate(TLK_SET, property, value, collection.displayName));
    }
  }

}
