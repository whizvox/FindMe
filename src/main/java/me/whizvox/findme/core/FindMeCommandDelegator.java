package me.whizvox.findme.core;

import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandDelegator;
import me.whizvox.findme.command.HelpCommandHelper;
import me.whizvox.findme.command.ReloadCommandHandler;
import me.whizvox.findme.core.command.*;

import java.util.List;

public class FindMeCommandDelegator extends CommandDelegator {

  public static final String
      PERMISSION_INFO = "findme.info",
      TLK_INFO = "base.info";

  public FindMeCommandDelegator() {
    super();
    register("help", List.of("?"), new HelpCommandHelper(this, "findme.help"));
    register("reload", List.of(), new ReloadCommandHandler());
    register("list", List.of(), new ListCommandHandler());
    register("addblock", List.of("addb"), new AddBlockCommandHandler());
    register("addentity", List.of("adde"), new AddEntityCommandHandler());
    register("removeblock", List.of("remb"), new RemoveBlockCommandHandler());
    register("removeentity", List.of("reme"), new RemoveEntityCommandHandler());
    register("set", List.of(), new SetCommandHandler());
    register("get", List.of(), new GetCommandHandler());
    register("setdefault", List.of(), new SetDefaultCommandHandler());
    register("collections", List.of("col"), new CollectionsCommandHandler());
    register("create", List.of(), new CreateCommandHandler());
    register("removeplayer", List.of("remp"), new RemovePlayerCommandHandler());
    register("removecollection", List.of("remc"), new RemoveCollectionCommandHandler());
    register("removefindable", List.of("remf"), new RemoveFindableCommandHandler());
    register("stats", List.of(), new StatsCommandHandler());
    register("revert", List.of(), new RevertCommandHandler());
    register("teleport", List.of("tp"), new TeleportCommandHandler());
    register("info", List.of("i"), new InfoCommandHandler());
    register("history", List.of("h"), new HistoryCommandHandler());
  }

  @Override
  public boolean onUnknownCommand(CommandContext context) {
    if (context.argCount() == 0) {
      if (context.sender().hasPermission(PERMISSION_INFO)) {
        context.sendTranslated(TLK_INFO);
        return true;
      }
    }
    return false;
  }

}
