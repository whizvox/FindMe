package me.whizvox.findme.command;

import me.whizvox.findme.exception.InterruptCommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.StreamSupport;

public record CommandContext(List<String> args,
                             String label,
                             Command command,
                             CommandSender sender) {

  public String arg(int index) {
    return args.get(index);
  }

  public String localLabel() {
    return args.get(0);
  }

  public int argCount() {
    return args.size();
  }

  public Player getPlayer() {
    if (sender instanceof Player player) {
      return player;
    }
    return InterruptCommandException.playerOnly();
  }

  public void sendMessage(String message) {
    sender.sendMessage(message);
  }

  public void sendMessage(String... messages) {
    sender.sendMessage(messages);
  }

  public void sendMessage(Iterable<String> messages) {
    sender.sendMessage(StreamSupport.stream(messages.spliterator(), false).toArray(String[]::new));
  }

}
