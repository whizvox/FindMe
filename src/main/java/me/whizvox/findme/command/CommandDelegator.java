package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.core.Reloadable;
import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandDelegator implements CommandExecutor, TabCompleter, Reloadable {

  private final Map<String, CommandHandler> handlers;
  private final Map<String, String> aliases;

  public CommandDelegator() {
    handlers = new HashMap<>();
    aliases = new HashMap<>();
  }

  public Map<String, String> getAliases() {
    return Collections.unmodifiableMap(aliases);
  }

  public Map<String, CommandHandler> getHandlers() {
    return Collections.unmodifiableMap(handlers);
  }

  public CommandHandler getHandler(String label) {
    String commandName = aliases.get(label);
    if (commandName == null) {
      return CommandHandler.EMPTY;
    }
    return handlers.getOrDefault(commandName, CommandHandler.EMPTY);
  }

  public void register(String commandName, @Nullable List<String> aliases, CommandHandler handler) {
    if (aliases != null) {
      aliases.forEach(alias -> this.aliases.put(alias, commandName));
    }
    this.aliases.put(commandName, commandName);
    handlers.put(commandName, handler);
  }

  public boolean onUnknownCommand(CommandContext context) {
    return false;
  }

  @Override
  public void onReload() {
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    CommandContext context = new CommandContext(List.of(args), label, command, sender);
    if (args.length == 0) {
      return onUnknownCommand(context);
    }
    String subCommand = args[0];
    CommandHandler handler = getHandler(subCommand);
    if (handler == CommandHandler.EMPTY) {
      return onUnknownCommand(context);
    }
    try {
      if (handler.hasPermission(sender)) {
        handler.execute(context);
      } else {
        sender.sendMessage(FindMe.inst().translate(FMStrings.ERROR_NO_PERMISSION));
      }
    } catch (InterruptCommandException e) {
      if (e.hasFormattedMessage()) {
        sender.sendMessage(e.formattedMessage);
      }
      if (e.showUsage) {
        sender.sendMessage(FindMe.inst().translate(FMStrings.ERROR_USAGE, "/%s %s %s".formatted(label, subCommand, handler.getUsageArguments())));
      }
    }
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    String subCommand = args[0];
    if (args.length == 1) {
      return aliases.entrySet().stream()
          .filter(entry -> entry.getKey().startsWith(subCommand) && handlers.get(entry.getValue()).hasPermission(sender))
          .map(Map.Entry::getKey)
          .toList();
    } else {
      return getHandler(subCommand).listSuggestions(new CommandContext(List.of(args), label, command, sender));
    }
  }

}
