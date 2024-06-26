package me.whizvox.findme.util;

import org.bukkit.ChatColor;

public class ChatUtils {

  public static String colorString(String str) {
    return ChatColor.translateAlternateColorCodes('&', str);
  }

  public static String colorUsage(String args) {
    StringBuilder sb = new StringBuilder();
    for (char c : args.toCharArray()) {
      if (c == '<' || c == '>') {
        sb.append(ChatColor.LIGHT_PURPLE);
      } else if (c == '[' || c == ']') {
        sb.append(ChatColor.GREEN);
      } else if (c == '|') {
        sb.append(ChatColor.YELLOW);
      }
      sb.append(c);
      if (c == '>' || c == '[' || c == ']' || c == '|') {
        sb.append(ChatColor.RESET);
      }
    }
    return sb.toString();
  }

}
