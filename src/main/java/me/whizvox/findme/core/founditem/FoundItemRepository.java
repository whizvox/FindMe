package me.whizvox.findme.core.founditem;

import me.whizvox.findme.repo.Page;
import me.whizvox.findme.repo.Pageable;
import me.whizvox.findme.repo.Repository;
import me.whizvox.findme.repo.SQLFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FoundItemRepository extends Repository {

  private static final String
      SQL_CREATE = "CREATE TABLE IF NOT EXISTS found_items(" +
          "player CHAR(36) NOT NULL, " +
          "collection SMALLINT NOT NULL, " +
          "findable INT NOT NULL, " +
          "when_found TIMESTAMP NOT NULL, " +
          "UNIQUE (player,findable)" +
      ")",
      SQL_INSERT = "INSERT INTO found_items (player,collection,findable,when_found) VALUES (?,?,?,?)",
      SQL_SELECT_COUNT = "SELECT COUNT(*) FROM found_items",
      SQL_SELECT_PLAYER_COUNTS = "SELECT collection,COUNT(*) AS count FROM found_items WHERE player=? GROUP BY collection",
      SQL_SELECT_ALL = "SELECT player,collection,findable,when_found FROM found_items",
      SQL_SELECT_BY_PLAYER = SQL_SELECT_ALL + " WHERE player=?",
      SQL_SELECT_BY_COLLECTION = SQL_SELECT_ALL + " WHERE collection=?",
      SQL_SELECT_BY_FINDABLE = SQL_SELECT_ALL + " WHERE findable=?",
      SQL_SELECT_PLAYER_COLLECTION = SQL_SELECT_ALL + " WHERE player=? AND collection=?",
      SQL_SELECT_ONE = SQL_SELECT_ALL + " WHERE player=? AND findable=?",
      SQL_DELETE_ALL = "DELETE FROM found_items",
      SQL_DELETE_ONE = SQL_DELETE_ALL + " WHERE player=? AND findable=?",
      SQL_DELETE_BY_PLAYER = SQL_DELETE_ALL + " WHERE player=?",
      SQL_DELETE_BY_COLLECTION = SQL_DELETE_ALL + " WHERE collection=?",
      SQL_DELETE_BY_FINDABLE = SQL_DELETE_ALL + " WHERE findable=?",
      SQL_DELETE_BY_PLAYER_COLLECTION = SQL_DELETE_ALL + " WHERE player=? AND collection=?";

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

  public void insert(UUID playerId, int collectionId, int findableId) {
    executeUpdate(SQL_INSERT, List.of(playerId, collectionId, findableId, LocalDateTime.now()));
  }

  public List<FoundItemDbo> findAll() {
    return fetchList(SQL_SELECT_ALL, null, FROM_ROW);
  }

  public Page<FoundItemDbo> findAll(@Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_ALL, null, pageable, FROM_ROW);
  }

  public Page<FoundItemDbo> findByPlayer(UUID playerId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_BY_PLAYER, List.of(playerId), pageable, FROM_ROW);
  }

  public Page<FoundItemDbo> findByCollection(int collectionId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_BY_COLLECTION, List.of(collectionId), pageable, FROM_ROW);
  }

  public Page<FoundItemDbo> findByFindable(int findableId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_BY_FINDABLE, List.of(findableId), pageable, FROM_ROW);
  }

  public Page<FoundItemDbo> findByPlayerCollection(UUID playerId, int collectionId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_PLAYER_COLLECTION, List.of(playerId, collectionId), pageable, FROM_ROW);
  }

  public Map<Integer, Integer> countPlayerItems(UUID playerId) {
    return executeQuery(SQL_SELECT_PLAYER_COUNTS, List.of(playerId), rs -> {
      Map<Integer, Integer> counts = new HashMap<>();
      while (rs.next()) {
        counts.put(rs.getInt(1), rs.getInt(2));
      }
      return counts;
    });
  }

  public boolean exists(UUID playerId, int findableId) {
    return fetchOne(SQL_SELECT_ONE, List.of(playerId, findableId), FROM_ROW).isPresent();
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

  public void deleteByPlayerCollection(UUID playerId, int findableId) {
    executeUpdate(SQL_DELETE_BY_PLAYER_COLLECTION, List.of(playerId, findableId));
  }

  private static final SQLFunction<ResultSet, FoundItemDbo> FROM_ROW = rs -> new FoundItemDbo(
      UUID.fromString(rs.getString(1)),
      rs.getShort(2),
      rs.getInt(3),
      rs.getTimestamp(4).toLocalDateTime()
  );

}
