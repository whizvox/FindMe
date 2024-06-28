package me.whizvox.findme.core;

import me.whizvox.findme.FindMe;
import me.whizvox.findme.command.CommandContext;
import me.whizvox.findme.command.CommandDelegator;
import me.whizvox.findme.command.HelpCommandHelper;
import me.whizvox.findme.command.ReloadCommandHandler;
import me.whizvox.findme.core.command.*;

import java.util.List;

public class FindMeCommandDelegator extends CommandDelegator {

  public FindMeCommandDelegator() {
    super();
    register("help", List.of("?"), new HelpCommandHelper(this, "findme.help"));
    register("reload", List.of(), new ReloadCommandHandler());
    register("list", List.of(), new ListCommandHandler());
    register("addblock", List.of("addb"), new AddBlockCommandHandler());
    register("addentity", List.of("adde"), new AddEntityCommandHandler());
    //register("remove", List.of("rem"), new RemoveCommandHandler());
    register("removeblock", List.of("remb"), new RemoveBlockCommandHandler());
    register("removeentity", List.of("reme"), new RemoveEntityHandler());
    register("set", List.of(), new SetCommandHandler());
    register("get", List.of(), new GetCommandHandler());
    register("setdefault", List.of(), new SetDefaultCommandHandler());
    register("collections", List.of("col"), new CollectionsCommandHandler());
    register("create", List.of(), new CreateCommandHandler());
    register("removeplayer", List.of("remp"), new RemovePlayerCommandHandler());
    register("removecollection", List.of("remc"), new RemoveCollectionHandler());
    register("removefindable", List.of("remf"), new RemoveFindableCommandHandler());
    register("stats", List.of(), new StatsCommandHandler());
  }

  @Override
  public boolean onUnknownCommand(CommandContext context) {
    if (context.argCount() == 0) {
      context.sendMessage(FindMe.inst().translate(FMStrings.COMMAND_FINDME_INFO));
      return true;
    }
    return false;
  }

}
