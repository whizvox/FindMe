package me.whizvox.findme.core.findable;

import java.util.UUID;

public record FindableDbo(int id, int collectionId, boolean isBlock, UUID uuid, int x, int y, int z) {
}
