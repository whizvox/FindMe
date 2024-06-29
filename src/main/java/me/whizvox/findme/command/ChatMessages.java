package me.whizvox.findme.command;

import java.util.ArrayList;
import java.util.List;

public class ChatMessages {

  private final List<ChatMessage> messages;

  public ChatMessages() {
    messages = new ArrayList<>();
  }

  public void add(ChatMessage msg) {
    messages.add(msg);
  }

  public void addLiteral(Object obj) {
    add(ChatMessage.literal(obj));
  }

  public void addTranslated(String key, Object... args) {
    add(ChatMessage.translated(key, args));
  }

  public String[] translateToArray() {
    String[] translatedMessages = new String[messages.size()];
    for (int i = 0; i < translatedMessages.length; i++) {
      ChatMessage message = messages.get(i);
      translatedMessages[i] = message.getString();
    }
    return translatedMessages;
  }

}
