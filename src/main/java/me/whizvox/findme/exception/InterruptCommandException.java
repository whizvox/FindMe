package me.whizvox.findme.exception;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMStrings;

public class InterruptCommandException extends RuntimeException {

  public final String formattedMessage;
  public final boolean showUsage;

  public InterruptCommandException(String formattedMessage, boolean showUsage) {
    super("Please report this to the plugin developer if you see this message!");
    this.formattedMessage = formattedMessage;
    this.showUsage = showUsage;
  }

  public boolean hasFormattedMessage() {
    return formattedMessage != null;
  }

  public static <T> T halt(String formattedMessage, boolean showUsage) {
    throw new InterruptCommandException(formattedMessage, showUsage);
  }

  public static <T> T halt(String formattedMessage) {
    return halt(formattedMessage, false);
  }

  public static <T> T showUsage() {
    return halt(null, true);
  }

  public static <T> T playerOnly() {
    return halt(FindMe.inst().translate(FMStrings.ERROR_PLAYER_ONLY));
  }

}
