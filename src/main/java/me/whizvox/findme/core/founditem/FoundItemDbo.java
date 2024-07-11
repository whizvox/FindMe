package me.whizvox.findme.core.founditem;

import java.time.LocalDateTime;
import java.util.UUID;

public record FoundItemDbo(UUID playerId, int collectionId, int findableId, LocalDateTime whenFound) {

}
