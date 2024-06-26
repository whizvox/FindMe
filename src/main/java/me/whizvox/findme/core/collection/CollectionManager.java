package me.whizvox.findme.core.collection;

import me.whizvox.findme.FindMe;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class CollectionManager {

  private final Map<String, FindableCollection> byName;
  private final Map<Integer, FindableCollection> byId;
  public FindableCollection defaultCollection;

  public CollectionManager() {
    byName = new HashMap<>();
    byId = new HashMap<>();
    defaultCollection = null;
  }

  public void load(FileConfiguration config) {
    byName.clear();
    byId.clear();
    String defaultCollectionName = config.getString("default", "default");
    config.getMapList("collections").forEach(data -> {
      FindableCollection collection = new FindableCollection();
      //noinspection unchecked
      collection.deserialize((Map<String, Object>) data);
      if (!byName.containsKey(collection.name) && !byId.containsKey(collection.id)) {
        byName.put(collection.name.toLowerCase(), collection);
        byId.put(collection.id, collection);
      } else {
        FindMe.inst().getLogger().warning("Could not load collection from configuration file, duplicate ID or name: [" + collection.id + "] " + collection.name);
      }
    });
    if (!byName.containsKey(defaultCollectionName)) {
      defaultCollection = create(defaultCollectionName, null);
    } else {
      defaultCollection = byName.get(defaultCollectionName);
    }
  }

  public void save(FileConfiguration config) {
    config.set("default", defaultCollection.name);
    config.set("collections", byName.values().stream().map(FindableCollection::serialize).toList());
  }

  public FindableCollection getDefaultCollection() {
    return defaultCollection;
  }

  public FindableCollection create(String name, @Nullable String parentName) {
    if (byName.containsKey(name)) {
      return null;
    }
    int id = byId.keySet().stream().max(Integer::compare).orElse(0) + 1;
    FindableCollection collection;
    if (parentName != null) {
      // copy or else create new
      collection = getCollection(parentName).map(FindableCollection::new).orElseGet(FindableCollection::new);
    } else {
      collection = new FindableCollection();
    }
    collection.id = id;
    collection.name = name;
    collection.displayName = name;
    byName.put(collection.name, collection);
    byId.put(collection.id, collection);
    return collection;
  }

  public Optional<FindableCollection> getCollection(int id) {
    return Optional.ofNullable(byId.get(id));
  }

  public Optional<FindableCollection> getCollection(String name) {
    return Optional.ofNullable(byName.get(name.toLowerCase()));
  }

  public Stream<FindableCollection> stream() {
    return byName.values().stream();
  }

}
