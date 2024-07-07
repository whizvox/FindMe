package me.whizvox.findme.listener;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.ChatMessage;
import me.whizvox.findme.core.FMConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;

public class BlockListener implements Listener {

  public static final String
      TLK_CANNOT_BREAK = "protect.cannotBreak";

  @EventHandler
  public void onBreakBlock(BlockBreakEvent event) {
    if (FMConfig.INST.shouldProtectBlocks() && !FindMe.inst().getFindables().getBlock(event.getBlock().getLocation()).isEmpty()) {
      ChatMessage.sendTranslated(event.getPlayer(), TLK_CANNOT_BREAK);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPushBlock(BlockBurnEvent event) {
    if (FMConfig.INST.shouldProtectBlocks() && !FindMe.inst().getFindables().getBlock(event.getBlock().getLocation()).isEmpty()) {
      event.setCancelled(true);
    }
  }

}
