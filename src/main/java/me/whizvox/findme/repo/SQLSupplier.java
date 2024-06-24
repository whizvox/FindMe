package me.whizvox.findme.repo;

import java.sql.SQLException;

public interface SQLSupplier<T> {

  T get() throws SQLException;

}
