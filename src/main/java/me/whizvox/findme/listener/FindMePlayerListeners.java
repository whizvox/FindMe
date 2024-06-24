package me.whizvox.findme.listener;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.findable.Findable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class FindMePlayerListeners implements Listener {

  @EventHandler
  public void onPlayerClick(PlayerInteractEvent event) {
    Location location = event.getClickedBlock().getLocation();
    Findable<Block> block = FindMe.inst().getFindables().getBlock(location);
    if (!block.isEmpty()) {
      // TODO Debug code, remove later
      event.getPlayer().sendMessage("FOUND SOMETHING");
    }
  }

}
