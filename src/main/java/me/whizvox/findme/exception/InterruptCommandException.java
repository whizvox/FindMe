package me.whizvox.findme.exception;

import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.core.FMStrings;

public class InterruptCommandException extends RuntimeException {

  public final ChatMessage playerMessage;
  public final boolean showUsage;

  public InterruptCommandException(ChatMessage message, boolean showUsage) {
    super("Please report this to the plugin developer if you see this message!");
    this.playerMessage = message;
    this.showUsage = showUsage;
  }

  public boolean hasPlayerMessage() {
    return playerMessage != null;
  }

  public static <T> T halt(ChatMessage message, boolean showUsage) {
    throw new InterruptCommandException(message, showUsage);
  }

  public static <T> T halt(ChatMessage message) {
    return halt(message, false);
  }

  public static <T> T showUsage() {
    return halt(null, true);
  }

  public static <T> T playerOnly() {
    return halt(ChatMessage.translated(FMStrings.ERR_PLAYER_ONLY));
  }

}
