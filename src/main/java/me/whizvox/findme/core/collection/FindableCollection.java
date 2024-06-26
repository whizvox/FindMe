package me.whizvox.findme.core.collection;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.util.ChatUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class FindableCollection {

  public int id;
  public String name;
  public String displayName;
  public String findMsg;
  public String findFirstMsg;
  public String completeMsg;
  public String completeBroadcastMsg;
  public String completeFirstMsg;
  public String completeFirstBroadcastMsg;
  @Nullable public Sound findSound;
  @Nullable public Sound findFirstSound;
  @Nullable public Sound completeSound;
  @Nullable public Sound completeBroadcastSound;
  @Nullable public Sound completeFirstSound;
  @Nullable public Sound completeFirstBroadcastSound;

  public FindableCollection() {
    id = 0;
    name = "";
    displayName = "";
    findMsg = ChatUtils.colorString("&aYou've found something! ({c}/{t})");
    findFirstMsg = ChatUtils.colorString("&aYou've found something rather mysterious... (1/{t})");
    completeMsg = ChatUtils.colorString("&aYou've completed the {d} collection!");
    completeBroadcastMsg = "";
    completeFirstMsg = ChatUtils.colorString("&aYou are the first player to complete the {d} collection!");
    completeFirstBroadcastMsg = "";
    findSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    findFirstSound = Sound.ENTITY_PLAYER_LEVELUP;
    completeSound = Sound.ENTITY_PLAYER_LEVELUP;
    completeBroadcastSound = null;
    completeFirstSound = Sound.BLOCK_BELL_RESONATE;
    completeFirstBroadcastSound = null;
  }

  public FindableCollection(FindableCollection other) {
    this.id = other.id;
    this.name = other.name;
    this.completeBroadcastMsg = other.completeBroadcastMsg;
    this.completeBroadcastSound = other.completeBroadcastSound;
    this.completeFirstBroadcastMsg = other.completeFirstBroadcastMsg;
    this.completeFirstBroadcastSound = other.completeFirstBroadcastSound;
    this.completeFirstMsg = other.completeFirstMsg;
    this.completeFirstSound = other.completeFirstSound;
    this.completeMsg = other.completeMsg;
    this.completeSound = other.completeSound;
    this.displayName = other.displayName;
    this.findFirstMsg = other.findFirstMsg;
    this.findFirstSound = other.findFirstSound;
    this.findMsg = other.findMsg;
    this.findSound = other.findSound;
  }

  private static <T> void read(Map<String, Object> data, String key, Consumer<T> consumer) {
    Object obj = data.get(key);
    if (obj != null) {
      //noinspection unchecked
      consumer.accept((T) obj);
    }
  }

  private static void readInt(Map<String, Object> data, String key, Consumer<Integer> consumer) {
    read(data, key, consumer);
  }

  private static void readBoolean(Map<String, Object> data, String key, Consumer<Boolean> consumer) {
    read(data, key, consumer);
  }

  private static void readString(Map<String, Object> data, String key, Consumer<String> consumer) {
    read(data, key, consumer);
  }

  private static void readFormattedText(Map<String, Object> data, String key, Consumer<String> consumer) {
    readString(data, key, value -> consumer.accept(ChatUtils.colorString(value)));
  }

  private static void readSound(Map<String, Object> data, String key, Consumer<Sound> consumer) {
    readString(data, key, value -> {
      if (value == null || value.isEmpty()) {
        consumer.accept(null);
        return;
      }
      Sound sound = Registry.SOUNDS.get(NamespacedKey.fromString(value));
      if (sound == null) {
        FindMe.inst().getLogger().warning("Could not deserialize sound: " + value);
        consumer.accept(null);
      } else {
        consumer.accept(sound);
      }
    });
  }

  private <T> void setField(Field field, T obj) {
    try {
      field.set(this, obj);
    } catch (IllegalAccessException e) {
      FindMe.inst().getLogger().log(Level.WARNING, "Could not set field in " + FindableCollection.class.getSimpleName(), e);
    }
  }

  public boolean isValid() {
    return id > 0 && !name.isEmpty();
  }

  public void deserialize(Map<String, Object> data) {
    data.keySet().forEach(key -> {
      Field field = FIELDS.get(key);
      Class<?> type = field.getType();
      if (Integer.class.isAssignableFrom(type)) {
        readInt(data, key, value -> setField(field, value));
      } else if (Boolean.class.isAssignableFrom(type)) {
        readBoolean(data, key, value -> setField(field, value));
      } else if (String.class.isAssignableFrom(type)) {
        if (name.endsWith("Msg") || key.equals("displayName")) {
          readFormattedText(data, key, value -> setField(field, value));
        } else if (key.endsWith("Sound")) {
          readSound(data, key, value -> setField(field, value));
        } else {
          readString(data, key, value -> setField(field, value));
        }
      }
    });
  }

  public Map<String, Object> serialize() {
    Map<String, Object> data = new HashMap<>();
    FIELDS.forEach((name, field) -> {
      try {
        Class<?> type = field.getType();
        Object obj = field.get(this);
        if (Sound.class.isAssignableFrom(type)) {
          data.put(name, obj == null ? "" : ((Sound) obj).getKey().toString());
        } else {
          data.put(name, obj);
        }
      } catch (IllegalAccessException e) {
        FindMe.inst().getLogger().log(Level.WARNING, "Could not serialize " + FindableCollection.class.getSimpleName() + " field", e);
      }
    });
    return data;
  }

  public static final Map<String, Field> FIELDS;

  static {
    Map<String, Field> fields = new HashMap<>();
    for (Field field : FindableCollection.class.getFields()) {
      int mod = field.getModifiers();
      if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod) && !Modifier.isTransient(mod) && Modifier.isPublic(mod)) {
        fields.put(field.getName(), field);
      }
    }
    FIELDS = Collections.unmodifiableMap(fields);
  }

}
