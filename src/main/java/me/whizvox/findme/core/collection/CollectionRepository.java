package me.whizvox.findme.core.collection;

import me.whizvox.findme.repo.Page;
import me.whizvox.findme.repo.Pageable;
import me.whizvox.findme.repo.Repository;
import me.whizvox.findme.repo.SQLFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CollectionRepository extends Repository {

  private static final String
      SQL_CREATE = "CREATE TABLE IF NOT EXISTS collections(id SMALLINT PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE, display_name VARCHAR(255) NOT NULL)",
      SQL_COUNT = "SELECT COUNT(*) FROM collections",
      SQL_INSERT = "INSERT INTO collections (id,name,display_name) VALUES (?,?,?)",
      SQL_UPDATE_NAME = "UPDATE collections SET name=? WHERE id=?",
      SQL_UPDATE_DISPLAY_NAME = "UPDATE collections SET display_name=? WHERE id=?",
      SQL_SELECT_ALL = "SELECT id,name,display_name FROM collections",
      SQL_SELECT_ONE = SQL_SELECT_ALL + " WHERE id=?",
      SQL_SELECT_BY_NAME = SQL_SELECT_ALL + " WHERE name=?",
      SQL_SELECT_LAST_ID = SQL_SELECT_ALL + " ORDER BY id DESC LIMIT 1",
      SQL_DELETE = "DELETE FROM collections WHERE id=?";

  private int nextId;

  public CollectionRepository(Connection conn) {
    super(conn);
    nextId = 1;
  }

  @Override
  protected void initialize_do() {
    execute(SQL_CREATE);
    nextId = fetchOne(SQL_SELECT_LAST_ID, null, FROM_ROW).map(collection -> collection.id() + 1).orElse(1);
  }

  @Override
  protected String getCountQuery() {
    return SQL_COUNT;
  }

  public CollectionDbo add(String name, @Nullable String displayName) {
    CollectionDbo col = new CollectionDbo(nextId, name, Objects.requireNonNullElse(displayName, name));
    executeUpdate(SQL_INSERT, List.of(col.id(), col.name(), col.displayName()));
    nextId++;
    return col;
  }

  public void updateName(int id, String newName) {
    executeUpdate(SQL_UPDATE_NAME, List.of(newName, id));
  }

  public void updateDisplayName(int id, String newDisplayName) {
    executeUpdate(SQL_UPDATE_DISPLAY_NAME, List.of(newDisplayName, id));
  }

  public List<CollectionDbo> findAll() {
    return fetchList(SQL_SELECT_ALL, null, FROM_ROW);
  }

  public Page<CollectionDbo> findAll(@Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_ALL, null, pageable, FROM_ROW);
  }

  public Optional<CollectionDbo> findById(int id) {
    return fetchOne(SQL_SELECT_ONE, List.of(id), FROM_ROW);
  }

  public Optional<CollectionDbo> findByName(String name) {
    return fetchOne(SQL_SELECT_BY_NAME, List.of(name), FROM_ROW);
  }

  private static final SQLFunction<ResultSet, CollectionDbo> FROM_ROW =
      rs -> new CollectionDbo(rs.getShort(1), rs.getString(2), rs.getString(3));

}
