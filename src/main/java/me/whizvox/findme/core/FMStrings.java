package me.whizvox.findme.core;

import me.whizvox.findme.command.HelpCommandHelper;
import me.whizvox.findme.command.ReloadCommandHandler;
import me.whizvox.findme.core.command.*;

import java.util.HashMap;
import java.util.Map;

public class FMStrings {

  public static final String
      ERR_USAGE = "error.usage",
      ERR_PLAYER_ONLY = "error.playerOnly",
      ERR_NO_PERMISSION = "error.noPermission",
      ERR_INTERRUPT = "error.interrupt",
      ERR_COMMAND_NOT_FOUND = "error.commandNotFound",
      ERR_INVALID_INT = "error.invalidInt",
      ERR_INT_OUT_OF_RANGE = "error.intOutOfRange",
      ERR_NO_BLOCK_FOUND = "error.noBlockFound",
      ERR_NO_ENTITY_FOUND = "error.noEntityFound",
      ERR_UNKNOWN_COLLECTION = "error.unknownCollection",
      ERR_UNKNOWN_FINDABLE = "error.unknownFindable",
      ERR_UNKNOWN_PLAYER = "error.unknownPlayer",
      ERR_UNKNOWN_WORLD = "error.unknownWorld",
      ERR_UNKNOWN_ENTITY = "error.invalidEntity",
      ERR_INVALID_ENUM = "error.invalidEnum",
      ERR_ALREADY_FOUND = "error.alreadyFound";

  public static Map<String, Object> getDefaults() {
    Map<String, Object> def = new HashMap<>();
    def.put(ERR_USAGE, "&cUsage: %s");
    def.put(ERR_PLAYER_ONLY, "&cOnly a player can use this command");
    def.put(ERR_INTERRUPT, "&cSomething went wrong running this command. Check the logs for more info");
    def.put(ERR_COMMAND_NOT_FOUND, "&cCommand not found. Try running &e/%s help &cfor help");
    def.put(ERR_INVALID_INT, "&cNot an integer: %d");
    def.put(ERR_INT_OUT_OF_RANGE, "&cInvalid integer: %d. Must be between %d and %d");
    def.put(ERR_NO_BLOCK_FOUND, "&cNot looking at any blocks");
    def.put(ERR_NO_ENTITY_FOUND, "&cNot looking at any entities");
    def.put(ERR_UNKNOWN_COLLECTION, "&cUnknown collection: %s");
    def.put(ERR_UNKNOWN_FINDABLE, "&cUnknown findable: %s");
    def.put(ERR_UNKNOWN_PLAYER, "&cUnknown player: %s");
    def.put(ERR_UNKNOWN_ENTITY, "&cUnknown entity ID: %s");
    def.put(ERR_INVALID_ENUM, "&cInvalid argument, must be one of [%s]");
    def.put(ERR_ALREADY_FOUND, "&cYou've already found this!");
    def.put(FindMeCommandDelegator.TLK_INFO, "&7=-= &bPlugin Information for &eFindMe &7=-=\n&aVersion: ???\n&aRun &b/findme help&a to view all commands\n&aFor more detailed information, check out the wiki: &ehttps://github.com/whizvox/FindMe/wiki");
    def.put(HelpCommandHelper.TLK_DESCRIPTION, "List all available sub-commands for &b/%s");
    def.put(HelpCommandHelper.TLK_HEADER, "&7=-= &bSub-Commands for &a/%1$s&b (&e%2$d&b/&e%3$d&b) &7=-=");
    def.put(HelpCommandHelper.TLK_ENTRY, "- &7/%1$s &b%2$s&r %3$s: %4$s");
    def.put(ReloadCommandHandler.TLK_DESCRIPTION, "Reload configurations and localizations for this plugin");
    def.put(ReloadCommandHandler.TLK_SUCCESS, "&aFinished reloading configurations");
    def.put(AddBlockCommandHandler.TLK_DESCRIPTION, "Add a new findable block");
    def.put(AddBlockCommandHandler.TLK_CONFLICT, "&cThat is already a findable block (&e#%1$d&c %2$s&r&c)");
    def.put(AddBlockCommandHandler.TLK_SUCCESS, "&aAdded findable block %1$s &b#%2$d&a to &e%3$s&a");
    def.put(AddEntityCommandHandler.TLK_DESCRIPTION, "Add a new findable entity");
    def.put(AddEntityCommandHandler.TLK_CONFLICT, "&cThat is already a findable entity (&e#%1$d&c %2$s&r&c)");
    def.put(AddEntityCommandHandler.TLK_SUCCESS, "&aAdded findable entity %1$s &b#%2$d&a to &e%3$s");
    def.put(CollectionsCommandHandler.TLK_DESCRIPTION, "List all collections");
    def.put(CollectionsCommandHandler.TLK_HEADER, "&7=-= &bCollections &7=-=");
    def.put(CollectionsCommandHandler.TLK_ENTRY, "- &b%1$s&r (&e%2$s&r)");
    def.put(CreateCommandHandler.TLK_DESCRIPTION, "Create a new collection");
    def.put(CreateCommandHandler.TLK_CONFLICT, "&cThere already exists a collection with that name");
    def.put(CreateCommandHandler.TLK_SUCCESS, "&aCreated collection %s");
    def.put(GetCommandHandler.TLK_DESCRIPTION, "Get the value of a collection's property");
    def.put(GetCommandHandler.TLK_SUCCESS, "&b[%1$s&r&b] &e%2$s&b: &r%3$s");
    def.put(ListCommandHandler.TLK_DESCRIPTION, "List all findables and where they are");
    def.put(ListCommandHandler.TLK_EMPTY, "&cThere is nothing to find...");
    def.put(ListCommandHandler.TLK_HEADER, "&7=-= &bList of Findables (&e%$1d&b/&e%$2d&b) &7=-=");
    def.put(ListCommandHandler.TLK_BLOCK, "- &b#%d&r (&e%s&r) %s in &e%s&r at &b%d&r,&b%d&r,&b%d");
    def.put(ListCommandHandler.TLK_UNKNOWN_BLOCK, "- &b#%d&r (&e%s&r) &4<unknown block> in &4%s%r at &b%d&r,&b%d&r,&b%d");
    def.put(ListCommandHandler.TLK_ENTITY, "- &b#%d&r (&e%s&r) %s in &e%s&r at &b%.1f&r,&b%.1f&r,&b%.1f");
    def.put(ListCommandHandler.TLK_UNKNOWN_ENTITY, "- &b#%d&r (&e%s&r) &4<unknown entity>&r (&b%s&r)");
    def.put(RemoveBlockCommandHandler.TLK_DESCRIPTION, "Remove a findable block from its collection");
    def.put(RemoveBlockCommandHandler.TLK_UNKNOWN, "&cSelected block is not part of any collection");
    def.put(RemoveBlockCommandHandler.TLK_SUCCESS, "&aBlock removed from collection");
    def.put(RemoveCollectionCommandHandler.TLK_DESCRIPTION, "Remove a collection and its findables");
    def.put(RemoveCollectionCommandHandler.TLK_SUCCESS, "&aRemoved collection %s");
    def.put(RemoveEntityCommandHandler.TLK_DESCRIPTION, "Remove a findable entity from its collection");
    def.put(RemoveEntityCommandHandler.TLK_NOT_FINDABLE, "&cSelected entity is not part of any collection");
    def.put(RemoveEntityCommandHandler.TLK_SUCCESS, "&aEntity removed from collection");
    def.put(RemoveFindableCommandHandler.TLK_DESCRIPTION, "Remove a findable from a collection");
    def.put(RemoveFindableCommandHandler.TLK_SUCCESS, "&aRemoved findable %s");
    def.put(RemovePlayerCommandHandler.TLK_DESCRIPTION, "Remove found items from a player's collection");
    def.put(RemovePlayerCommandHandler.TLK_DEFAULT_CONFLICT, "&cCannot remove the default collection");
    def.put(RemovePlayerCommandHandler.TLK_SUCCESS_PLAYER, "&aRemoved all findables collected by %s");
    def.put(RemovePlayerCommandHandler.TLK_SUCCESS_COLLECTION, "&aRemoved all findables from player's collection");
    def.put(RemovePlayerCommandHandler.TLK_SUCCESS_FINDABLE, "&aRemoved player's findable");
    def.put(SetCommandHandler.TLK_DESCRIPTION, "Set a collection's properties");
    def.put(SetCommandHandler.TLK_UNKNOWN_SOUND, "&cUnknown sound: %s");
    def.put(SetCommandHandler.TLK_SET, "&aSet the value of &b%1$s&a to %2$s&r&a for %3$s");
    def.put(SetCommandHandler.TLK_CANNOT_UNSET, "&cCannot unset that property");
    def.put(SetCommandHandler.TLK_UNSET, "&aUnset the value of &b%1$s&a for %2$s");
    def.put(SetDefaultCommandHandler.TLK_DESCRIPTION, "Set the default collection");
    def.put(SetDefaultCommandHandler.TLK_NO_CHANGE, "&cThat is already the default collection");
    def.put(SetDefaultCommandHandler.TLK_SUCCESS, "&aDefault collection has been changed to %s");
    def.put(StatsCommandHandler.TLK_DESCRIPTION, "View stats about a player's collections");
    def.put(StatsCommandHandler.TLK_NO_COLLECTIONS, "&cNo collections...");
    def.put(StatsCommandHandler.TLK_HEADER, "&7=-= &bStats for &e%s &7=-=");
    def.put(StatsCommandHandler.TLK_ENTRY, "- &b%1$s&r: &e%2$d&r/&e%3$d&r (%4$.1f%%)");
    return def;
  }

}
