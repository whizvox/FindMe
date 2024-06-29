package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;
import org.bukkit.command.CommandSender;

public interface ChatMessage {

  String getString();

  default boolean isEmpty() {
    return this == EMPTY;
  }

  static ChatMessage literal(Object str) {
    return () -> String.valueOf(str);
  }

  static ChatMessage translated(String key, Object... args) {
    return () -> FindMe.inst().translate(key, args);
  }

  static void sendTranslated(CommandSender sender, String key, Object... args) {
    sender.sendMessage(ChatMessage.translated(key, args).getString());
  }

  ChatMessage EMPTY = () -> "";

}
