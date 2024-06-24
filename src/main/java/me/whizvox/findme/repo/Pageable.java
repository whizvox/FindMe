package me.whizvox.findme.repo;

public record Pageable(int page, int limit) {

  public int offset() {
    return (page - 1) * limit;
  }

  public static final Pageable DEFAULT = new Pageable(0, 10);

}
