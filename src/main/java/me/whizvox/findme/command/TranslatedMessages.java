package me.whizvox.findme.command;

import me.whizvox.findme.FindMe;

import java.util.ArrayList;
import java.util.List;

public class TranslatedMessages {

  private final List<TranslatedMessage> messages;

  public TranslatedMessages() {
    messages = new ArrayList<>();
  }

  public void add(TranslatedMessage msg) {
    messages.add(msg);
  }

  public void add(String key, Object... args) {
    add(TranslatedMessage.of(key, args));
  }

  public String[] translateToArray() {
    String[] translatedMessages = new String[messages.size()];
    for (int i = 0; i < translatedMessages.length; i++) {
      TranslatedMessage message = messages.get(i);
      translatedMessages[i] = FindMe.inst().translate(message.key(), message.args());
    }
    return translatedMessages;
  }

}
