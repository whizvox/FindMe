package me.whizvox.findme.core.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.*;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.collection.FindableCollection;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

public class RevertCommandHandler extends CommandHandler {

  public static final String
      TLK_DESCRIPTION = "revert.description",
      TLK_CANNOT_REVERT = "revert.cannotRevert";

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(SetCommandHandler.PERMISSION);
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
    if (property.equals("name")) {
      context.sendTranslated(TLK_CANNOT_REVERT);
      return;
    }
    Field field = FindableCollection.FIELDS.get(property);
    FindableCollection def = new FindableCollection();
    try {
      Object defaultValue = field.get(def);
      field.set(collection, defaultValue);
      FindMe.inst().saveCollections();
      context.sendTranslated(SetCommandHandler.TLK_SET, property, defaultValue, collection.displayName);
    } catch (IllegalAccessException e) {
      FindMe.inst().getLogger().log(Level.WARNING, "Could not deserialize field " + field.getName() + " for " + FindableCollection.class.getSimpleName(), e);
      context.sendTranslated(FMStrings.ERR_INTERRUPT);
    }
  }

}
