package me.whizvox.findme.core.founditem;

import me.whizvox.findme.repo.Page;
import me.whizvox.findme.repo.Pageable;
import me.whizvox.findme.repo.Repository;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class FoundItemRepository extends Repository {

  private static final String
      SQL_CREATE = "CREATE TABLE IF NOT EXISTS found_items(" +
          "player CHAR(36) PRIMARY KEY, " +
          "collection SMALLINT NOT NULL, " +
          "findable INT NOT NULL, " +
          "UNIQUE (player,findable), " +
          "FOREIGN KEY (collection) REFERENCES collections(id), " +
          "FOREIGN KEY (findable) REFERENCES findables(id)" +
      ")",
      SQL_INSERT = "INSERT INTO TABLE found_items (player,collection,findable) VALUES (?,?,?)",
      SQL_SELECT_COUNT = "SELECT COUNT(*) FROM found_items",
      SQL_SELECT_ALL = "SELECT player,collection,findable FROM found_items",
      SQL_SELECT_BY_PLAYER = SQL_SELECT_ALL + " WHERE player=?",
      SQL_SELECT_BY_COLLECTION = SQL_SELECT_ALL + " WHERE collection=?",
      SQL_SELECT_BY_FINDABLE = SQL_SELECT_ALL + " WHERE findable=?",
      SQL_SELECT_ONE = SQL_SELECT_ALL + " WHERE player=? AND findable=?",
      SQL_DELETE_ALL = "DELETE FROM found_items",
      SQL_DELETE_ONE = SQL_DELETE_ALL + " WHERE player=? AND findable=?",
      SQL_DELETE_BY_PLAYER = SQL_DELETE_ALL + " WHERE player=?",
      SQL_DELETE_BY_COLLECTION = SQL_DELETE_ALL + " WHERE collection=?",
      SQL_DELETE_BY_FINDABLE = SQL_DELETE_ALL + " WHERE findable=?";

  public FoundItemRepository(Connection conn) {
    super(conn);
  }

  @Override
  protected void initialize_do() {
    execute(SQL_CREATE);
  }

  @Override
  protected String getCountQuery() {
    return SQL_SELECT_COUNT;
  }

  public void insert(FoundItemDbo collection) {
    executeUpdate(SQL_INSERT, List.of(collection.playerId(), collection.collectionId(), collection.findableId()));
  }

  public List<FoundItemDbo> findAll() {
    return fetchList(SQL_SELECT_ALL, null, FoundItemRepository::fromRow);
  }

  public Page<FoundItemDbo> findAll(@Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_ALL, null, pageable, FoundItemRepository::fromRow);
  }

  public Page<FoundItemDbo> findByPlayer(UUID playerId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_BY_PLAYER, List.of(playerId), pageable, FoundItemRepository::fromRow);
  }

  public Page<FoundItemDbo> findByCollection(int collectionId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_BY_COLLECTION, List.of(collectionId), pageable, FoundItemRepository::fromRow);
  }

  public Page<FoundItemDbo> findByFindable(int findableId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_BY_FINDABLE, List.of(findableId), pageable, FoundItemRepository::fromRow);
  }

  public boolean exists(UUID playerId, int findableId) {
    return fetchOne(SQL_SELECT_ONE, List.of(playerId, findableId), FoundItemRepository::fromRow).isPresent();
  }

  public void deleteOne(UUID playerId, int findableId) {
    executeUpdate(SQL_DELETE_ONE, List.of(playerId, findableId));
  }

  public void deleteByPlayer(UUID playerId) {
    executeUpdate(SQL_DELETE_BY_PLAYER, List.of(playerId));
  }

  public void deleteByCollection(int collectionId) {
    executeUpdate(SQL_DELETE_BY_COLLECTION, List.of(collectionId));
  }

  public void deleteByFindable(int findableId) {
    executeUpdate(SQL_DELETE_BY_FINDABLE, List.of(findableId));
  }

  private static FoundItemDbo fromRow(ResultSet rs) throws SQLException {
    return new FoundItemDbo(
        UUID.fromString(rs.getString(1)),
        rs.getShort(2),
        rs.getInt(3)
    );
  }

}
