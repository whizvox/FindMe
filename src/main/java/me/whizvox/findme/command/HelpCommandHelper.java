package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMStrings;
import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.repo.Pageable;
import me.whizvox.findme.util.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommandHelper extends CommandHandler {

  private final CommandDelegator parent;
  private final String permission;

  public HelpCommandHelper(CommandDelegator parent, String permission) {
    this.parent = parent;
    this.permission = permission;
  }

  @Override
  public String getDescription(CommandContext context) {
    return FindMe.inst().translate(FMStrings.COMMAND_HELP_DESCRIPTION, context.command().getName());
  }

  @Override
  public String getUsageArguments() {
    return "[<page>]";
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission(permission);
  }

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.argCount() == 2) {
      return SuggestionHelper.pages(context.arg(1), parent.getHandlers().size(), 10);
    }
    return super.listSuggestions(context);
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    int pageNum = ArgumentHelper.getInt(context, 1, () -> 1, 1, Integer.MAX_VALUE);
    Pageable pageable = new Pageable(pageNum, 10);
    List<String> commands = parent.getHandlers().keySet().stream()
        .filter(cmd -> parent.getHandler(cmd).hasPermission(context.sender()))
        .sorted()
        .skip((pageNum - 1) * pageable.limit())
        .limit(10)
        .toList();
    List<String> messages = new ArrayList<>();
    messages.add(FindMe.inst().translate(FMStrings.COMMAND_HELP_HEADER, context.command().getName(), pageNum, (int) Math.ceil((double) parent.getHandlers().size() / pageable.limit())));
    commands.forEach(cmd -> {
      CommandHandler handler = parent.getHandler(cmd);
      messages.add(FindMe.inst().translate(FMStrings.COMMAND_HELP_ENTRY, context.command().getName(), cmd, ChatUtils.colorUsage(handler.getUsageArguments()), handler.getDescription(context)));
    });
    context.sendMessage(messages);
  }

}
