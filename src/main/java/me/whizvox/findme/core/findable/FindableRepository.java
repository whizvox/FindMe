package me.whizvox.findme.core.findable;

import me.whizvox.findme.repo.Page;
import me.whizvox.findme.repo.Pageable;
import me.whizvox.findme.repo.Repository;
import me.whizvox.findme.repo.SQLFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

public class FindableRepository extends Repository {

  private static final String
      SQL_CREATE = "CREATE TABLE IF NOT EXISTS findables(" +
          "id INT PRIMARY KEY, " +
          "collection SMALLINT NOT NULL, " +
          "is_block BIT NOT NULL, " +
          "uuid CHAR(36) NOT NULL, " +
          "x INT NOT NULL, " +
          "y INT NOT NULL, " +
          "z INT NOT NULL, " +
          "FOREIGN KEY (collection) REFERENCES collections(id)" +
      ")",
      SQL_COUNT = "SELECT COUNT(*) FROM findables",
      SQL_COUNT_COLLECTIONS = "SELECT collection,COUNT(*) AS count FROM findables GROUP BY collection",
      SQL_INSERT = "INSERT INTO findables (id,collection,is_block,uuid,x,y,z) VALUES (?,?,?,?,?,?,?)",
      SQL_SELECT_ALL = "SELECT id,collection,is_block,uuid,x,y,z FROM findables",
      SQL_SELECT_ONE = SQL_SELECT_ALL + " WHERE id=?",
      SQL_SELECT_BY_COLLECTION = SQL_SELECT_ALL + " WHERE collection=?",
      SQL_SELECT_ENTITY = SQL_SELECT_ALL + " WHERE is_block=0 AND uuid=?",
      SQL_SELECT_BLOCK = SQL_SELECT_ALL + " WHERE is_block=1 AND uuid=? AND x=? AND y=? AND z=?",
      SQL_SELECT_LAST = SQL_SELECT_ALL + " ORDER BY id DESC LIMIT 1",
      SQL_DELETE_ALL = "DELETE FROM findables",
      SQL_DELETE_ONE = SQL_DELETE_ALL + " WHERE id=?",
      SQL_DELETE_ENTITY = SQL_DELETE_ALL + " WHERE is_block=0 AND uuid=?",
      SQL_DELETE_BLOCK = SQL_DELETE_ALL + " WHERE is_block=1 AND uuid=? AND x=? AND y=? AND z=?",
      SQL_DELETE_BY_COLLECTION = SQL_DELETE_ALL + " WHERE collection=?";

  private int nextId;

  public FindableRepository(Connection conn) {
    super(conn);
    nextId = 1;
  }

  @Override
  protected void initialize_do() {
    execute(SQL_CREATE);
    nextId = fetchOne(SQL_SELECT_LAST, null, FROM_ROW).map(findable -> findable.id() + 1).orElse(1);
  }

  @Override
  protected String getCountQuery() {
    return SQL_COUNT;
  }

  private void add(FindableDbo findable) {
    executeUpdate(SQL_INSERT, List.of(findable.id(), findable.collectionId(), findable.isBlock(), findable.uuid(), findable.x(), findable.y(), findable.z()));
    nextId++;
  }

  public FindableDbo addEntity(int collectionId, UUID entityId) {
    FindableDbo findable = new FindableDbo(nextId, collectionId, false, entityId, 0, 0, 0);
    add(findable);
    return findable;
  }

  public FindableDbo addBlock(int collectionId, UUID worldId, int x, int y, int z) {
    FindableDbo findable = new FindableDbo(nextId, collectionId, true, worldId, x, y, z);
    add(findable);
    return findable;
  }

  public List<FindableDbo> findAll() {
    return fetchList(SQL_SELECT_ALL, null, FROM_ROW);
  }

  public Page<FindableDbo> findAll(@Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_ALL, null, pageable, FROM_ROW);
  }

  public Optional<FindableDbo> findById(int id) {
    return fetchOne(SQL_SELECT_ONE, List.of(id), FROM_ROW);
  }

  public Page<FindableDbo> findByCollection(int collectionId, @Nullable Pageable pageable) {
    return fetchPage(SQL_SELECT_BY_COLLECTION, List.of(collectionId), pageable, FROM_ROW);
  }

  public Optional<FindableDbo> findEntity(UUID entityId) {
    return fetchOne(SQL_SELECT_ENTITY, List.of(entityId), FROM_ROW);
  }

  public Optional<FindableDbo> findBlock(UUID worldId, int x, int y, int z) {
    return fetchOne(SQL_SELECT_BLOCK, List.of(worldId, x, y, z), FROM_ROW);
  }

  public Map<Integer, Integer> countCollections() {
    return executeQuery(SQL_COUNT_COLLECTIONS, null, rs -> {
      Map<Integer, Integer> counts = new HashMap<>();
      while (rs.next()) {
        counts.put(rs.getInt(1), rs.getInt(2));
      }
      return counts;
    });
  }

  public void deleteById(int id) {
    executeUpdate(SQL_DELETE_ONE, List.of(id));
  }

  public void deleteEntity(UUID entityId) {
    executeUpdate(SQL_DELETE_ENTITY, List.of(entityId));
  }

  public void deleteBlock(UUID worldId, int x, int y, int z) {
    executeUpdate(SQL_DELETE_BLOCK, List.of(worldId, x, y, z));
  }

  public void deleteByCollection(int collectionId) {
    executeUpdate(SQL_DELETE_BY_COLLECTION, List.of(collectionId));
  }

  private static final SQLFunction<ResultSet, FindableDbo> FROM_ROW = rs ->
      new FindableDbo(rs.getInt(1), rs.getInt(2), rs.getBoolean(3), UUID.fromString(rs.getString(4)), rs.getInt(5),
          rs.getInt(6), rs.getInt(7));

}
