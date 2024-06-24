package me.whizvox.findme.core.collection;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CollectionManager {

  private static final String DEFAULT_NAME = "default";

  private final CollectionRepository repo;
  private final Map<Integer, CollectionDbo> byId;
  private final Map<String, CollectionDbo> byName;

  public final CollectionDbo defaultCollection;

  public CollectionManager(Connection conn) {
    repo = new CollectionRepository(conn);
    repo.initialize();
    defaultCollection = repo.findByName(DEFAULT_NAME).orElseGet(() -> repo.add(DEFAULT_NAME, null));
    byId = new HashMap<>();
    byName = new HashMap<>();
    refresh();
  }

  public CollectionRepository getRepo() {
    return repo;
  }

  public void refresh() {
    byId.clear();
    byName.clear();
    repo.findAll().forEach(collection -> {
      byId.put(collection.id(), collection);
      byName.put(collection.name().toLowerCase(), collection);
    });
  }

  public Optional<CollectionDbo> getCollection(int id) {
    return Optional.ofNullable(byId.get(id));
  }

  public Optional<CollectionDbo> getCollection(String name) {
    return Optional.ofNullable(byName.get(name.toLowerCase()));
  }

  public void updateName(String oldName, String newName) {
    getCollection(oldName).ifPresent(collection -> repo.updateName(collection.id(), newName));
  }

  public void updateDisplayName(String name, String newDisplayName) {
    getCollection(name).ifPresent(collection -> repo.updateDisplayName(collection.id(), newDisplayName));
  }

}
