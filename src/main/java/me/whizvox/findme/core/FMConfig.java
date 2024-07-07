package me.whizvox.findme.core;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class FMConfig {

  public static final String
      CFG_CANCEL_EVENT_ON_FIND = "cancelEventOnFind",
      CFG_PROTECT_BLOCKS = "protectBlocks",
      CFG_PROTECT_ENTITIES = "protectEntities",
      CFG_IMMOBILIZE_ENTITIES = "immobilizeEntities";

  private boolean cancelEventOnFind;
  private boolean protectBlocks;
  private boolean protectEntities;
  private boolean immobilizeEntities;

  public FMConfig() {
  }

  public boolean shouldCancelEventOnFind() {
    return cancelEventOnFind;
  }

  public boolean shouldProtectBlocks() {
    return protectBlocks;
  }

  public boolean shouldProtectEntities() {
    return protectEntities;
  }

  public boolean shouldImmobilizeEntities() {
    return immobilizeEntities;
  }

  public void load(FileConfiguration config) {
    config.addDefault(CFG_CANCEL_EVENT_ON_FIND, false);
    config.addDefault(CFG_PROTECT_BLOCKS, true);
    config.addDefault(CFG_PROTECT_ENTITIES, true);
    config.addDefault(CFG_IMMOBILIZE_ENTITIES, true);
    config.options().copyDefaults(true);
    config.setComments(CFG_CANCEL_EVENT_ON_FIND, List.of("Whether to cancel the interaction event when a player finds an object"));
    config.setComments(CFG_PROTECT_BLOCKS, List.of("Whether to protect findable blocks from being destroyed"));
    config.setComments(CFG_PROTECT_ENTITIES, List.of("Whether to protect findable entities from being killed"));
    config.setComments(CFG_IMMOBILIZE_ENTITIES, List.of("Whether to immobilize findable entities"));
    cancelEventOnFind = config.getBoolean(CFG_CANCEL_EVENT_ON_FIND);
    protectBlocks = config.getBoolean(CFG_PROTECT_BLOCKS);
    protectEntities = config.getBoolean(CFG_PROTECT_ENTITIES);
    immobilizeEntities = config.getBoolean(CFG_IMMOBILIZE_ENTITIES);
  }

  public void save(FileConfiguration config) {
    config.set(CFG_CANCEL_EVENT_ON_FIND, cancelEventOnFind);
    config.set(CFG_PROTECT_BLOCKS, protectBlocks);
    config.set(CFG_PROTECT_ENTITIES, protectEntities);
    config.set(CFG_IMMOBILIZE_ENTITIES, immobilizeEntities);
  }

  public static final FMConfig INST = new FMConfig();

}
