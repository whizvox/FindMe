package me.whizvox.findme.repo;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.*;

public abstract class Repository {

  protected final Connection conn;
  private int count;

  public Repository(Connection conn) {
    this.conn = conn;
    count = 0;
  }

  public int getCount() {
    return count;
  }

  protected void changeCount(int amount) {
    count += amount;
  }

  private <T> T handle(SQLSupplier<T> runnable) {
    try {
      return runnable.get();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected boolean execute(String sql) {
    return handle(() -> {
      try (Statement stmt = conn.createStatement()) {
        return stmt.execute(sql);
      }
    });
  }

  protected int executeUpdate(String sql) {
    return handle(() -> {
      try (Statement stmt = conn.createStatement()) {
        return stmt.executeUpdate(sql);
      }
    });
  }

  private <R> R executePrepared(String sql, @Nullable List<Object> args, SQLFunction<PreparedStatement, R> consumer) {
    // FIXME Debug
    //FindMe.inst().getLogger().info("SQL: " + sql + ", ARGS: " + args);
    return handle(() -> {
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        List<Object> actualArgs = Objects.requireNonNullElse(args, List.of());
        int index = 1;
        for (Object arg : actualArgs) {
          if (arg instanceof Boolean value) {
            stmt.setBoolean(index, value);
          } else if (arg instanceof Integer value) {
            stmt.setInt(index, value);
          } else if (arg instanceof Enum<?> value) {
            stmt.setByte(index, (byte) value.ordinal());
          } else if (arg instanceof UUID value) {
            stmt.setString(index, value.toString());
          } else if (arg instanceof SQLNull value) {
            stmt.setNull(index, value.sqlType());
          } else {
            stmt.setString(index, String.valueOf(arg));
          }
          index++;
        }
        return consumer.accept(stmt);
      }
    });
  }

  protected <R> R executeQuery(String sql, @Nullable List<Object> args, SQLFunction<ResultSet, R> consumer) {
    return executePrepared(sql, args, stmt -> {
      ResultSet rs = stmt.executeQuery();
      return consumer.accept(rs);
    });
  }

  protected <R> Optional<R> fetchOne(String sql, @Nullable List<Object> args, SQLFunction<ResultSet, R> fromRow) {
    return executeQuery(sql, args, rs -> {
      if (rs.next()) {
        return Optional.of(fromRow.accept(rs));
      }
      return Optional.empty();
    });
  }

  protected <R> List<R> fetchList(String sql, @Nullable List<Object> args, SQLFunction<ResultSet, R> fromRow) {
    return executeQuery(sql, args, rs -> {
      List<R> items = new ArrayList<>();
      while (rs.next()) {
        items.add(fromRow.accept(rs));
      }
      return Collections.unmodifiableList(items);
    });
  }

  protected <R> Page<R> fetchPage(String sql, @Nullable List<Object> args, @Nullable Pageable pageable, SQLFunction<ResultSet, R> fromRow) {
    pageable = Objects.requireNonNullElse(pageable, Pageable.DEFAULT);
    if (args == null) {
      args = new ArrayList<>();
    } else {
      args = new ArrayList<>(args);
    }
    args.add(pageable.limit());
    args.add(pageable.offset());
    List<R> items = fetchList(sql + " LIMIT ? OFFSET ?", args, fromRow);
    return new Page<>(pageable.page(), (int) Math.ceil((float) count / pageable.limit()), count, items);
  }

  protected int executeUpdate(String sql, @Nullable List<Object> args) {
    return executePrepared(sql, args, stmt -> {
      int updated = stmt.executeUpdate();
      int actualUpdated = 0;
      String toLower = sql.toLowerCase();
      if (toLower.startsWith("insert")) {
        actualUpdated = updated;
      } else if (toLower.startsWith("delete")) {
        actualUpdated = -updated;
      } // can update statements insert or delete rows?
      count += actualUpdated;
      return updated;
    });
  }

  public void initialize() {
    initialize_do();
    count = executeQuery(getCountQuery(), null, rs -> {
      rs.next();
      return rs.getInt(1);
    });
  }

  protected abstract void initialize_do();

  protected abstract String getCountQuery();

}
