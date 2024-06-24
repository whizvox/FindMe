package me.whizvox.findme.command;

import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class CommandHandler {

  public String getUsageArguments() {
    return "";
  }

  public String getDescription(CommandContext context) {
    return "";
  }

  public List<String> listSuggestions(CommandContext context) {
    return List.of();
  }

  public abstract boolean hasPermission(CommandSender sender);

  public abstract void execute(CommandContext context) throws InterruptCommandException;

  public static final CommandHandler EMPTY = new CommandHandler() {
    @Override
    public boolean hasPermission(CommandSender sender) {
      return false;
    }
    @Override
    public void execute(CommandContext context) throws InterruptCommandException {
    }
  };

}
