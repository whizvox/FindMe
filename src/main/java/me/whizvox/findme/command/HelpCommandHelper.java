package me.whizvox.findme.command;

import me.whizvox.findme.exception.InterruptCommandException;
import me.whizvox.findme.repo.Pageable;
import me.whizvox.findme.util.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommandHelper extends CommandHandler {

  public static final String
      TLK_DESCRIPTION = "help.description",
      TLK_HEADER = "help.header",
      TLK_ENTRY = "help.entry";

  private final CommandDelegator parent;
  private final String permission;

  public HelpCommandHelper(CommandDelegator parent, String permission) {
    this.parent = parent;
    this.permission = permission;
  }

  @Override
  public ChatMessage getDescription(CommandContext context) {
    return ChatMessage.translated(TLK_DESCRIPTION, context.command().getName());
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
    ChatMessages msg = new ChatMessages();
    msg.addTranslated(TLK_HEADER, context.command().getName(), pageNum, (int) Math.ceil((double) parent.getHandlers().size() / pageable.limit()));
    commands.forEach(cmd -> {
      CommandHandler handler = parent.getHandler(cmd);
      String usage = ChatUtils.colorUsage(handler.getUsageArguments());
      msg.addTranslated(TLK_ENTRY, context.command().getName(), cmd, usage.isEmpty() ? "" : " " + usage, handler.getDescription(context).getString());
    });
    context.sendMessage(msg);
  }

}
