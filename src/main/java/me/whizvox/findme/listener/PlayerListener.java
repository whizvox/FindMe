package me.whizvox.findme.listener;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.util.FMUtils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {

  @EventHandler
  public void onPlayerClick(PlayerInteractEvent event) {
    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    FMUtils.attemptFind(event.getPlayer(), () -> FindMe.inst().getFindables().getBlock(block.getLocation()), event);
  }

  @EventHandler
  public void onEntityInteract(PlayerInteractEntityEvent event) {
    FMUtils.attemptFind(event.getPlayer(), () -> FindMe.inst().getFindables().getEntity(event.getRightClicked()), event);
  }

}
