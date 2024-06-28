package me.whizvox.findme.command;

public record TranslatedMessage(String key, Object[] args) {

  public static TranslatedMessage of(String key, Object... args) {
    return new TranslatedMessage(key, args);
  }

}
