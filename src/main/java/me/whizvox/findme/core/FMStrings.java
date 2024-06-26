package me.whizvox.findme.core;

import me.whizvox.findme.core.command.CollectionsCommandHandler;
import me.whizvox.findme.core.command.GetCommandHandler;
import me.whizvox.findme.core.command.SetCommandHandler;
import me.whizvox.findme.core.command.SetDefaultCommandHandler;

import java.util.Map;

import static java.util.Map.entry;

public class FMStrings {

  public static final String
      ERROR_USAGE = "error.usage",
      ERROR_PLAYER_ONLY = "error.playerOnly",
      ERROR_NO_PERMISSION = "error.noPermission",
      ERROR_INTERRUPT = "error.interrupt",
      ERROR_COMMAND_NOT_FOUND = "error.commandNotFound",
      ERROR_INVALID_INT = "error.invalidInt",
      ERROR_INT_OUT_OF_RANGE = "error.intOutOfRange",
      ERROR_MANUAL_NO_COMMAND_FOUND = "error.manual.noCommandFound",
      ERROR_NO_BLOCK_FOUND = "error.noBlockFound",
      COMMAND_HELP_HEADER = "command.help.header",
      COMMAND_HELP_ENTRY = "command.help.entry",
      COMMAND_HELP_DESCRIPTION = "command.help.description",
      COMMAND_RELOAD_DESCRIPTION = "command.reload.description",
      COMMAND_RELOAD_FINISH = "command.reload.finish",
      COMMAND_ADD_BLOCK_SUCCESS = "command.addBlock.success",
      COMMAND_ADD_BLOCK_DESCRIPTION = "command.addBlock.description",
      COMMAND_ADD_ENTITY_SUCCESS = "command.addEntity.success",
      COMMAND_ADD_ENTITY_DESCRIPTION = "command.addEntity.description",
      COMMAND_ADD_CONFLICT = "command.add.conflict",
      COMMAND_REMOVE_ANY_DESCRIPTION = "command.removeAny.description",
      COMMAND_REMOVE_ANY_NOT_FOUND = "command.removeAny.notFound",
      COMMAND_REMOVE_ANY_BLOCK = "command.removeAny.block",
      COMMAND_REMOVE_ANY_ENTITY = "command.removeAny.entity",
      COMMAND_REMOVE_ANY_UNKNOWN_BLOCK = "command.removeAny.unknownBlock",
      COMMAND_REMOVE_ANY_UNKNOWN_ENTITY = "command.removeAny.unknownEntity",
      COMMAND_REMOVE_BLOCK_DESCRIPTION = "command.removeBlock.description",
      COMMAND_REMOVE_BLOCK_NOT_FOUND = "command.removeBlock.notFound",
      COMMAND_REMOVE_BLOCK_SUCCESS = "command.removeBlock.success",
      COMMAND_REMOVE_ENTITY_DESCRIPTION = "command.removeEntity.description",
      COMMAND_REMOVE_ENTITY_NOT_FOUND = "command.removeEntity.notFound",
      COMMAND_REMOVE_ENTITY_SUCCESS = "command.removeEntity.success",
      COMMAND_LIST_DESCRIPTION = "command.list.description",
      COMMAND_LIST_HEADER = "command.list.header",
      COMMAND_LIST_BLOCK = "command.list.block",
      COMMAND_LIST_UNKNOWN_BLOCK = "command.list.unknownBlock",
      COMMAND_LIST_ENTITY = "command.list.entity",
      COMMAND_LIST_UNKNOWN_ENTITY = "command.list.unknownEntity",
      COMMAND_FINDME_INFO = "command.findme.info";

  public static Map<String, Object> getDefaults() {
    return Map.ofEntries(
        entry(ERROR_USAGE, "&cUsage: %s"),
        entry(ERROR_PLAYER_ONLY, "&cOnly a player can use this command"),
        entry(ERROR_INTERRUPT, "&cSomething went wrong running this command. Check the logs for more info"),
        entry(ERROR_COMMAND_NOT_FOUND, "&cCommand not found. Try running &e/%s help &cfor help"),
        entry(ERROR_INVALID_INT, "&cNot an integer: %d"),
        entry(ERROR_INT_OUT_OF_RANGE, "&cInvalid integer: %d. Must be between %d and %d"),
        entry(ERROR_MANUAL_NO_COMMAND_FOUND, "&cNo manual entry found for %s"),
        entry(ERROR_NO_BLOCK_FOUND, "&cNot looking at any blocks"),
        entry(COMMAND_HELP_HEADER, "&7=-= &bSub-Commands for &a/%1$s&b (&e%2$d&b/&e%3$d&b) &7=-="),
        entry(COMMAND_HELP_ENTRY, "- &7/%1$s &b%2$s&r %3$s: %4$s"),
        entry(COMMAND_HELP_DESCRIPTION, "List all available sub-commands for &b/%s"),
        entry(COMMAND_RELOAD_FINISH, "&aFinished reloading configurations"),
        entry(COMMAND_RELOAD_DESCRIPTION, "Reload configurations and localizations for this plugin"),
        entry(COMMAND_ADD_BLOCK_SUCCESS, "&aAdded findable block %1$s &b#%2$d&a to &e%3$s&a"),
        entry(COMMAND_ADD_BLOCK_DESCRIPTION, "Add a new findable block"),
        entry(COMMAND_ADD_ENTITY_SUCCESS, "&aAdded findable entity %1$s &b#%2$d&a to &e%3$s"),
        entry(COMMAND_ADD_ENTITY_DESCRIPTION, "Add a new findable entity"),
        entry(COMMAND_ADD_CONFLICT, "&cThat is already part of collection &e%s"),
        entry(COMMAND_REMOVE_ANY_DESCRIPTION, "Remove a findable block or entity from its collection"),
        entry(COMMAND_REMOVE_ANY_NOT_FOUND, "&cNo findable found with that ID"),
        entry(COMMAND_REMOVE_ANY_BLOCK, "&aBlock %1$s &b#%2$d&a (&e%3$s&a) in &e%4$s&a at &b%5$d&a,&b%6$d&a,&b%7$d&a removed"),
        entry(COMMAND_REMOVE_ANY_ENTITY, "&aEntity %$1s &b#%$2d&a (&e%$3s&a) in &e%$4s&a at &b%5$.1f&a,&b%6$.1&a,&b%$7.1f&a removed"),
        entry(COMMAND_REMOVE_ANY_UNKNOWN_BLOCK, "&aUnknown block &b#%1$d&a (&e%2$s&a) removed"),
        entry(COMMAND_REMOVE_ANY_UNKNOWN_ENTITY, "&aUnknown entity &b#%1$d&a (&e%2$s&a) removed"),
        entry(COMMAND_REMOVE_BLOCK_DESCRIPTION, "Remove a findable block from its collection"),
        entry(COMMAND_REMOVE_BLOCK_NOT_FOUND, "&cSelected block is not part of any collection"),
        entry(COMMAND_REMOVE_BLOCK_SUCCESS, "&aBlock removed from collection"),
        entry(COMMAND_REMOVE_ENTITY_DESCRIPTION, "Remove a findable entity from its collection"),
        entry(COMMAND_REMOVE_ENTITY_NOT_FOUND, "&cSelected entity is not part of any collection"),
        entry(COMMAND_REMOVE_ENTITY_SUCCESS, "&aEntity removed from collection"),
        entry(COMMAND_LIST_DESCRIPTION, "List all findables and where they are"),
        entry(COMMAND_LIST_HEADER, "&7=-= &bList of Findables (&e%$1d&b/&e%$2d&b) &7=-="),
        entry(COMMAND_LIST_BLOCK, "- &b#%d&r (&e%s&r) %s in &e%s&r at &b%d&r,&b%d&r,&b%d"),
        entry(COMMAND_LIST_UNKNOWN_BLOCK, "- &b#%d&r (&e%s&r) &4<unknown block> in &4%s%r at &b%d&r,&b%d&r,&b%d"),
        entry(COMMAND_LIST_ENTITY, "- &b#%d&r (&e%s&r) %s in &e%s&r at &b%.1f&r,&b%.1f&r,&b%.1f"),
        entry(COMMAND_LIST_UNKNOWN_ENTITY, "- &b#%d&r (&e%s&r) &4<unknown entity>&r (&b%s&r)"),
        entry(COMMAND_FINDME_INFO, "&7=-= &bPlugin Information for &eFindMe &7=-=\n&aVersion: ???\n&aRun &b/findme help&a to view all commands\n&aFor more detailed information, check out the wiki: &ehttps://github.com/whizvox/FindMe/wiki"),
        entry(SetCommandHandler.TLK_UNKNOWN_SOUND, "&cUnknown sound: %s"),
        entry(SetCommandHandler.TLK_SET, "&aSet the value of &b%1$s&a to %2$s&r&a for %3$s"),
        entry(SetCommandHandler.TLK_CANNOT_UNSET, "&aCannot unset that property"),
        entry(SetCommandHandler.TLK_UNSET, "&aUnset the value of &b%1$s&a for %2$s"),
        entry(GetCommandHandler.TLK_SUCCESS, "&b[%1$s&r&b] &e%2$s&b: &r%3$s"),
        entry(SetDefaultCommandHandler.TLK_NO_CHANGE, "&cThat is already the default collection"),
        entry(SetDefaultCommandHandler.TLK_SUCCESS, "&aDefault collection has been changed to %s"),
        entry(CollectionsCommandHandler.TLK_HEADER, "&7=-= &bCollections &7=-="),
        entry(CollectionsCommandHandler.TLK_ENTRY, "- &b%1$s&r (&e%2$s&r)")
    );
  }

}
