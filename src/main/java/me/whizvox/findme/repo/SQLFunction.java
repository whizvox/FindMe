package me.whizvox.findme.repo;

import java.sql.SQLException;

public interface SQLFunction<A, R> {

  R accept(A obj) throws SQLException;

}
