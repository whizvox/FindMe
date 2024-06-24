package me.whizvox.findme.repo;

import java.util.List;
import java.util.stream.Stream;

public record Page<T>(int page, int totalPages, int totalItems, List<T> items) {

}
