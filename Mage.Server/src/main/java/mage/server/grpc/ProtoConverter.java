package mage.server.grpc;

import mage.ObjectColor;
import mage.abilities.icon.CardIcon;
import mage.abilities.icon.CardIconImpl;
import mage.abilities.icon.CardIconType;
import mage.cards.FrameStyle;
import mage.cards.decks.DeckCardLists;
import mage.constants.*;
import mage.counters.Counter;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.proto.*;
import mage.view.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for converting between Java view objects and Protocol Buffer messages.
 * All conversions are 1:1 mappings matching the proto definitions exactly.
 */
public final class ProtoConverter {

    private ProtoConverter() {}

    // ============================================================================
    // UUID Conversions
    // ============================================================================

    public static String toProtoUuid(UUID uuid) {
        return uuid != null ? uuid.toString() : "";
    }

    public static UUID fromProtoUuid(String uuid) {
        return uuid != null && !uuid.isEmpty() ? UUID.fromString(uuid) : null;
    }

    // ============================================================================
    // ManaType Conversions (mage.constants.ManaType <-> mage.proto.ManaType)
    // ============================================================================

    public static mage.constants.ManaType toManaType(mage.proto.ManaType protoType) {
        switch (protoType) {
            case MANA_TYPE_BLACK: return mage.constants.ManaType.BLACK;
            case MANA_TYPE_BLUE: return mage.constants.ManaType.BLUE;
            case MANA_TYPE_GREEN: return mage.constants.ManaType.GREEN;
            case MANA_TYPE_RED: return mage.constants.ManaType.RED;
            case MANA_TYPE_WHITE: return mage.constants.ManaType.WHITE;
            case MANA_TYPE_GENERIC: return mage.constants.ManaType.GENERIC;
            case MANA_TYPE_COLORLESS: return mage.constants.ManaType.COLORLESS;
            default: return mage.constants.ManaType.COLORLESS;
        }
    }

    public static mage.proto.ManaType toProtoManaType(mage.constants.ManaType type) {
        if (type == null) return mage.proto.ManaType.MANA_TYPE_UNSPECIFIED;
        switch (type) {
            case BLACK: return mage.proto.ManaType.MANA_TYPE_BLACK;
            case BLUE: return mage.proto.ManaType.MANA_TYPE_BLUE;
            case GREEN: return mage.proto.ManaType.MANA_TYPE_GREEN;
            case RED: return mage.proto.ManaType.MANA_TYPE_RED;
            case WHITE: return mage.proto.ManaType.MANA_TYPE_WHITE;
            case GENERIC: return mage.proto.ManaType.MANA_TYPE_GENERIC;
            case COLORLESS: return mage.proto.ManaType.MANA_TYPE_COLORLESS;
            default: return mage.proto.ManaType.MANA_TYPE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // PlayerType Conversions (mage.players.PlayerType <-> mage.proto.PlayerType)
    // ============================================================================

    public static PlayerType toPlayerType(mage.proto.PlayerType protoType) {
        switch (protoType) {
            case PLAYER_TYPE_HUMAN: return PlayerType.HUMAN;
            case PLAYER_TYPE_COMPUTER_DRAFT_BOT: return PlayerType.COMPUTER_DRAFT_BOT;
            case PLAYER_TYPE_COMPUTER_MONTE_CARLO: return PlayerType.COMPUTER_MONTE_CARLO;
            case PLAYER_TYPE_COMPUTER_MAD: return PlayerType.COMPUTER_MAD;
            default: return PlayerType.HUMAN;
        }
    }

    public static mage.proto.PlayerType toProtoPlayerType(PlayerType type) {
        if (type == null) return mage.proto.PlayerType.PLAYER_TYPE_UNSPECIFIED;
        switch (type) {
            case HUMAN: return mage.proto.PlayerType.PLAYER_TYPE_HUMAN;
            case COMPUTER_DRAFT_BOT: return mage.proto.PlayerType.PLAYER_TYPE_COMPUTER_DRAFT_BOT;
            case COMPUTER_MONTE_CARLO: return mage.proto.PlayerType.PLAYER_TYPE_COMPUTER_MONTE_CARLO;
            case COMPUTER_MAD: return mage.proto.PlayerType.PLAYER_TYPE_COMPUTER_MAD;
            default: return mage.proto.PlayerType.PLAYER_TYPE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // PlayerAction Conversions (mage.constants.PlayerAction <-> mage.proto.PlayerAction)
    // ============================================================================

    public static mage.constants.PlayerAction toPlayerAction(mage.proto.PlayerAction protoAction) {
        switch (protoAction) {
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_MY_NEXT_TURN: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_MY_NEXT_TURN;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_TURN_END_STEP: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_TURN_END_STEP;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_MAIN_PHASE: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_NEXT_MAIN_PHASE;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_TURN: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_NEXT_TURN;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_TURN_SKIP_STACK: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_NEXT_TURN_SKIP_STACK;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_STACK_RESOLVED: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_STACK_RESOLVED;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_END_STEP_BEFORE_MY_NEXT_TURN: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_END_STEP_BEFORE_MY_NEXT_TURN;
            case PLAYER_ACTION_PASS_PRIORITY_CANCEL_ALL_ACTIONS: return mage.constants.PlayerAction.PASS_PRIORITY_CANCEL_ALL_ACTIONS;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_ABILITY_FIRST: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_ABILITY_FIRST;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_NAME_FIRST: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_NAME_FIRST;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_ABILITY_LAST: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_ABILITY_LAST;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_NAME_LAST: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_NAME_LAST;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_RESET_ALL: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_RESET_ALL;
            case PLAYER_ACTION_ROLLBACK_TURNS: return mage.constants.PlayerAction.ROLLBACK_TURNS;
            case PLAYER_ACTION_UNDO: return mage.constants.PlayerAction.UNDO;
            case PLAYER_ACTION_CONCEDE: return mage.constants.PlayerAction.CONCEDE;
            case PLAYER_ACTION_MANA_AUTO_PAYMENT_ON: return mage.constants.PlayerAction.MANA_AUTO_PAYMENT_ON;
            case PLAYER_ACTION_MANA_AUTO_PAYMENT_OFF: return mage.constants.PlayerAction.MANA_AUTO_PAYMENT_OFF;
            case PLAYER_ACTION_MANA_AUTO_PAYMENT_RESTRICTED_ON: return mage.constants.PlayerAction.MANA_AUTO_PAYMENT_RESTRICTED_ON;
            case PLAYER_ACTION_MANA_AUTO_PAYMENT_RESTRICTED_OFF: return mage.constants.PlayerAction.MANA_AUTO_PAYMENT_RESTRICTED_OFF;
            case PLAYER_ACTION_USE_FIRST_MANA_ABILITY_ON: return mage.constants.PlayerAction.USE_FIRST_MANA_ABILITY_ON;
            case PLAYER_ACTION_USE_FIRST_MANA_ABILITY_OFF: return mage.constants.PlayerAction.USE_FIRST_MANA_ABILITY_OFF;
            case PLAYER_ACTION_RESET_AUTO_SELECT_REPLACEMENT_EFFECTS: return mage.constants.PlayerAction.RESET_AUTO_SELECT_REPLACEMENT_EFFECTS;
            case PLAYER_ACTION_REVOKE_PERMISSIONS_TO_SEE_HAND_CARDS: return mage.constants.PlayerAction.REVOKE_PERMISSIONS_TO_SEE_HAND_CARDS;
            case PLAYER_ACTION_REQUEST_PERMISSION_TO_SEE_HAND_CARDS: return mage.constants.PlayerAction.REQUEST_PERMISSION_TO_SEE_HAND_CARDS;
            case PLAYER_ACTION_REQUEST_PERMISSION_TO_ROLLBACK_TURN: return mage.constants.PlayerAction.REQUEST_PERMISSION_TO_ROLLBACK_TURN;
            case PLAYER_ACTION_ADD_PERMISSION_TO_SEE_HAND_CARDS: return mage.constants.PlayerAction.ADD_PERMISSION_TO_SEE_HAND_CARDS;
            case PLAYER_ACTION_ADD_PERMISSION_TO_ROLLBACK_TURN: return mage.constants.PlayerAction.ADD_PERMISSION_TO_ROLLBACK_TURN;
            case PLAYER_ACTION_DENY_PERMISSION_TO_ROLLBACK_TURN: return mage.constants.PlayerAction.DENY_PERMISSION_TO_ROLLBACK_TURN;
            case PLAYER_ACTION_PERMISSION_REQUESTS_ALLOWED_ON: return mage.constants.PlayerAction.PERMISSION_REQUESTS_ALLOWED_ON;
            case PLAYER_ACTION_PERMISSION_REQUESTS_ALLOWED_OFF: return mage.constants.PlayerAction.PERMISSION_REQUESTS_ALLOWED_OFF;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_ID_YES: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_ID_YES;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_ID_NO: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_ID_NO;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_TEXT_YES: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_TEXT_YES;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_TEXT_NO: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_TEXT_NO;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_RESET_ALL: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_RESET_ALL;
            case PLAYER_ACTION_CLIENT_DOWNLOAD_SYMBOLS: return mage.constants.PlayerAction.CLIENT_DOWNLOAD_SYMBOLS;
            case PLAYER_ACTION_CLIENT_QUIT_TOURNAMENT: return mage.constants.PlayerAction.CLIENT_QUIT_TOURNAMENT;
            case PLAYER_ACTION_CLIENT_QUIT_DRAFT_TOURNAMENT: return mage.constants.PlayerAction.CLIENT_QUIT_DRAFT_TOURNAMENT;
            case PLAYER_ACTION_CLIENT_CONCEDE_GAME: return mage.constants.PlayerAction.CLIENT_CONCEDE_GAME;
            case PLAYER_ACTION_CLIENT_CONCEDE_MATCH: return mage.constants.PlayerAction.CLIENT_CONCEDE_MATCH;
            case PLAYER_ACTION_CLIENT_STOP_WATCHING: return mage.constants.PlayerAction.CLIENT_STOP_WATCHING;
            case PLAYER_ACTION_CLIENT_DISCONNECT_FULL: return mage.constants.PlayerAction.CLIENT_DISCONNECT_FULL;
            case PLAYER_ACTION_CLIENT_DISCONNECT_KEEP_GAMES: return mage.constants.PlayerAction.CLIENT_DISCONNECT_KEEP_GAMES;
            case PLAYER_ACTION_CLIENT_EXIT_FULL: return mage.constants.PlayerAction.CLIENT_EXIT_FULL;
            case PLAYER_ACTION_CLIENT_EXIT_KEEP_GAMES: return mage.constants.PlayerAction.CLIENT_EXIT_KEEP_GAMES;
            case PLAYER_ACTION_CLIENT_REMOVE_TABLE: return mage.constants.PlayerAction.CLIENT_REMOVE_TABLE;
            case PLAYER_ACTION_CLIENT_DOWNLOAD_CARD_IMAGES: return mage.constants.PlayerAction.CLIENT_DOWNLOAD_CARD_IMAGES;
            case PLAYER_ACTION_CLIENT_RECONNECT: return mage.constants.PlayerAction.CLIENT_RECONNECT;
            case PLAYER_ACTION_CLIENT_REPLAY_ACTION: return mage.constants.PlayerAction.CLIENT_REPLAY_ACTION;
            case PLAYER_ACTION_HOLD_PRIORITY: return mage.constants.PlayerAction.HOLD_PRIORITY;
            case PLAYER_ACTION_UNHOLD_PRIORITY: return mage.constants.PlayerAction.UNHOLD_PRIORITY;
            case PLAYER_ACTION_VIEW_LIMITED_DECK: return mage.constants.PlayerAction.VIEW_LIMITED_DECK;
            case PLAYER_ACTION_VIEW_SIDEBOARD: return mage.constants.PlayerAction.VIEW_SIDEBOARD;
            case PLAYER_ACTION_TOGGLE_RECORD_MACRO: return mage.constants.PlayerAction.TOGGLE_RECORD_MACRO;
            default: return null;
        }
    }

    public static mage.proto.PlayerAction toProtoPlayerAction(mage.constants.PlayerAction action) {
        if (action == null) return mage.proto.PlayerAction.PLAYER_ACTION_UNSPECIFIED;
        switch (action) {
            case PASS_PRIORITY_UNTIL_MY_NEXT_TURN: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_UNTIL_MY_NEXT_TURN;
            case PASS_PRIORITY_UNTIL_TURN_END_STEP: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_UNTIL_TURN_END_STEP;
            case PASS_PRIORITY_UNTIL_NEXT_MAIN_PHASE: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_MAIN_PHASE;
            case PASS_PRIORITY_UNTIL_NEXT_TURN: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_TURN;
            case PASS_PRIORITY_UNTIL_NEXT_TURN_SKIP_STACK: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_TURN_SKIP_STACK;
            case PASS_PRIORITY_UNTIL_STACK_RESOLVED: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_UNTIL_STACK_RESOLVED;
            case PASS_PRIORITY_UNTIL_END_STEP_BEFORE_MY_NEXT_TURN: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_UNTIL_END_STEP_BEFORE_MY_NEXT_TURN;
            case PASS_PRIORITY_CANCEL_ALL_ACTIONS: return mage.proto.PlayerAction.PLAYER_ACTION_PASS_PRIORITY_CANCEL_ALL_ACTIONS;
            case TRIGGER_AUTO_ORDER_ABILITY_FIRST: return mage.proto.PlayerAction.PLAYER_ACTION_TRIGGER_AUTO_ORDER_ABILITY_FIRST;
            case TRIGGER_AUTO_ORDER_NAME_FIRST: return mage.proto.PlayerAction.PLAYER_ACTION_TRIGGER_AUTO_ORDER_NAME_FIRST;
            case TRIGGER_AUTO_ORDER_ABILITY_LAST: return mage.proto.PlayerAction.PLAYER_ACTION_TRIGGER_AUTO_ORDER_ABILITY_LAST;
            case TRIGGER_AUTO_ORDER_NAME_LAST: return mage.proto.PlayerAction.PLAYER_ACTION_TRIGGER_AUTO_ORDER_NAME_LAST;
            case TRIGGER_AUTO_ORDER_RESET_ALL: return mage.proto.PlayerAction.PLAYER_ACTION_TRIGGER_AUTO_ORDER_RESET_ALL;
            case ROLLBACK_TURNS: return mage.proto.PlayerAction.PLAYER_ACTION_ROLLBACK_TURNS;
            case UNDO: return mage.proto.PlayerAction.PLAYER_ACTION_UNDO;
            case CONCEDE: return mage.proto.PlayerAction.PLAYER_ACTION_CONCEDE;
            case MANA_AUTO_PAYMENT_ON: return mage.proto.PlayerAction.PLAYER_ACTION_MANA_AUTO_PAYMENT_ON;
            case MANA_AUTO_PAYMENT_OFF: return mage.proto.PlayerAction.PLAYER_ACTION_MANA_AUTO_PAYMENT_OFF;
            case MANA_AUTO_PAYMENT_RESTRICTED_ON: return mage.proto.PlayerAction.PLAYER_ACTION_MANA_AUTO_PAYMENT_RESTRICTED_ON;
            case MANA_AUTO_PAYMENT_RESTRICTED_OFF: return mage.proto.PlayerAction.PLAYER_ACTION_MANA_AUTO_PAYMENT_RESTRICTED_OFF;
            case USE_FIRST_MANA_ABILITY_ON: return mage.proto.PlayerAction.PLAYER_ACTION_USE_FIRST_MANA_ABILITY_ON;
            case USE_FIRST_MANA_ABILITY_OFF: return mage.proto.PlayerAction.PLAYER_ACTION_USE_FIRST_MANA_ABILITY_OFF;
            case RESET_AUTO_SELECT_REPLACEMENT_EFFECTS: return mage.proto.PlayerAction.PLAYER_ACTION_RESET_AUTO_SELECT_REPLACEMENT_EFFECTS;
            case REVOKE_PERMISSIONS_TO_SEE_HAND_CARDS: return mage.proto.PlayerAction.PLAYER_ACTION_REVOKE_PERMISSIONS_TO_SEE_HAND_CARDS;
            case REQUEST_PERMISSION_TO_SEE_HAND_CARDS: return mage.proto.PlayerAction.PLAYER_ACTION_REQUEST_PERMISSION_TO_SEE_HAND_CARDS;
            case REQUEST_PERMISSION_TO_ROLLBACK_TURN: return mage.proto.PlayerAction.PLAYER_ACTION_REQUEST_PERMISSION_TO_ROLLBACK_TURN;
            case ADD_PERMISSION_TO_SEE_HAND_CARDS: return mage.proto.PlayerAction.PLAYER_ACTION_ADD_PERMISSION_TO_SEE_HAND_CARDS;
            case ADD_PERMISSION_TO_ROLLBACK_TURN: return mage.proto.PlayerAction.PLAYER_ACTION_ADD_PERMISSION_TO_ROLLBACK_TURN;
            case DENY_PERMISSION_TO_ROLLBACK_TURN: return mage.proto.PlayerAction.PLAYER_ACTION_DENY_PERMISSION_TO_ROLLBACK_TURN;
            case PERMISSION_REQUESTS_ALLOWED_ON: return mage.proto.PlayerAction.PLAYER_ACTION_PERMISSION_REQUESTS_ALLOWED_ON;
            case PERMISSION_REQUESTS_ALLOWED_OFF: return mage.proto.PlayerAction.PLAYER_ACTION_PERMISSION_REQUESTS_ALLOWED_OFF;
            case REQUEST_AUTO_ANSWER_ID_YES: return mage.proto.PlayerAction.PLAYER_ACTION_REQUEST_AUTO_ANSWER_ID_YES;
            case REQUEST_AUTO_ANSWER_ID_NO: return mage.proto.PlayerAction.PLAYER_ACTION_REQUEST_AUTO_ANSWER_ID_NO;
            case REQUEST_AUTO_ANSWER_TEXT_YES: return mage.proto.PlayerAction.PLAYER_ACTION_REQUEST_AUTO_ANSWER_TEXT_YES;
            case REQUEST_AUTO_ANSWER_TEXT_NO: return mage.proto.PlayerAction.PLAYER_ACTION_REQUEST_AUTO_ANSWER_TEXT_NO;
            case REQUEST_AUTO_ANSWER_RESET_ALL: return mage.proto.PlayerAction.PLAYER_ACTION_REQUEST_AUTO_ANSWER_RESET_ALL;
            case CLIENT_DOWNLOAD_SYMBOLS: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_DOWNLOAD_SYMBOLS;
            case CLIENT_QUIT_TOURNAMENT: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_QUIT_TOURNAMENT;
            case CLIENT_QUIT_DRAFT_TOURNAMENT: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_QUIT_DRAFT_TOURNAMENT;
            case CLIENT_CONCEDE_GAME: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_CONCEDE_GAME;
            case CLIENT_CONCEDE_MATCH: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_CONCEDE_MATCH;
            case CLIENT_STOP_WATCHING: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_STOP_WATCHING;
            case CLIENT_DISCONNECT_FULL: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_DISCONNECT_FULL;
            case CLIENT_DISCONNECT_KEEP_GAMES: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_DISCONNECT_KEEP_GAMES;
            case CLIENT_EXIT_FULL: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_EXIT_FULL;
            case CLIENT_EXIT_KEEP_GAMES: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_EXIT_KEEP_GAMES;
            case CLIENT_REMOVE_TABLE: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_REMOVE_TABLE;
            case CLIENT_DOWNLOAD_CARD_IMAGES: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_DOWNLOAD_CARD_IMAGES;
            case CLIENT_RECONNECT: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_RECONNECT;
            case CLIENT_REPLAY_ACTION: return mage.proto.PlayerAction.PLAYER_ACTION_CLIENT_REPLAY_ACTION;
            case HOLD_PRIORITY: return mage.proto.PlayerAction.PLAYER_ACTION_HOLD_PRIORITY;
            case UNHOLD_PRIORITY: return mage.proto.PlayerAction.PLAYER_ACTION_UNHOLD_PRIORITY;
            case VIEW_LIMITED_DECK: return mage.proto.PlayerAction.PLAYER_ACTION_VIEW_LIMITED_DECK;
            case VIEW_SIDEBOARD: return mage.proto.PlayerAction.PLAYER_ACTION_VIEW_SIDEBOARD;
            case TOGGLE_RECORD_MACRO: return mage.proto.PlayerAction.PLAYER_ACTION_TOGGLE_RECORD_MACRO;
            default: return mage.proto.PlayerAction.PLAYER_ACTION_UNSPECIFIED;
        }
    }

    // ============================================================================
    // SkillLevel Conversions
    // ============================================================================

    public static SkillLevel toSkillLevel(mage.proto.SkillLevel protoLevel) {
        switch (protoLevel) {
            case SKILL_LEVEL_BEGINNER: return SkillLevel.BEGINNER;
            case SKILL_LEVEL_CASUAL: return SkillLevel.CASUAL;
            case SKILL_LEVEL_SERIOUS: return SkillLevel.SERIOUS;
            default: return SkillLevel.CASUAL;
        }
    }

    public static mage.proto.SkillLevel toProtoSkillLevel(SkillLevel level) {
        if (level == null) return mage.proto.SkillLevel.SKILL_LEVEL_UNSPECIFIED;
        switch (level) {
            case BEGINNER: return mage.proto.SkillLevel.SKILL_LEVEL_BEGINNER;
            case CASUAL: return mage.proto.SkillLevel.SKILL_LEVEL_CASUAL;
            case SERIOUS: return mage.proto.SkillLevel.SKILL_LEVEL_SERIOUS;
            default: return mage.proto.SkillLevel.SKILL_LEVEL_UNSPECIFIED;
        }
    }

    // ============================================================================
    // Zone Conversions
    // ============================================================================

    public static Zone toZone(mage.proto.Zone protoZone) {
        switch (protoZone) {
            case ZONE_HAND: return Zone.HAND;
            case ZONE_GRAVEYARD: return Zone.GRAVEYARD;
            case ZONE_LIBRARY: return Zone.LIBRARY;
            case ZONE_BATTLEFIELD: return Zone.BATTLEFIELD;
            case ZONE_STACK: return Zone.STACK;
            case ZONE_EXILED: return Zone.EXILED;
            case ZONE_ALL: return Zone.ALL;
            case ZONE_OUTSIDE: return Zone.OUTSIDE;
            case ZONE_COMMAND: return Zone.COMMAND;
            default: return null;
        }
    }

    public static mage.proto.Zone toProtoZone(Zone zone) {
        if (zone == null) return mage.proto.Zone.ZONE_UNSPECIFIED;
        switch (zone) {
            case HAND: return mage.proto.Zone.ZONE_HAND;
            case GRAVEYARD: return mage.proto.Zone.ZONE_GRAVEYARD;
            case LIBRARY: return mage.proto.Zone.ZONE_LIBRARY;
            case BATTLEFIELD: return mage.proto.Zone.ZONE_BATTLEFIELD;
            case STACK: return mage.proto.Zone.ZONE_STACK;
            case EXILED: return mage.proto.Zone.ZONE_EXILED;
            case ALL: return mage.proto.Zone.ZONE_ALL;
            case OUTSIDE: return mage.proto.Zone.ZONE_OUTSIDE;
            case COMMAND: return mage.proto.Zone.ZONE_COMMAND;
            default: return mage.proto.Zone.ZONE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // CardType Conversions
    // ============================================================================

    public static CardType toCardType(mage.proto.CardType protoType) {
        switch (protoType) {
            case CARD_TYPE_ARTIFACT: return CardType.ARTIFACT;
            case CARD_TYPE_BATTLE: return CardType.BATTLE;
            case CARD_TYPE_CONSPIRACY: return CardType.CONSPIRACY;
            case CARD_TYPE_CREATURE: return CardType.CREATURE;
            case CARD_TYPE_DUNGEON: return CardType.DUNGEON;
            case CARD_TYPE_ENCHANTMENT: return CardType.ENCHANTMENT;
            case CARD_TYPE_INSTANT: return CardType.INSTANT;
            case CARD_TYPE_LAND: return CardType.LAND;
            case CARD_TYPE_PHENOMENON: return CardType.PHENOMENON;
            case CARD_TYPE_PLANE: return CardType.PLANE;
            case CARD_TYPE_PLANESWALKER: return CardType.PLANESWALKER;
            case CARD_TYPE_SCHEME: return CardType.SCHEME;
            case CARD_TYPE_SORCERY: return CardType.SORCERY;
            case CARD_TYPE_KINDRED: return CardType.KINDRED;
            case CARD_TYPE_VANGUARD: return CardType.VANGUARD;
            default: return null;
        }
    }

    public static mage.proto.CardType toProtoCardType(CardType type) {
        if (type == null) return mage.proto.CardType.CARD_TYPE_UNSPECIFIED;
        switch (type) {
            case ARTIFACT: return mage.proto.CardType.CARD_TYPE_ARTIFACT;
            case BATTLE: return mage.proto.CardType.CARD_TYPE_BATTLE;
            case CONSPIRACY: return mage.proto.CardType.CARD_TYPE_CONSPIRACY;
            case CREATURE: return mage.proto.CardType.CARD_TYPE_CREATURE;
            case DUNGEON: return mage.proto.CardType.CARD_TYPE_DUNGEON;
            case ENCHANTMENT: return mage.proto.CardType.CARD_TYPE_ENCHANTMENT;
            case INSTANT: return mage.proto.CardType.CARD_TYPE_INSTANT;
            case LAND: return mage.proto.CardType.CARD_TYPE_LAND;
            case PHENOMENON: return mage.proto.CardType.CARD_TYPE_PHENOMENON;
            case PLANE: return mage.proto.CardType.CARD_TYPE_PLANE;
            case PLANESWALKER: return mage.proto.CardType.CARD_TYPE_PLANESWALKER;
            case SCHEME: return mage.proto.CardType.CARD_TYPE_SCHEME;
            case SORCERY: return mage.proto.CardType.CARD_TYPE_SORCERY;
            case KINDRED: return mage.proto.CardType.CARD_TYPE_KINDRED;
            case VANGUARD: return mage.proto.CardType.CARD_TYPE_VANGUARD;
            default: return mage.proto.CardType.CARD_TYPE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // SuperType Conversions
    // ============================================================================

    public static SuperType toSuperType(mage.proto.SuperType protoType) {
        switch (protoType) {
            case SUPER_TYPE_BASIC: return SuperType.BASIC;
            case SUPER_TYPE_ELITE: return SuperType.ELITE;
            case SUPER_TYPE_LEGENDARY: return SuperType.LEGENDARY;
            case SUPER_TYPE_ONGOING: return SuperType.ONGOING;
            case SUPER_TYPE_SNOW: return SuperType.SNOW;
            case SUPER_TYPE_WORLD: return SuperType.WORLD;
            default: return null;
        }
    }

    public static mage.proto.SuperType toProtoSuperType(SuperType type) {
        if (type == null) return mage.proto.SuperType.SUPER_TYPE_UNSPECIFIED;
        switch (type) {
            case BASIC: return mage.proto.SuperType.SUPER_TYPE_BASIC;
            case ELITE: return mage.proto.SuperType.SUPER_TYPE_ELITE;
            case LEGENDARY: return mage.proto.SuperType.SUPER_TYPE_LEGENDARY;
            case ONGOING: return mage.proto.SuperType.SUPER_TYPE_ONGOING;
            case SNOW: return mage.proto.SuperType.SUPER_TYPE_SNOW;
            case WORLD: return mage.proto.SuperType.SUPER_TYPE_WORLD;
            default: return mage.proto.SuperType.SUPER_TYPE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // Rarity Conversions
    // ============================================================================

    public static Rarity toRarity(mage.proto.Rarity protoRarity) {
        switch (protoRarity) {
            case RARITY_LAND: return Rarity.LAND;
            case RARITY_COMMON: return Rarity.COMMON;
            case RARITY_UNCOMMON: return Rarity.UNCOMMON;
            case RARITY_RARE: return Rarity.RARE;
            case RARITY_MYTHIC: return Rarity.MYTHIC;
            case RARITY_SPECIAL: return Rarity.SPECIAL;
            case RARITY_BONUS: return Rarity.BONUS;
            default: return null;
        }
    }

    public static mage.proto.Rarity toProtoRarity(Rarity rarity) {
        if (rarity == null) return mage.proto.Rarity.RARITY_UNSPECIFIED;
        switch (rarity) {
            case LAND: return mage.proto.Rarity.RARITY_LAND;
            case COMMON: return mage.proto.Rarity.RARITY_COMMON;
            case UNCOMMON: return mage.proto.Rarity.RARITY_UNCOMMON;
            case RARE: return mage.proto.Rarity.RARITY_RARE;
            case MYTHIC: return mage.proto.Rarity.RARITY_MYTHIC;
            case SPECIAL: return mage.proto.Rarity.RARITY_SPECIAL;
            case BONUS: return mage.proto.Rarity.RARITY_BONUS;
            default: return mage.proto.Rarity.RARITY_UNSPECIFIED;
        }
    }

    // ============================================================================
    // TableState Conversions
    // ============================================================================

    public static TableState toTableState(mage.proto.TableState protoState) {
        switch (protoState) {
            case TABLE_STATE_WAITING: return TableState.WAITING;
            case TABLE_STATE_READY_TO_START: return TableState.READY_TO_START;
            case TABLE_STATE_STARTING: return TableState.STARTING;
            case TABLE_STATE_DRAFTING: return TableState.DRAFTING;
            case TABLE_STATE_CONSTRUCTING: return TableState.CONSTRUCTING;
            case TABLE_STATE_DUELING: return TableState.DUELING;
            case TABLE_STATE_SIDEBOARDING: return TableState.SIDEBOARDING;
            case TABLE_STATE_FINISHED: return TableState.FINISHED;
            default: return null;
        }
    }

    public static mage.proto.TableState toProtoTableState(TableState state) {
        if (state == null) return mage.proto.TableState.TABLE_STATE_UNSPECIFIED;
        switch (state) {
            case WAITING: return mage.proto.TableState.TABLE_STATE_WAITING;
            case READY_TO_START: return mage.proto.TableState.TABLE_STATE_READY_TO_START;
            case STARTING: return mage.proto.TableState.TABLE_STATE_STARTING;
            case DRAFTING: return mage.proto.TableState.TABLE_STATE_DRAFTING;
            case CONSTRUCTING: return mage.proto.TableState.TABLE_STATE_CONSTRUCTING;
            case DUELING: return mage.proto.TableState.TABLE_STATE_DUELING;
            case SIDEBOARDING: return mage.proto.TableState.TABLE_STATE_SIDEBOARDING;
            case FINISHED: return mage.proto.TableState.TABLE_STATE_FINISHED;
            default: return mage.proto.TableState.TABLE_STATE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // PhaseStep Conversions
    // ============================================================================

    public static PhaseStep toPhaseStep(mage.proto.PhaseStep protoStep) {
        switch (protoStep) {
            case PHASE_STEP_UNTAP: return PhaseStep.UNTAP;
            case PHASE_STEP_UPKEEP: return PhaseStep.UPKEEP;
            case PHASE_STEP_DRAW: return PhaseStep.DRAW;
            case PHASE_STEP_PRECOMBAT_MAIN: return PhaseStep.PRECOMBAT_MAIN;
            case PHASE_STEP_BEGIN_COMBAT: return PhaseStep.BEGIN_COMBAT;
            case PHASE_STEP_DECLARE_ATTACKERS: return PhaseStep.DECLARE_ATTACKERS;
            case PHASE_STEP_DECLARE_BLOCKERS: return PhaseStep.DECLARE_BLOCKERS;
            case PHASE_STEP_FIRST_COMBAT_DAMAGE: return PhaseStep.FIRST_COMBAT_DAMAGE;
            case PHASE_STEP_COMBAT_DAMAGE: return PhaseStep.COMBAT_DAMAGE;
            case PHASE_STEP_END_COMBAT: return PhaseStep.END_COMBAT;
            case PHASE_STEP_POSTCOMBAT_MAIN: return PhaseStep.POSTCOMBAT_MAIN;
            case PHASE_STEP_END_TURN: return PhaseStep.END_TURN;
            case PHASE_STEP_CLEANUP: return PhaseStep.CLEANUP;
            default: return null;
        }
    }

    public static mage.proto.PhaseStep toProtoPhaseStep(PhaseStep step) {
        if (step == null) return mage.proto.PhaseStep.PHASE_STEP_UNSPECIFIED;
        switch (step) {
            case UNTAP: return mage.proto.PhaseStep.PHASE_STEP_UNTAP;
            case UPKEEP: return mage.proto.PhaseStep.PHASE_STEP_UPKEEP;
            case DRAW: return mage.proto.PhaseStep.PHASE_STEP_DRAW;
            case PRECOMBAT_MAIN: return mage.proto.PhaseStep.PHASE_STEP_PRECOMBAT_MAIN;
            case BEGIN_COMBAT: return mage.proto.PhaseStep.PHASE_STEP_BEGIN_COMBAT;
            case DECLARE_ATTACKERS: return mage.proto.PhaseStep.PHASE_STEP_DECLARE_ATTACKERS;
            case DECLARE_BLOCKERS: return mage.proto.PhaseStep.PHASE_STEP_DECLARE_BLOCKERS;
            case FIRST_COMBAT_DAMAGE: return mage.proto.PhaseStep.PHASE_STEP_FIRST_COMBAT_DAMAGE;
            case COMBAT_DAMAGE: return mage.proto.PhaseStep.PHASE_STEP_COMBAT_DAMAGE;
            case END_COMBAT: return mage.proto.PhaseStep.PHASE_STEP_END_COMBAT;
            case POSTCOMBAT_MAIN: return mage.proto.PhaseStep.PHASE_STEP_POSTCOMBAT_MAIN;
            case END_TURN: return mage.proto.PhaseStep.PHASE_STEP_END_TURN;
            case CLEANUP: return mage.proto.PhaseStep.PHASE_STEP_CLEANUP;
            default: return mage.proto.PhaseStep.PHASE_STEP_UNSPECIFIED;
        }
    }

    // ============================================================================
    // FrameStyle Conversions
    // ============================================================================

    public static FrameStyle toFrameStyle(mage.proto.FrameStyle protoStyle) {
        switch (protoStyle) {
            case FRAME_STYLE_M15_NORMAL: return FrameStyle.M15_NORMAL;
            case FRAME_STYLE_BFZ_FULL_ART_BASIC: return FrameStyle.BFZ_FULL_ART_BASIC;
            case FRAME_STYLE_KLD_INVENTION: return FrameStyle.KLD_INVENTION;
            case FRAME_STYLE_ZEN_FULL_ART_BASIC: return FrameStyle.ZEN_FULL_ART_BASIC;
            case FRAME_STYLE_MPRP_FULL_ART_BASIC: return FrameStyle.MPRP_FULL_ART_BASIC;
            case FRAME_STYLE_MPOP_FULL_ART_BASIC: return FrameStyle.MPOP_FULL_ART_BASIC;
            case FRAME_STYLE_UNH_FULL_ART_BASIC: return FrameStyle.UNH_FULL_ART_BASIC;
            case FRAME_STYLE_UGL_FULL_ART_BASIC: return FrameStyle.UGL_FULL_ART_BASIC;
            case FRAME_STYLE_UST_FULL_ART_BASIC: return FrameStyle.UST_FULL_ART_BASIC;
            case FRAME_STYLE_ANA_FULL_ART_BASIC: return FrameStyle.ANA_FULL_ART_BASIC;
            case FRAME_STYLE_LEA_ORIGINAL_DUAL_LAND_ART_BASIC: return FrameStyle.LEA_ORIGINAL_DUAL_LAND_ART_BASIC;
            case FRAME_STYLE_RETRO: return FrameStyle.RETRO;
            default: return FrameStyle.M15_NORMAL;
        }
    }

    public static mage.proto.FrameStyle toProtoFrameStyle(FrameStyle style) {
        if (style == null) return mage.proto.FrameStyle.FRAME_STYLE_UNSPECIFIED;
        switch (style) {
            case M15_NORMAL: return mage.proto.FrameStyle.FRAME_STYLE_M15_NORMAL;
            case BFZ_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_BFZ_FULL_ART_BASIC;
            case KLD_INVENTION: return mage.proto.FrameStyle.FRAME_STYLE_KLD_INVENTION;
            case ZEN_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_ZEN_FULL_ART_BASIC;
            case MPRP_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_MPRP_FULL_ART_BASIC;
            case MPOP_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_MPOP_FULL_ART_BASIC;
            case UNH_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_UNH_FULL_ART_BASIC;
            case UGL_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_UGL_FULL_ART_BASIC;
            case UST_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_UST_FULL_ART_BASIC;
            case ANA_FULL_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_ANA_FULL_ART_BASIC;
            case LEA_ORIGINAL_DUAL_LAND_ART_BASIC: return mage.proto.FrameStyle.FRAME_STYLE_LEA_ORIGINAL_DUAL_LAND_ART_BASIC;
            case RETRO: return mage.proto.FrameStyle.FRAME_STYLE_RETRO;
            default: return mage.proto.FrameStyle.FRAME_STYLE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // ArtRect Conversions
    // ============================================================================

    public static mage.cards.ArtRect toArtRect(mage.proto.ArtRect protoRect) {
        switch (protoRect) {
            case ART_RECT_NORMAL: return mage.cards.ArtRect.NORMAL;
            case ART_RECT_RETRO: return mage.cards.ArtRect.RETRO;
            case ART_RECT_AFTERMATH_TOP: return mage.cards.ArtRect.AFTERMATH_TOP;
            case ART_RECT_AFTERMATH_BOTTOM: return mage.cards.ArtRect.AFTERMATH_BOTTOM;
            case ART_RECT_SPLIT_LEFT: return mage.cards.ArtRect.SPLIT_LEFT;
            case ART_RECT_SPLIT_RIGHT: return mage.cards.ArtRect.SPLIT_RIGHT;
            case ART_RECT_SPLIT_FUSED: return mage.cards.ArtRect.SPLIT_FUSED;
            case ART_RECT_FULL_LENGTH_LEFT: return mage.cards.ArtRect.FULL_LENGTH_LEFT;
            case ART_RECT_FULL_LENGTH_RIGHT: return mage.cards.ArtRect.FULL_LENGTH_RIGHT;
            default: return mage.cards.ArtRect.NORMAL;
        }
    }

    public static mage.proto.ArtRect toProtoArtRect(mage.cards.ArtRect rect) {
        if (rect == null) return mage.proto.ArtRect.ART_RECT_UNSPECIFIED;
        switch (rect) {
            case NORMAL: return mage.proto.ArtRect.ART_RECT_NORMAL;
            case RETRO: return mage.proto.ArtRect.ART_RECT_RETRO;
            case AFTERMATH_TOP: return mage.proto.ArtRect.ART_RECT_AFTERMATH_TOP;
            case AFTERMATH_BOTTOM: return mage.proto.ArtRect.ART_RECT_AFTERMATH_BOTTOM;
            case SPLIT_LEFT: return mage.proto.ArtRect.ART_RECT_SPLIT_LEFT;
            case SPLIT_RIGHT: return mage.proto.ArtRect.ART_RECT_SPLIT_RIGHT;
            case SPLIT_FUSED: return mage.proto.ArtRect.ART_RECT_SPLIT_FUSED;
            case FULL_LENGTH_LEFT: return mage.proto.ArtRect.ART_RECT_FULL_LENGTH_LEFT;
            case FULL_LENGTH_RIGHT: return mage.proto.ArtRect.ART_RECT_FULL_LENGTH_RIGHT;
            default: return mage.proto.ArtRect.ART_RECT_UNSPECIFIED;
        }
    }

    // ============================================================================
    // AbilityType Conversions
    // ============================================================================

    public static AbilityType toAbilityType(mage.proto.AbilityType protoType) {
        switch (protoType) {
            case ABILITY_TYPE_PLAY_LAND: return AbilityType.PLAY_LAND;
            case ABILITY_TYPE_SPELL: return AbilityType.SPELL;
            case ABILITY_TYPE_STATIC: return AbilityType.STATIC;
            case ABILITY_TYPE_EVASION: return AbilityType.EVASION;
            case ABILITY_TYPE_ACTIVATED_NONMANA: return AbilityType.ACTIVATED_NONMANA;
            case ABILITY_TYPE_ACTIVATED_MANA: return AbilityType.ACTIVATED_MANA;
            case ABILITY_TYPE_TRIGGERED_NONMANA: return AbilityType.TRIGGERED_NONMANA;
            case ABILITY_TYPE_TRIGGERED_MANA: return AbilityType.TRIGGERED_MANA;
            case ABILITY_TYPE_SPECIAL_ACTION: return AbilityType.SPECIAL_ACTION;
            case ABILITY_TYPE_SPECIAL_MANA_PAYMENT: return AbilityType.SPECIAL_MANA_PAYMENT;
            default: return null;
        }
    }

    public static mage.proto.AbilityType toProtoAbilityType(AbilityType type) {
        if (type == null) return mage.proto.AbilityType.ABILITY_TYPE_UNSPECIFIED;
        switch (type) {
            case PLAY_LAND: return mage.proto.AbilityType.ABILITY_TYPE_PLAY_LAND;
            case SPELL: return mage.proto.AbilityType.ABILITY_TYPE_SPELL;
            case STATIC: return mage.proto.AbilityType.ABILITY_TYPE_STATIC;
            case EVASION: return mage.proto.AbilityType.ABILITY_TYPE_EVASION;
            case ACTIVATED_NONMANA: return mage.proto.AbilityType.ABILITY_TYPE_ACTIVATED_NONMANA;
            case ACTIVATED_MANA: return mage.proto.AbilityType.ABILITY_TYPE_ACTIVATED_MANA;
            case TRIGGERED_NONMANA: return mage.proto.AbilityType.ABILITY_TYPE_TRIGGERED_NONMANA;
            case TRIGGERED_MANA: return mage.proto.AbilityType.ABILITY_TYPE_TRIGGERED_MANA;
            case SPECIAL_ACTION: return mage.proto.AbilityType.ABILITY_TYPE_SPECIAL_ACTION;
            case SPECIAL_MANA_PAYMENT: return mage.proto.AbilityType.ABILITY_TYPE_SPECIAL_MANA_PAYMENT;
            default: return mage.proto.AbilityType.ABILITY_TYPE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // MageObjectType Conversions
    // ============================================================================

    public static MageObjectType toMageObjectType(mage.proto.MageObjectType protoType) {
        switch (protoType) {
            case MAGE_OBJECT_TYPE_ABILITY_STACK_FROM_CARD: return MageObjectType.ABILITY_STACK_FROM_CARD;
            case MAGE_OBJECT_TYPE_ABILITY_STACK_FROM_TOKEN: return MageObjectType.ABILITY_STACK_FROM_TOKEN;
            case MAGE_OBJECT_TYPE_CARD: return MageObjectType.CARD;
            case MAGE_OBJECT_TYPE_COPY_CARD: return MageObjectType.COPY_CARD;
            case MAGE_OBJECT_TYPE_TOKEN: return MageObjectType.TOKEN;
            case MAGE_OBJECT_TYPE_SPELL: return MageObjectType.SPELL;
            case MAGE_OBJECT_TYPE_PERMANENT: return MageObjectType.PERMANENT;
            case MAGE_OBJECT_TYPE_DUNGEON: return MageObjectType.DUNGEON;
            case MAGE_OBJECT_TYPE_EMBLEM: return MageObjectType.EMBLEM;
            case MAGE_OBJECT_TYPE_COMMANDER: return MageObjectType.COMMANDER;
            case MAGE_OBJECT_TYPE_DESIGNATION: return MageObjectType.DESIGNATION;
            case MAGE_OBJECT_TYPE_PLANE: return MageObjectType.PLANE;
            case MAGE_OBJECT_TYPE_NULL: return MageObjectType.NULL;
            default: return MageObjectType.NULL;
        }
    }

    public static mage.proto.MageObjectType toProtoMageObjectType(MageObjectType type) {
        if (type == null) return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_UNSPECIFIED;
        switch (type) {
            case ABILITY_STACK_FROM_CARD: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_ABILITY_STACK_FROM_CARD;
            case ABILITY_STACK_FROM_TOKEN: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_ABILITY_STACK_FROM_TOKEN;
            case CARD: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_CARD;
            case COPY_CARD: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_COPY_CARD;
            case TOKEN: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_TOKEN;
            case SPELL: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_SPELL;
            case PERMANENT: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_PERMANENT;
            case DUNGEON: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_DUNGEON;
            case EMBLEM: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_EMBLEM;
            case COMMANDER: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_COMMANDER;
            case DESIGNATION: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_DESIGNATION;
            case PLANE: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_PLANE;
            case NULL: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_NULL;
            default: return mage.proto.MageObjectType.MAGE_OBJECT_TYPE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // ObjectColor Conversions
    // ============================================================================

    public static mage.proto.ObjectColor toProtoObjectColor(ObjectColor color) {
        if (color == null) return mage.proto.ObjectColor.getDefaultInstance();
        return mage.proto.ObjectColor.newBuilder()
                .setIsWhite(color.isWhite())
                .setIsBlue(color.isBlue())
                .setIsBlack(color.isBlack())
                .setIsRed(color.isRed())
                .setIsGreen(color.isGreen())
                .build();
    }

    public static ObjectColor fromProtoObjectColor(mage.proto.ObjectColor proto) {
        if (proto == null) return new ObjectColor();
        ObjectColor color = new ObjectColor();
        if (proto.getIsWhite()) color.setWhite(true);
        if (proto.getIsBlue()) color.setBlue(true);
        if (proto.getIsBlack()) color.setBlack(true);
        if (proto.getIsRed()) color.setRed(true);
        if (proto.getIsGreen()) color.setGreen(true);
        return color;
    }

    // ============================================================================
    // MessageColor Conversions
    // ============================================================================

    public static mage.proto.MessageColor toProtoMessageColor(ChatMessage.MessageColor color) {
        if (color == null) return mage.proto.MessageColor.MESSAGE_COLOR_UNSPECIFIED;
        switch (color) {
            case BLACK: return mage.proto.MessageColor.MESSAGE_COLOR_BLACK;
            case RED: return mage.proto.MessageColor.MESSAGE_COLOR_RED;
            case GREEN: return mage.proto.MessageColor.MESSAGE_COLOR_GREEN;
            case BLUE: return mage.proto.MessageColor.MESSAGE_COLOR_BLUE;
            case ORANGE: return mage.proto.MessageColor.MESSAGE_COLOR_ORANGE;
            case YELLOW: return mage.proto.MessageColor.MESSAGE_COLOR_YELLOW;
            default: return mage.proto.MessageColor.MESSAGE_COLOR_UNSPECIFIED;
        }
    }

    // ============================================================================
    // MessageType Conversions
    // ============================================================================

    public static mage.proto.MessageType toProtoMessageType(ChatMessage.MessageType type) {
        if (type == null) return mage.proto.MessageType.MESSAGE_TYPE_UNSPECIFIED;
        switch (type) {
            case USER_INFO: return mage.proto.MessageType.MESSAGE_TYPE_USER_INFO;
            case STATUS: return mage.proto.MessageType.MESSAGE_TYPE_STATUS;
            case GAME: return mage.proto.MessageType.MESSAGE_TYPE_GAME;
            case TALK: return mage.proto.MessageType.MESSAGE_TYPE_TALK;
            case WHISPER_FROM: return mage.proto.MessageType.MESSAGE_TYPE_WHISPER_FROM;
            case WHISPER_TO: return mage.proto.MessageType.MESSAGE_TYPE_WHISPER_TO;
            default: return mage.proto.MessageType.MESSAGE_TYPE_UNSPECIFIED;
        }
    }

    // ============================================================================
    // SoundToPlay Conversions
    // ============================================================================

    public static mage.proto.SoundToPlay toProtoSoundToPlay(ChatMessage.SoundToPlay sound) {
        if (sound == null) return mage.proto.SoundToPlay.SOUND_TO_PLAY_UNSPECIFIED;
        switch (sound) {
            case PlayerLeft: return mage.proto.SoundToPlay.SOUND_TO_PLAY_PLAYER_LEFT;
            case PlayerQuitTournament: return mage.proto.SoundToPlay.SOUND_TO_PLAY_PLAYER_QUIT_TOURNAMENT;
            case PlayerSubmittedDeck: return mage.proto.SoundToPlay.SOUND_TO_PLAY_PLAYER_SUBMITTED_DECK;
            case PlayerWhispered: return mage.proto.SoundToPlay.SOUND_TO_PLAY_PLAYER_WHISPERED;
            default: return mage.proto.SoundToPlay.SOUND_TO_PLAY_UNSPECIFIED;
        }
    }

    // ============================================================================
    // View Object Conversions (Java View -> Proto)
    // ============================================================================

    public static CounterViewProto toProtoCounterView(CounterView view) {
        if (view == null) return CounterViewProto.getDefaultInstance();
        return CounterViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setCount(view.getCount())
                .build();
    }

    public static ManaPoolViewProto toProtoManaPoolView(ManaPoolView view) {
        if (view == null) return ManaPoolViewProto.getDefaultInstance();
        return ManaPoolViewProto.newBuilder()
                .setRed(view.getRed())
                .setGreen(view.getGreen())
                .setBlue(view.getBlue())
                .setWhite(view.getWhite())
                .setBlack(view.getBlack())
                .setColorless(view.getColorless())
                .build();
    }

    // ============================================================================
    // DeckCardLists Conversion
    // ============================================================================

    public static DeckCardLists fromProtoDeckCardLists(DeckCardListsProto proto) {
        if (proto == null) return new DeckCardLists();
        DeckCardLists deck = new DeckCardLists();
        deck.setName(proto.getName());
        for (String card : proto.getCardsList()) {
            deck.getCards().add(card);
        }
        for (String card : proto.getSideboardList()) {
            deck.getSideboard().add(card);
        }
        return deck;
    }

    public static DeckCardListsProto toProtoDeckCardLists(DeckCardLists deck) {
        if (deck == null) return DeckCardListsProto.getDefaultInstance();
        DeckCardListsProto.Builder builder = DeckCardListsProto.newBuilder()
                .setName(deck.getName() != null ? deck.getName() : "");
        for (Object card : deck.getCards()) {
            builder.addCards(card.toString());
        }
        for (Object card : deck.getSideboard()) {
            builder.addSideboard(card.toString());
        }
        return builder.build();
    }
}
