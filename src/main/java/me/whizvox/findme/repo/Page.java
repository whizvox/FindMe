package me.whizvox.findme.repo;

import java.util.List;

public record Page<T>(int page, int totalPages, int totalItems, List<T> items) {

}
