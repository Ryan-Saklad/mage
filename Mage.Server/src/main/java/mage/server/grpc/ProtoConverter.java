package mage.server.grpc;

import mage.ObjectColor;
import mage.cards.FrameStyle;
import mage.cards.decks.DeckCardLists;
import mage.players.PlayerType;
import mage.view.ChatMessage;
import mage.view.CounterView;
import mage.view.ManaPoolView;

/**
 * Utility class for converting between Java view objects and Protocol Buffer messages.
 * All conversions are 1:1 mappings matching the proto definitions exactly.
 */
public final class ProtoConverter {

    private ProtoConverter() {}

    // ============================================================================
    // UUID Conversions
    // ============================================================================

    public static String toProtoUuid(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : "";
    }

    public static java.util.UUID fromProtoUuid(String uuid) {
        return uuid != null && !uuid.isEmpty() ? java.util.UUID.fromString(uuid) : null;
    }

    // ============================================================================
    // MageVersion Conversions
    // ============================================================================

    public static mage.utils.MageVersion fromProtoVersion(mage.proto.MageVersion proto) {
        if (proto == null) return null;
        return new mage.utils.MageVersion(
                proto.getMajor(),
                proto.getMinor(),
                proto.getRelease(),
                proto.getReleaseInfo()
        );
    }

    public static mage.proto.MageVersion toProtoVersion(mage.utils.MageVersion version) {
        if (version == null) return mage.proto.MageVersion.getDefaultInstance();
        return mage.proto.MageVersion.newBuilder()
                .setMajor(version.getMajor())
                .setMinor(version.getMinor())
                .setRelease(version.getRelease())
                .setReleaseInfo(version.getReleaseInfo() != null ? version.getReleaseInfo() : "")
                .build();
    }

    // ============================================================================
    // UserData Conversions
    // ============================================================================

    public static mage.players.net.UserData fromProtoUserData(mage.proto.UserDataProto proto) {
        if (proto == null) return null;
        mage.players.net.UserData userData = new mage.players.net.UserData(
                mage.players.net.UserGroup.DEFAULT,
                proto.getAvatarId(),
                proto.getShowAbilityPickerForced(),
                proto.getAllowRequestHandToAll(),
                proto.getConfirmEmptyManaPool(),
                proto.getUserSkipPrioritySteps() != null ? proto.getUserSkipPrioritySteps() : "",
                proto.getFlagName(),
                proto.getAskMoveToGraveOrder(),
                proto.getManaPoolAutomatic(),
                proto.getManaPoolAutomaticRestricted(),
                proto.getPastasPlain(),
                proto.getUseFirstManaAbility(),
                proto.getUserIdStr()
        );
        return userData;
    }

    public static mage.proto.UserDataProto toProtoUserData(mage.players.net.UserData userData) {
        if (userData == null) return mage.proto.UserDataProto.getDefaultInstance();
        return mage.proto.UserDataProto.newBuilder()
                .setAvatarId(userData.getAvatarId())
                .setShowAbilityPickerForced(userData.isShowAbilityPickerForced())
                .setAllowRequestHandToAll(userData.isAllowRequestHandToAll())
                .setConfirmEmptyManaPool(userData.isConfirmEmptyManaPool())
                .setUserSkipPrioritySteps(userData.getUserSkipPrioritySteps() != null ? userData.getUserSkipPrioritySteps() : "")
                .setFlagName(userData.getFlagName() != null ? userData.getFlagName() : "")
                .setAskMoveToGraveOrder(userData.isAskMoveToGraveOrder())
                .setManaPoolAutomatic(userData.isManaPoolAutomatic())
                .setManaPoolAutomaticRestricted(userData.isManaPoolAutomaticRestricted())
                .setPastasPlain(userData.getPastasPlain() != null ? userData.getPastasPlain() : "")
                .setUseFirstManaAbility(userData.isUseFirstManaAbility())
                .setUserIdStr(userData.getUserIdStr() != null ? userData.getUserIdStr() : "")
                .build();
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

    public static mage.constants.SkillLevel toSkillLevel(mage.proto.SkillLevel protoLevel) {
        switch (protoLevel) {
            case SKILL_LEVEL_BEGINNER: return mage.constants.SkillLevel.BEGINNER;
            case SKILL_LEVEL_CASUAL: return mage.constants.SkillLevel.CASUAL;
            case SKILL_LEVEL_SERIOUS: return mage.constants.SkillLevel.SERIOUS;
            default: return mage.constants.SkillLevel.CASUAL;
        }
    }

    public static mage.proto.SkillLevel toProtoSkillLevel(mage.constants.SkillLevel level) {
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

    public static mage.constants.Zone toZone(mage.proto.Zone protoZone) {
        switch (protoZone) {
            case ZONE_HAND: return mage.constants.Zone.HAND;
            case ZONE_GRAVEYARD: return mage.constants.Zone.GRAVEYARD;
            case ZONE_LIBRARY: return mage.constants.Zone.LIBRARY;
            case ZONE_BATTLEFIELD: return mage.constants.Zone.BATTLEFIELD;
            case ZONE_STACK: return mage.constants.Zone.STACK;
            case ZONE_EXILED: return mage.constants.Zone.EXILED;
            case ZONE_ALL: return mage.constants.Zone.ALL;
            case ZONE_OUTSIDE: return mage.constants.Zone.OUTSIDE;
            case ZONE_COMMAND: return mage.constants.Zone.COMMAND;
            default: return null;
        }
    }

    public static mage.proto.Zone toProtoZone(mage.constants.Zone zone) {
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

    public static mage.constants.CardType toCardType(mage.proto.CardType protoType) {
        switch (protoType) {
            case CARD_TYPE_ARTIFACT: return mage.constants.CardType.ARTIFACT;
            case CARD_TYPE_BATTLE: return mage.constants.CardType.BATTLE;
            case CARD_TYPE_CONSPIRACY: return mage.constants.CardType.CONSPIRACY;
            case CARD_TYPE_CREATURE: return mage.constants.CardType.CREATURE;
            case CARD_TYPE_DUNGEON: return mage.constants.CardType.DUNGEON;
            case CARD_TYPE_ENCHANTMENT: return mage.constants.CardType.ENCHANTMENT;
            case CARD_TYPE_INSTANT: return mage.constants.CardType.INSTANT;
            case CARD_TYPE_LAND: return mage.constants.CardType.LAND;
            case CARD_TYPE_PHENOMENON: return mage.constants.CardType.PHENOMENON;
            case CARD_TYPE_PLANE: return mage.constants.CardType.PLANE;
            case CARD_TYPE_PLANESWALKER: return mage.constants.CardType.PLANESWALKER;
            case CARD_TYPE_SCHEME: return mage.constants.CardType.SCHEME;
            case CARD_TYPE_SORCERY: return mage.constants.CardType.SORCERY;
            case CARD_TYPE_KINDRED: return mage.constants.CardType.KINDRED;
            case CARD_TYPE_VANGUARD: return mage.constants.CardType.VANGUARD;
            default: return null;
        }
    }

    public static mage.proto.CardType toProtoCardType(mage.constants.CardType type) {
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

    public static mage.constants.SuperType toSuperType(mage.proto.SuperType protoType) {
        switch (protoType) {
            case SUPER_TYPE_BASIC: return mage.constants.SuperType.BASIC;
            case SUPER_TYPE_ELITE: return mage.constants.SuperType.ELITE;
            case SUPER_TYPE_LEGENDARY: return mage.constants.SuperType.LEGENDARY;
            case SUPER_TYPE_ONGOING: return mage.constants.SuperType.ONGOING;
            case SUPER_TYPE_SNOW: return mage.constants.SuperType.SNOW;
            case SUPER_TYPE_WORLD: return mage.constants.SuperType.WORLD;
            default: return null;
        }
    }

    public static mage.proto.SuperType toProtoSuperType(mage.constants.SuperType type) {
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

    public static mage.constants.Rarity toRarity(mage.proto.Rarity protoRarity) {
        switch (protoRarity) {
            case RARITY_LAND: return mage.constants.Rarity.LAND;
            case RARITY_COMMON: return mage.constants.Rarity.COMMON;
            case RARITY_UNCOMMON: return mage.constants.Rarity.UNCOMMON;
            case RARITY_RARE: return mage.constants.Rarity.RARE;
            case RARITY_MYTHIC: return mage.constants.Rarity.MYTHIC;
            case RARITY_SPECIAL: return mage.constants.Rarity.SPECIAL;
            case RARITY_BONUS: return mage.constants.Rarity.BONUS;
            default: return null;
        }
    }

    public static mage.proto.Rarity toProtoRarity(mage.constants.Rarity rarity) {
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

    public static mage.constants.TableState toTableState(mage.proto.TableState protoState) {
        switch (protoState) {
            case TABLE_STATE_WAITING: return mage.constants.TableState.WAITING;
            case TABLE_STATE_READY_TO_START: return mage.constants.TableState.READY_TO_START;
            case TABLE_STATE_STARTING: return mage.constants.TableState.STARTING;
            case TABLE_STATE_DRAFTING: return mage.constants.TableState.DRAFTING;
            case TABLE_STATE_CONSTRUCTING: return mage.constants.TableState.CONSTRUCTING;
            case TABLE_STATE_DUELING: return mage.constants.TableState.DUELING;
            case TABLE_STATE_SIDEBOARDING: return mage.constants.TableState.SIDEBOARDING;
            case TABLE_STATE_FINISHED: return mage.constants.TableState.FINISHED;
            default: return null;
        }
    }

    public static mage.proto.TableState toProtoTableState(mage.constants.TableState state) {
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

    public static mage.constants.PhaseStep toPhaseStep(mage.proto.PhaseStep protoStep) {
        switch (protoStep) {
            case PHASE_STEP_UNTAP: return mage.constants.PhaseStep.UNTAP;
            case PHASE_STEP_UPKEEP: return mage.constants.PhaseStep.UPKEEP;
            case PHASE_STEP_DRAW: return mage.constants.PhaseStep.DRAW;
            case PHASE_STEP_PRECOMBAT_MAIN: return mage.constants.PhaseStep.PRECOMBAT_MAIN;
            case PHASE_STEP_BEGIN_COMBAT: return mage.constants.PhaseStep.BEGIN_COMBAT;
            case PHASE_STEP_DECLARE_ATTACKERS: return mage.constants.PhaseStep.DECLARE_ATTACKERS;
            case PHASE_STEP_DECLARE_BLOCKERS: return mage.constants.PhaseStep.DECLARE_BLOCKERS;
            case PHASE_STEP_FIRST_COMBAT_DAMAGE: return mage.constants.PhaseStep.FIRST_COMBAT_DAMAGE;
            case PHASE_STEP_COMBAT_DAMAGE: return mage.constants.PhaseStep.COMBAT_DAMAGE;
            case PHASE_STEP_END_COMBAT: return mage.constants.PhaseStep.END_COMBAT;
            case PHASE_STEP_POSTCOMBAT_MAIN: return mage.constants.PhaseStep.POSTCOMBAT_MAIN;
            case PHASE_STEP_END_TURN: return mage.constants.PhaseStep.END_TURN;
            case PHASE_STEP_CLEANUP: return mage.constants.PhaseStep.CLEANUP;
            default: return null;
        }
    }

    public static mage.proto.PhaseStep toProtoPhaseStep(mage.constants.PhaseStep step) {
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

    public static mage.constants.AbilityType toAbilityType(mage.proto.AbilityType protoType) {
        switch (protoType) {
            case ABILITY_TYPE_PLAY_LAND: return mage.constants.AbilityType.PLAY_LAND;
            case ABILITY_TYPE_SPELL: return mage.constants.AbilityType.SPELL;
            case ABILITY_TYPE_STATIC: return mage.constants.AbilityType.STATIC;
            case ABILITY_TYPE_EVASION: return mage.constants.AbilityType.EVASION;
            case ABILITY_TYPE_ACTIVATED_NONMANA: return mage.constants.AbilityType.ACTIVATED_NONMANA;
            case ABILITY_TYPE_ACTIVATED_MANA: return mage.constants.AbilityType.ACTIVATED_MANA;
            case ABILITY_TYPE_TRIGGERED_NONMANA: return mage.constants.AbilityType.TRIGGERED_NONMANA;
            case ABILITY_TYPE_TRIGGERED_MANA: return mage.constants.AbilityType.TRIGGERED_MANA;
            case ABILITY_TYPE_SPECIAL_ACTION: return mage.constants.AbilityType.SPECIAL_ACTION;
            case ABILITY_TYPE_SPECIAL_MANA_PAYMENT: return mage.constants.AbilityType.SPECIAL_MANA_PAYMENT;
            default: return null;
        }
    }

    public static mage.proto.AbilityType toProtoAbilityType(mage.constants.AbilityType type) {
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

    public static mage.constants.MageObjectType toMageObjectType(mage.proto.MageObjectType protoType) {
        switch (protoType) {
            case MAGE_OBJECT_TYPE_ABILITY_STACK_FROM_CARD: return mage.constants.MageObjectType.ABILITY_STACK_FROM_CARD;
            case MAGE_OBJECT_TYPE_ABILITY_STACK_FROM_TOKEN: return mage.constants.MageObjectType.ABILITY_STACK_FROM_TOKEN;
            case MAGE_OBJECT_TYPE_CARD: return mage.constants.MageObjectType.CARD;
            case MAGE_OBJECT_TYPE_COPY_CARD: return mage.constants.MageObjectType.COPY_CARD;
            case MAGE_OBJECT_TYPE_TOKEN: return mage.constants.MageObjectType.TOKEN;
            case MAGE_OBJECT_TYPE_SPELL: return mage.constants.MageObjectType.SPELL;
            case MAGE_OBJECT_TYPE_PERMANENT: return mage.constants.MageObjectType.PERMANENT;
            case MAGE_OBJECT_TYPE_DUNGEON: return mage.constants.MageObjectType.DUNGEON;
            case MAGE_OBJECT_TYPE_EMBLEM: return mage.constants.MageObjectType.EMBLEM;
            case MAGE_OBJECT_TYPE_COMMANDER: return mage.constants.MageObjectType.COMMANDER;
            case MAGE_OBJECT_TYPE_DESIGNATION: return mage.constants.MageObjectType.DESIGNATION;
            case MAGE_OBJECT_TYPE_PLANE: return mage.constants.MageObjectType.PLANE;
            case MAGE_OBJECT_TYPE_NULL: return mage.constants.MageObjectType.NULL;
            default: return mage.constants.MageObjectType.NULL;
        }
    }

    public static mage.proto.MageObjectType toProtoMageObjectType(mage.constants.MageObjectType type) {
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

    public static mage.proto.CounterViewProto toProtoCounterView(CounterView view) {
        if (view == null) return mage.proto.CounterViewProto.getDefaultInstance();
        return mage.proto.CounterViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setCount(view.getCount())
                .build();
    }

    public static mage.proto.ManaPoolViewProto toProtoManaPoolView(ManaPoolView view) {
        if (view == null) return mage.proto.ManaPoolViewProto.getDefaultInstance();
        return mage.proto.ManaPoolViewProto.newBuilder()
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

    public static DeckCardLists fromProtoDeckCardLists(mage.proto.DeckCardListsProto proto) {
        if (proto == null) return new DeckCardLists();
        DeckCardLists deck = new DeckCardLists();
        deck.setName(proto.getName());
        // Cards are stored as simple strings in proto format "cardName|setCode|cardNum|quantity"
        for (String cardStr : proto.getCardsList()) {
            mage.cards.decks.DeckCardInfo cardInfo = mage.cards.decks.DeckCardInfo.createFromString(cardStr);
            if (cardInfo != null) {
                deck.getCards().add(cardInfo);
            }
        }
        for (String cardStr : proto.getSideboardList()) {
            mage.cards.decks.DeckCardInfo cardInfo = mage.cards.decks.DeckCardInfo.createFromString(cardStr);
            if (cardInfo != null) {
                deck.getSideboard().add(cardInfo);
            }
        }
        return deck;
    }

    public static mage.proto.DeckCardListsProto toProtoDeckCardLists(DeckCardLists deck) {
        if (deck == null) return mage.proto.DeckCardListsProto.getDefaultInstance();
        mage.proto.DeckCardListsProto.Builder builder = mage.proto.DeckCardListsProto.newBuilder()
                .setName(deck.getName() != null ? deck.getName() : "");
        for (mage.cards.decks.DeckCardInfo card : deck.getCards()) {
            builder.addCards(card.toString());
        }
        for (mage.cards.decks.DeckCardInfo card : deck.getSideboard()) {
            builder.addSideboard(card.toString());
        }
        return builder.build();
    }

    // ============================================================================
    // GameTypeView Conversion
    // ============================================================================

    public static mage.proto.GameTypeViewProto toProtoGameTypeView(mage.view.GameTypeView view) {
        if (view == null) return mage.proto.GameTypeViewProto.getDefaultInstance();
        return mage.proto.GameTypeViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setMinPlayers(view.getMinPlayers())
                .setMaxPlayers(view.getMaxPlayers())
                .setNumTeams(view.getNumTeams())
                .setPlayersPerTeam(view.getPlayersPerTeam())
                .setUseRange(view.isUseRange())
                .setUseAttackOption(view.isUseAttackOption())
                .build();
    }

    // ============================================================================
    // TournamentTypeView Conversion
    // ============================================================================

    public static mage.proto.TournamentTypeViewProto toProtoTournamentTypeView(mage.view.TournamentTypeView view) {
        if (view == null) return mage.proto.TournamentTypeViewProto.getDefaultInstance();
        return mage.proto.TournamentTypeViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setMinPlayers(view.getMinPlayers())
                .setMaxPlayers(view.getMaxPlayers())
                .setNumBoosters(view.getNumBoosters())
                .setDraft(view.isDraft())
                .setLimited(view.isLimited())
                .setCubeBooster(view.isCubeBooster())
                .setElimination(view.isElimination())
                .setRandom(view.isRandom())
                .setReshuffled(view.isReshuffled())
                .setRichMan(view.isRichMan())
                .setJumpstart(view.isJumpstart())
                .build();
    }

    // ============================================================================
    // TableView Conversion
    // ============================================================================

    public static mage.proto.TableViewProto toProtoTableView(mage.view.TableView view) {
        if (view == null) return mage.proto.TableViewProto.getDefaultInstance();
        mage.proto.TableViewProto.Builder builder = mage.proto.TableViewProto.newBuilder()
                .setTableId(view.getTableId() != null ? view.getTableId().toString() : "")
                .setTableName(view.getTableName() != null ? view.getTableName() : "")
                .setControllerName(view.getControllerName() != null ? view.getControllerName() : "")
                .setGameType(view.getGameType() != null ? view.getGameType() : "")
                .setDeckType(view.getDeckType() != null ? view.getDeckType() : "")
                .setTableState(toProtoTableState(view.getTableState()))
                .setCreateTime(view.getCreateTime() != null ? view.getCreateTime().getTime() : 0)
                .setSeatsInfo(view.getSeatsInfo() != null ? view.getSeatsInfo() : "")
                .setSkillLevel(view.getSkillLevel() != null ? view.getSkillLevel() : "")
                .setQuitRatio(view.getQuitRatio() != null ? view.getQuitRatio() : "")
                .setMinimumRating(view.getMinimumRating())
                .setLimited(view.isLimited())
                .setRated(view.isRated())
                .setPassworded(view.isPassworded())
                .setSpectatorsAllowed(view.getSpectatorsAllowed())
                .setRollbackTurnsAllowed(view.getRollbackTurnsAllowed())
                .setWins(view.getWins())
                .setFreeMulligans(view.getFreeMulligans())
                .setPlaneChase(view.isPlaneChase())
                .setTournament(view.isTournament())
                .setRowColor(view.getRowColor() != null ? view.getRowColor() : "");

        if (view.getTableState() != null) {
            builder.setTableStateStr(view.getTableState().toString());
        }

        return builder.build();
    }

    // ============================================================================
    // MatchView Conversion
    // ============================================================================

    public static mage.proto.MatchViewProto toProtoMatchView(mage.view.MatchView view) {
        if (view == null) return mage.proto.MatchViewProto.getDefaultInstance();
        mage.proto.MatchViewProto.Builder builder = mage.proto.MatchViewProto.newBuilder()
                .setMatchId(view.getMatchId() != null ? view.getMatchId().toString() : "")
                .setMatchName(view.getMatchName() != null ? view.getMatchName() : "")
                .setGameType(view.getGameType() != null ? view.getGameType() : "")
                .setDeckType(view.getDeckType() != null ? view.getDeckType() : "")
                .setGames(view.getGames() != null ? view.getGames() : "")
                .setResult(view.getResult() != null ? view.getResult() : "")
                .setStartTime(view.getStartTime() != null ? view.getStartTime().getTime() : 0)
                .setEndTime(view.getEndTime() != null ? view.getEndTime().getTime() : 0)
                .setReplayAvailable(view.isReplayAvailable())
                .setRated(view.isRated());

        if (view.getPlayers() != null) {
            for (String player : view.getPlayers()) {
                builder.addPlayers(player);
            }
        }

        return builder.build();
    }

    // ============================================================================
    // RoomUsersView Conversion
    // ============================================================================

    public static mage.proto.RoomUsersViewProto toProtoRoomUsersView(mage.view.RoomUsersView view) {
        if (view == null) return mage.proto.RoomUsersViewProto.getDefaultInstance();
        mage.proto.RoomUsersViewProto.Builder builder = mage.proto.RoomUsersViewProto.newBuilder()
                .setRoomId(view.getRoomId() != null ? view.getRoomId().toString() : "");

        if (view.getUsersView() != null) {
            for (mage.view.UsersView userView : view.getUsersView()) {
                builder.addUsers(toProtoUsersView(userView));
            }
        }

        return builder.build();
    }

    public static mage.proto.UsersViewProto toProtoUsersView(mage.view.UsersView view) {
        if (view == null) return mage.proto.UsersViewProto.getDefaultInstance();
        return mage.proto.UsersViewProto.newBuilder()
                .setUserName(view.getUserName() != null ? view.getUserName() : "")
                .setInfoState(view.getInfoState() != null ? view.getInfoState() : "")
                .setInfoGames(view.getInfoGames() != null ? view.getInfoGames() : "")
                .setInfoPing(view.getInfoPing() != null ? view.getInfoPing() : "")
                .setFlagName(view.getFlagName() != null ? view.getFlagName() : "")
                .build();
    }
}
