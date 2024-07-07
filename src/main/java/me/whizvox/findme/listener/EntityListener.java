package me.whizvox.findme.listener;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.core.FMConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;

public class EntityListener implements Listener {

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (FMConfig.INST.shouldProtectEntities() && !FindMe.inst().getFindables().getEntity(event.getEntity()).isEmpty()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityTeleportByPortal(EntityPortalEvent event) {
    if (FMConfig.INST.shouldImmobilizeEntities() && !FindMe.inst().getFindables().getEntity(event.getEntity()).isEmpty()) {
      event.setCancelled(true);
    }
  }

}
