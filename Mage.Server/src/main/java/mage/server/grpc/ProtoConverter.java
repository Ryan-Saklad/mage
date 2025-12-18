package mage.server.grpc;

import mage.cards.decks.DeckCardLists;
import mage.constants.SubType;
import mage.players.net.UserData;
import mage.proto.*;
import mage.view.*;

import java.util.*;

/**
 * Utility class for converting between Java view objects and Protocol Buffer messages.
 */
public final class ProtoConverter {

    private ProtoConverter() {}

    // UUID conversions - use java.util.UUID explicitly to avoid ambiguity with mage.proto.UUID
    public static String toProtoUuid(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : "";
    }

    public static java.util.UUID fromProtoUuid(String uuid) {
        return uuid != null && !uuid.isEmpty() ? java.util.UUID.fromString(uuid) : null;
    }

    // ManaType conversions
    public static mage.constants.ManaType toManaType(mage.proto.ManaType protoType) {
        switch (protoType) {
            case MANA_TYPE_WHITE: return mage.constants.ManaType.WHITE;
            case MANA_TYPE_BLUE: return mage.constants.ManaType.BLUE;
            case MANA_TYPE_BLACK: return mage.constants.ManaType.BLACK;
            case MANA_TYPE_RED: return mage.constants.ManaType.RED;
            case MANA_TYPE_GREEN: return mage.constants.ManaType.GREEN;
            case MANA_TYPE_COLORLESS: return mage.constants.ManaType.COLORLESS;
            default: return mage.constants.ManaType.COLORLESS;
        }
    }

    public static mage.proto.ManaType toProtoManaType(mage.constants.ManaType type) {
        switch (type) {
            case WHITE: return mage.proto.ManaType.MANA_TYPE_WHITE;
            case BLUE: return mage.proto.ManaType.MANA_TYPE_BLUE;
            case BLACK: return mage.proto.ManaType.MANA_TYPE_BLACK;
            case RED: return mage.proto.ManaType.MANA_TYPE_RED;
            case GREEN: return mage.proto.ManaType.MANA_TYPE_GREEN;
            case COLORLESS: return mage.proto.ManaType.MANA_TYPE_COLORLESS;
            default: return mage.proto.ManaType.MANA_TYPE_UNSPECIFIED;
        }
    }

    // PlayerAction conversions
    public static mage.constants.PlayerAction toPlayerAction(mage.proto.PlayerAction protoAction) {
        switch (protoAction) {
            case PLAYER_ACTION_PASS_PRIORITY: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_NEXT_TURN;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_TURN: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_NEXT_TURN;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_TURN_END_STEP: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_TURN_END_STEP;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_TURN_SKIP_STACK: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_NEXT_TURN_SKIP_STACK;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_MAIN_PHASE: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_NEXT_MAIN_PHASE;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_NEXT_END_STEP: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_TURN_END_STEP; // Map to nearest equivalent
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_MY_NEXT_TURN: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_MY_NEXT_TURN;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_END_STEP_BEFORE_MY_NEXT_TURN: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_END_STEP_BEFORE_MY_NEXT_TURN;
            case PLAYER_ACTION_PASS_PRIORITY_UNTIL_STACK_RESOLVED: return mage.constants.PlayerAction.PASS_PRIORITY_UNTIL_STACK_RESOLVED;
            case PLAYER_ACTION_PASS_PRIORITY_CANCEL_ALL_ACTIONS: return mage.constants.PlayerAction.PASS_PRIORITY_CANCEL_ALL_ACTIONS;
            case PLAYER_ACTION_UNDO: return mage.constants.PlayerAction.UNDO;
            case PLAYER_ACTION_CONCEDE: return mage.constants.PlayerAction.CONCEDE;
            case PLAYER_ACTION_MANA_AUTO_PAYMENT_ON: return mage.constants.PlayerAction.MANA_AUTO_PAYMENT_ON;
            case PLAYER_ACTION_MANA_AUTO_PAYMENT_OFF: return mage.constants.PlayerAction.MANA_AUTO_PAYMENT_OFF;
            case PLAYER_ACTION_MANA_AUTO_PAYMENT_RESET: return mage.constants.PlayerAction.MANA_AUTO_PAYMENT_ON; // Reset to on by default
            case PLAYER_ACTION_ADD_PERMISSION_TO_ROLLBACK_TURN: return mage.constants.PlayerAction.ADD_PERMISSION_TO_ROLLBACK_TURN;
            case PLAYER_ACTION_DENY_PERMISSON_TO_ROLLBACK_TURN: return mage.constants.PlayerAction.DENY_PERMISSION_TO_ROLLBACK_TURN;
            case PLAYER_ACTION_ROLLBACK_TURNS: return mage.constants.PlayerAction.ROLLBACK_TURNS;
            case PLAYER_ACTION_REQUEST_PERMISSION_TO_SEE_HAND_CARDS: return mage.constants.PlayerAction.REQUEST_PERMISSION_TO_SEE_HAND_CARDS;
            case PLAYER_ACTION_REVOKE_PERMISSIONS_TO_SEE_HAND_CARDS: return mage.constants.PlayerAction.REVOKE_PERMISSIONS_TO_SEE_HAND_CARDS;
            case PLAYER_ACTION_ADD_PERMISSION_TO_SEE_HAND_CARDS: return mage.constants.PlayerAction.ADD_PERMISSION_TO_SEE_HAND_CARDS;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_ABILITY_FIRST: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_ABILITY_FIRST;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_ABILITY_LAST: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_ABILITY_LAST;
            case PLAYER_ACTION_TRIGGER_AUTO_ORDER_RESET_ALL: return mage.constants.PlayerAction.TRIGGER_AUTO_ORDER_RESET_ALL;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_ID_YES: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_ID_YES;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_ID_NO: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_ID_NO;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_TEXT_YES: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_TEXT_YES;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_TEXT_NO: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_TEXT_NO;
            case PLAYER_ACTION_REQUEST_AUTO_ANSWER_RESET_ALL: return mage.constants.PlayerAction.REQUEST_AUTO_ANSWER_RESET_ALL;
            case PLAYER_ACTION_HOLD_PRIORITY: return mage.constants.PlayerAction.HOLD_PRIORITY;
            case PLAYER_ACTION_UNHOLD_PRIORITY: return mage.constants.PlayerAction.UNHOLD_PRIORITY;
            case PLAYER_ACTION_PICTURE_AS_FOIL: return null; // Not supported in Java enum
            case PLAYER_ACTION_PICTURE_AS_NON_FOIL: return null; // Not supported in Java enum
            default: return null;
        }
    }

    // SkillLevel conversions
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

    // PlayerType conversions
    public static mage.players.PlayerType toPlayerType(mage.proto.PlayerType protoType) {
        switch (protoType) {
            case PLAYER_TYPE_HUMAN: return mage.players.PlayerType.HUMAN;
            case PLAYER_TYPE_COMPUTER_MAD: return mage.players.PlayerType.COMPUTER_MAD;
            case PLAYER_TYPE_COMPUTER_SIMPLE: return mage.players.PlayerType.COMPUTER_MAD; // Map to MAD as fallback
            case PLAYER_TYPE_COMPUTER_MONTE_CARLO: return mage.players.PlayerType.COMPUTER_MONTE_CARLO;
            case PLAYER_TYPE_COMPUTER_DRAFT_BOT: return mage.players.PlayerType.COMPUTER_DRAFT_BOT;
            default: return mage.players.PlayerType.HUMAN;
        }
    }

    public static mage.proto.PlayerType toProtoPlayerType(mage.players.PlayerType type) {
        if (type == null) return mage.proto.PlayerType.PLAYER_TYPE_UNSPECIFIED;
        switch (type) {
            case HUMAN: return mage.proto.PlayerType.PLAYER_TYPE_HUMAN;
            case COMPUTER_MAD: return mage.proto.PlayerType.PLAYER_TYPE_COMPUTER_MAD;
            case COMPUTER_MONTE_CARLO: return mage.proto.PlayerType.PLAYER_TYPE_COMPUTER_MONTE_CARLO;
            case COMPUTER_DRAFT_BOT: return mage.proto.PlayerType.PLAYER_TYPE_COMPUTER_DRAFT_BOT;
            default: return mage.proto.PlayerType.PLAYER_TYPE_UNSPECIFIED;
        }
    }

    // Zone conversions
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

    // CardType conversions
    public static mage.proto.CardType toProtoCardType(mage.constants.CardType cardType) {
        if (cardType == null) return mage.proto.CardType.CARD_TYPE_UNSPECIFIED;
        switch (cardType) {
            case ARTIFACT: return mage.proto.CardType.CARD_TYPE_ARTIFACT;
            case CONSPIRACY: return mage.proto.CardType.CARD_TYPE_CONSPIRACY;
            case CREATURE: return mage.proto.CardType.CARD_TYPE_CREATURE;
            case DUNGEON: return mage.proto.CardType.CARD_TYPE_DUNGEON;
            case ENCHANTMENT: return mage.proto.CardType.CARD_TYPE_ENCHANTMENT;
            case INSTANT: return mage.proto.CardType.CARD_TYPE_INSTANT;
            case KINDRED: return mage.proto.CardType.CARD_TYPE_KINDRED;
            case LAND: return mage.proto.CardType.CARD_TYPE_LAND;
            case PHENOMENON: return mage.proto.CardType.CARD_TYPE_PHENOMENON;
            case PLANE: return mage.proto.CardType.CARD_TYPE_PLANE;
            case PLANESWALKER: return mage.proto.CardType.CARD_TYPE_PLANESWALKER;
            case SCHEME: return mage.proto.CardType.CARD_TYPE_SCHEME;
            case SORCERY: return mage.proto.CardType.CARD_TYPE_SORCERY;
            case BATTLE: return mage.proto.CardType.CARD_TYPE_BATTLE;
            default: return mage.proto.CardType.CARD_TYPE_UNSPECIFIED;
        }
    }

    // SuperType conversions
    public static mage.proto.SuperType toProtoSuperType(mage.constants.SuperType superType) {
        if (superType == null) return mage.proto.SuperType.SUPER_TYPE_UNSPECIFIED;
        switch (superType) {
            case BASIC: return mage.proto.SuperType.SUPER_TYPE_BASIC;
            case ELITE: return mage.proto.SuperType.SUPER_TYPE_ELITE;
            case HOST: return mage.proto.SuperType.SUPER_TYPE_HOST;
            case LEGENDARY: return mage.proto.SuperType.SUPER_TYPE_LEGENDARY;
            case ONGOING: return mage.proto.SuperType.SUPER_TYPE_ONGOING;
            case SNOW: return mage.proto.SuperType.SUPER_TYPE_SNOW;
            case WORLD: return mage.proto.SuperType.SUPER_TYPE_WORLD;
            default: return mage.proto.SuperType.SUPER_TYPE_UNSPECIFIED;
        }
    }

    // Rarity conversions
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

    // ObjectColor conversion
    public static ObjectColor toProtoColor(mage.ObjectColor color) {
        if (color == null) return ObjectColor.getDefaultInstance();
        return ObjectColor.newBuilder()
                .setIsWhite(color.isWhite())
                .setIsBlue(color.isBlue())
                .setIsBlack(color.isBlack())
                .setIsRed(color.isRed())
                .setIsGreen(color.isGreen())
                .build();
    }

    // MageVersion conversion
    // Note: mage.utils.MageVersion uses static constants and doesn't expose getters
    public static mage.proto.MageVersion toProtoVersion(mage.utils.MageVersion version) {
        if (version == null) return mage.proto.MageVersion.getDefaultInstance();
        // Use static constants since MageVersion doesn't expose getters
        return mage.proto.MageVersion.newBuilder()
                .setMajor(mage.utils.MageVersion.MAGE_VERSION_MAJOR)
                .setMinor(mage.utils.MageVersion.MAGE_VERSION_MINOR)
                .setPatch(mage.utils.MageVersion.MAGE_VERSION_RELEASE)
                .setInfo(mage.utils.MageVersion.MAGE_VERSION_RELEASE_INFO)
                .build();
    }

    public static mage.utils.MageVersion fromProtoVersion(mage.proto.MageVersion proto) {
        if (proto == null) return null;
        // MageVersion requires a Class for build time detection
        return new mage.utils.MageVersion(ProtoConverter.class);
    }

    // UserData conversion
    public static UserData fromProtoUserData(UserDataProto proto) {
        if (proto == null) return null;
        UserData userData = UserData.getDefaultUserDataView();
        userData.setAvatarId(proto.getAvatarId());
        userData.setGroupId(proto.getGroupId());
        // show_absent_mana not available in UserData
        userData.setAllowRequestShowHandCards(proto.getAllowRequestShowHandCards());
        userData.setConfirmEmptyManaPool(proto.getConfirmEmptyManaPool());
        userData.setFlagName(proto.getFlagName());
        userData.setAskMoveToGraveOrder(proto.getAskMoveToGraveOrder());
        userData.setManaPoolAutomatic(proto.getManaAutoPayment());
        return userData;
    }

    // DeckCardLists conversion
    public static DeckCardLists fromProtoDeckCardLists(DeckCardListsProto proto) {
        if (proto == null) return null;
        DeckCardLists deckList = new DeckCardLists();
        deckList.setName(proto.getName());
        for (String card : proto.getCardsList()) {
            mage.cards.decks.DeckCardInfo info = parseDeckCardInfo(card);
            if (info != null) {
                deckList.getCards().add(info);
            }
        }
        for (String card : proto.getSideboardList()) {
            mage.cards.decks.DeckCardInfo info = parseDeckCardInfo(card);
            if (info != null) {
                deckList.getSideboard().add(info);
            }
        }
        return deckList;
    }

    private static mage.cards.decks.DeckCardInfo parseDeckCardInfo(String cardString) {
        // Format: "cardName|setCode|cardNumber|quantity"
        String[] parts = cardString.split("\\|");
        if (parts.length >= 4) {
            return new mage.cards.decks.DeckCardInfo(
                parts[0], // name
                parts[2], // card number
                parts[1], // set code
                Integer.parseInt(parts[3]) // quantity
            );
        }
        return null;
    }

    // TableView conversion
    public static TableViewProto toProtoTableView(TableView view) {
        if (view == null) return TableViewProto.getDefaultInstance();
        TableViewProto.Builder builder = TableViewProto.newBuilder()
                .setTableId(toProtoUuid(view.getTableId()))
                .setGameType(view.getGameType() != null ? view.getGameType() : "")
                .setControllerName(view.getControllerName() != null ? view.getControllerName() : "")
                .setDeckType(view.getDeckType() != null ? view.getDeckType() : "")
                .setTableState(view.getTableState() != null ? view.getTableState().toString() : "")
                .setIsTourney(view.isTournament())
                .setLimited(view.isLimited())
                .setRated(view.isRated())
                .setSkillLevel(toProtoSkillLevel(view.getSkillLevel()));

        if (view.getCreateTime() != null) {
            builder.setCreateTime(view.getCreateTime().getTime());
        }

        for (SeatView seat : view.getSeats()) {
            builder.addSeats(toProtoSeatView(seat));
        }

        return builder.build();
    }

    public static SeatViewProto toProtoSeatView(SeatView view) {
        if (view == null) return SeatViewProto.getDefaultInstance();
        return SeatViewProto.newBuilder()
                .setPlayerName(view.getPlayerName() != null ? view.getPlayerName() : "")
                .setPlayerType(view.getPlayerType() != null ? view.getPlayerType().toString() : "")
                .build();
    }

    // GameView conversion
    public static GameViewProto toProtoGameView(GameView view) {
        if (view == null) return GameViewProto.getDefaultInstance();

        PlayerView myPlayer = view.getMyPlayer();
        java.util.UUID myPlayerId = myPlayer != null ? myPlayer.getPlayerId() : null;

        GameViewProto.Builder builder = GameViewProto.newBuilder()
                .setPriorityTime(view.getPriorityTime())
                .setMyPlayerId(toProtoUuid(myPlayerId))
                .setPhase(view.getPhase() != null ? view.getPhase().toString() : "")
                .setStep(view.getStep() != null ? view.getStep().toString() : "")
                .setActivePlayerId(toProtoUuid(view.getActivePlayerId()))
                .setActivePlayerName(view.getActivePlayerName() != null ? view.getActivePlayerName() : "")
                .setPriorityPlayerName(view.getPriorityPlayerName() != null ? view.getPriorityPlayerName() : "")
                .setTurn(view.getTurn())
                .setSpecial(view.getSpecial())
                .setRollbackTurnsAllowed(view.isRollbackTurnsAllowed());

        for (PlayerView player : view.getPlayers()) {
            builder.addPlayers(toProtoPlayerView(player));
        }

        builder.setMyHand(toProtoCardsView(view.getMyHand()));
        builder.setStack(toProtoCardsView(view.getStack()));

        for (ExileView exile : view.getExile()) {
            builder.addExiles(toProtoExileView(exile));
        }

        for (RevealedView revealed : view.getRevealed()) {
            builder.addRevealed(toProtoRevealedView(revealed));
        }

        for (LookedAtView lookedAt : view.getLookedAt()) {
            builder.addLookedAt(toProtoLookedAtView(lookedAt));
        }

        for (CombatGroupView combat : view.getCombat()) {
            builder.addCombat(toProtoCombatGroupView(combat));
        }

        return builder.build();
    }

    public static PlayerViewProto toProtoPlayerView(PlayerView view) {
        if (view == null) return PlayerViewProto.getDefaultInstance();
        PlayerViewProto.Builder builder = PlayerViewProto.newBuilder()
                .setPlayerId(toProtoUuid(view.getPlayerId()))
                .setName(view.getName() != null ? view.getName() : "")
                .setLife(view.getLife())
                .setLibraryCount(view.getLibraryCount())
                .setHandCount(view.getHandCount())
                .setIsActive(view.isActive())
                .setHasPriority(view.hasPriority())
                .setTimerActive(view.isTimerActive())
                .setHasLeft(view.hasLeft())
                .setPassedTurn(view.isPassedTurn())
                .setPassedUntilEndOfTurn(view.isPassedUntilEndOfTurn())
                .setPassedUntilNextMain(view.isPassedUntilNextMain())
                .setPassedUntilStackResolved(view.isPassedUntilStackResolved())
                .setPassedAllTurns(view.isPassedAllTurns())
                .setPassedUntilEndStepBeforeMyTurn(view.isPassedUntilEndStepBeforeMyTurn())
                .setMonarch(view.isMonarch())
                .setInitiative(view.isInitiative())
                .setPriorityTimeLeftSecs(view.getPriorityTimeLeftSecs());

        if (view.getManaPool() != null) {
            builder.setManaPool(toProtoManaPoolView(view.getManaPool()));
        }

        builder.setGraveyard(toProtoCardsView(view.getGraveyard()));
        builder.setExile(toProtoCardsView(view.getExile()));

        for (CounterView counter : view.getCounters()) {
            builder.addCounters(toProtoCounterView(counter));
        }

        for (Map.Entry<java.util.UUID, PermanentView> entry : view.getBattlefield().entrySet()) {
            builder.putBattlefield(entry.getKey().toString(), toProtoPermanentView(entry.getValue()));
        }

        return builder.build();
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

    public static CounterViewProto toProtoCounterView(CounterView view) {
        if (view == null) return CounterViewProto.getDefaultInstance();
        return CounterViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setCount(view.getCount())
                .build();
    }

    public static CardsViewProto toProtoCardsView(CardsView view) {
        if (view == null) return CardsViewProto.getDefaultInstance();
        CardsViewProto.Builder builder = CardsViewProto.newBuilder();
        for (Map.Entry<java.util.UUID, CardView> entry : view.entrySet()) {
            builder.putCards(entry.getKey().toString(), toProtoCardView(entry.getValue()));
        }
        return builder.build();
    }

    public static CardViewProto toProtoCardView(CardView view) {
        if (view == null) return CardViewProto.getDefaultInstance();
        CardViewProto.Builder builder = CardViewProto.newBuilder()
                .setId(toProtoUuid(view.getId()))
                .setExpansionSetCode(view.getExpansionSetCode() != null ? view.getExpansionSetCode() : "")
                .setCardNumber(view.getCardNumber() != null ? view.getCardNumber() : "")
                .setUsesVariousArt(view.getUsesVariousArt())
                .setName(view.getName() != null ? view.getName() : "")
                .setDisplayName(view.getDisplayName() != null ? view.getDisplayName() : "")
                .setDisplayFullName(view.getDisplayFullName() != null ? view.getDisplayFullName() : "")
                .setPower(view.getPower() != null ? view.getPower() : "")
                .setToughness(view.getToughness() != null ? view.getToughness() : "")
                .setLoyalty(view.getLoyalty() != null ? view.getLoyalty() : "")
                .setManaValue(view.getManaValue())
                .setRarity(toProtoRarity(view.getRarity()))
                .setIsAbility(view.isAbility())
                .setIsToken(view.isToken())
                .setTransformed(view.isTransformed())
                .setFlipCard(view.isFlipCard())
                .setFaceDown(view.isFaceDown())
                .setIsSplitCard(view.isSplitCard())
                .setPaid(view.isPaid())
                .setControlledByOwner(view.isControlledByOwner())
                .setZone(toProtoZone(view.getZone()))
                .setCanAttack(view.isCanAttack())
                .setCanBlock(view.isCanBlock());

        if (view.getColor() != null) {
            builder.setColor(toProtoColor(view.getColor()));
        }
        if (view.getFrameColor() != null) {
            builder.setFrameColor(toProtoColor(view.getFrameColor()));
        }

        for (mage.constants.CardType cardType : view.getCardTypes()) {
            builder.addCardTypes(toProtoCardType(cardType));
        }
        for (mage.constants.SuperType superType : view.getSuperTypes()) {
            builder.addSuperTypes(toProtoSuperType(superType));
        }
        // SubTypes returns SubTypes object which has a stream
        for (SubType subType : view.getSubTypes()) {
            builder.addSubTypes(subType.toString());
        }
        for (String rule : view.getRules()) {
            builder.addRules(rule);
        }
        for (java.util.UUID target : view.getTargets()) {
            builder.addTargets(target.toString());
        }

        return builder.build();
    }

    public static PermanentViewProto toProtoPermanentView(PermanentView view) {
        if (view == null) return PermanentViewProto.getDefaultInstance();
        return PermanentViewProto.newBuilder()
                .setCard(toProtoCardView(view))
                .setTapped(view.isTapped())
                .setFlipped(view.isFlipped())
                .setPhasedIn(view.isPhasedIn())
                .setDamage(view.getDamage())
                .setControlled(view.isControlled())
                .setControllerName(view.getNameController() != null ? view.getNameController() : "")
                .build();
    }

    public static ExileViewProto toProtoExileView(ExileView view) {
        if (view == null) return ExileViewProto.getDefaultInstance();
        // ExileView extends CardsView, so the view itself IS the cards
        return ExileViewProto.newBuilder()
                .setId(toProtoUuid(view.getId()))
                .setName(view.getName() != null ? view.getName() : "")
                .setCards(toProtoCardsView(view))
                .build();
    }

    public static RevealedViewProto toProtoRevealedView(RevealedView view) {
        if (view == null) return RevealedViewProto.getDefaultInstance();
        return RevealedViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setCards(toProtoCardsView(view.getCards()))
                .build();
    }

    public static LookedAtViewProto toProtoLookedAtView(LookedAtView view) {
        if (view == null) return LookedAtViewProto.getDefaultInstance();
        return LookedAtViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setCards(toProtoSimpleCardsView(view.getCards()))
                .build();
    }

    public static SimpleCardsViewProto toProtoSimpleCardsView(SimpleCardsView view) {
        if (view == null) return SimpleCardsViewProto.getDefaultInstance();
        SimpleCardsViewProto.Builder builder = SimpleCardsViewProto.newBuilder();
        for (Map.Entry<java.util.UUID, SimpleCardView> entry : view.entrySet()) {
            builder.putCards(entry.getKey().toString(), toProtoSimpleCardView(entry.getValue()));
        }
        return builder.build();
    }

    public static SimpleCardViewProto toProtoSimpleCardView(SimpleCardView view) {
        if (view == null) return SimpleCardViewProto.getDefaultInstance();
        return SimpleCardViewProto.newBuilder()
                .setId(toProtoUuid(view.getId()))
                .setExpansionSetCode(view.getExpansionSetCode() != null ? view.getExpansionSetCode() : "")
                .setCardNumber(view.getCardNumber() != null ? view.getCardNumber() : "")
                .setUsesVariousArt(view.getUsesVariousArt())
                .build();
    }

    public static CombatGroupViewProto toProtoCombatGroupView(CombatGroupView view) {
        if (view == null) return CombatGroupViewProto.getDefaultInstance();
        CombatGroupViewProto.Builder builder = CombatGroupViewProto.newBuilder()
                .setAttackedId(toProtoUuid(view.getDefenderId()));

        for (CardView attacker : view.getAttackers().values()) {
            builder.addAttackers(toProtoUuid(attacker.getId()));
        }
        for (CardView blocker : view.getBlockers().values()) {
            builder.addBlockers(toProtoUuid(blocker.getId()));
        }

        return builder.build();
    }

    // TournamentView conversion
    public static TournamentViewProto toProtoTournamentView(TournamentView view) {
        if (view == null) return TournamentViewProto.getDefaultInstance();
        TournamentViewProto.Builder builder = TournamentViewProto.newBuilder()
                .setTournamentName(view.getTournamentName() != null ? view.getTournamentName() : "")
                .setTournamentType(view.getTournamentType() != null ? view.getTournamentType() : "")
                .setTournamentState(view.getTournamentState() != null ? view.getTournamentState() : "");

        for (TournamentPlayerView player : view.getPlayers()) {
            builder.addPlayers(toProtoTournamentPlayerView(player));
        }

        for (RoundView round : view.getRounds()) {
            builder.addRounds(toProtoRoundView(round));
        }

        return builder.build();
    }

    public static TournamentPlayerViewProto toProtoTournamentPlayerView(TournamentPlayerView view) {
        if (view == null) return TournamentPlayerViewProto.getDefaultInstance();
        return TournamentPlayerViewProto.newBuilder()
                .setName(view.getName() != null ? view.getName() : "")
                .setState(view.getState() != null ? view.getState() : "")
                .setResults(view.getResults() != null ? view.getResults() : "")
                .setPoints(view.getPoints())
                .setQuit(view.hasQuit())
                .build();
    }

    public static RoundViewProto toProtoRoundView(RoundView view) {
        if (view == null) return RoundViewProto.getDefaultInstance();
        RoundViewProto.Builder builder = RoundViewProto.newBuilder();
        for (TournamentGameView game : view.getGames()) {
            builder.addGames(toProtoTournamentGameView(game));
        }
        return builder.build();
    }

    public static TournamentGameViewProto toProtoTournamentGameView(TournamentGameView view) {
        if (view == null) return TournamentGameViewProto.getDefaultInstance();
        TournamentGameViewProto.Builder builder = TournamentGameViewProto.newBuilder()
                .setGameId(toProtoUuid(view.getGameId()))
                .setTableId(toProtoUuid(view.getTableId()))
                .setState(view.getState() != null ? view.getState() : "")
                .setResult(view.getResult() != null ? view.getResult() : "");

        for (String player : view.getPlayers()) {
            builder.addPlayers(player);
        }

        return builder.build();
    }

    // DraftPickView conversion
    public static DraftPickViewProto toProtoDraftPickView(DraftPickView view) {
        if (view == null) return DraftPickViewProto.getDefaultInstance();
        return DraftPickViewProto.newBuilder()
                .setBooster(toProtoSimpleCardsView(view.getBooster()))
                .setPicks(toProtoSimpleCardsView(view.getPicks()))
                .build();
    }

    // MatchView conversion
    public static MatchViewProto toProtoMatchView(MatchView view) {
        if (view == null) return MatchViewProto.getDefaultInstance();
        MatchViewProto.Builder builder = MatchViewProto.newBuilder()
                .setMatchId(toProtoUuid(view.getMatchId()))
                .setMatchName(view.getName() != null ? view.getName() : "")
                .setGameType(view.getGameType() != null ? view.getGameType() : "")
                .setDeckType(view.getDeckType() != null ? view.getDeckType() : "")
                .setResult(view.getResult() != null ? view.getResult() : "")
                .setRated(view.isRated());

        // getPlayers() returns a single String, not List<String>
        String players = view.getPlayers();
        if (players != null && !players.isEmpty()) {
            builder.addPlayers(players);
        }

        return builder.build();
    }

    // UserView conversion
    public static UserViewProto toProtoUserView(UserView view) {
        if (view == null) return UserViewProto.getDefaultInstance();
        return UserViewProto.newBuilder()
                .setUserName(view.getUserName() != null ? view.getUserName() : "")
                .setHost(view.getHost() != null ? view.getHost() : "")
                .setSessionId(view.getSessionId() != null ? view.getSessionId() : "")
                .setTimeConnected(view.getTimeConnected() != null ? view.getTimeConnected().getTime() : 0)
                .setLastActivity(view.getLastActivity() != null ? view.getLastActivity().getTime() : 0)
                .setGameInfo(view.getGameInfo() != null ? view.getGameInfo() : "")
                .setUserState(view.getUserState() != null ? view.getUserState() : "")
                .setClientVersion(view.getClientVersion() != null ? view.getClientVersion() : "")
                .build();
    }

    // RoomUsersView conversion
    public static RoomUsersViewProto toProtoRoomUsersView(RoomUsersView view) {
        if (view == null) return RoomUsersViewProto.getDefaultInstance();
        RoomUsersViewProto.Builder builder = RoomUsersViewProto.newBuilder()
                .setRoomId(toProtoUuid(view.getRoomId()));

        for (UsersView user : view.getUsersView()) {
            builder.addUsers(toProtoUserFromUsersView(user));
        }

        return builder.build();
    }

    private static UserViewProto toProtoUserFromUsersView(UsersView view) {
        if (view == null) return UserViewProto.getDefaultInstance();
        return UserViewProto.newBuilder()
                .setUserName(view.getUserName() != null ? view.getUserName() : "")
                .setGameInfo(view.getInfoState() != null ? view.getInfoState() : "")
                .build();
    }

    // ChatMessage conversion
    public static ChatMessageProto toProtoChatMessage(ChatMessage msg) {
        if (msg == null) return ChatMessageProto.getDefaultInstance();
        return ChatMessageProto.newBuilder()
                .setUsername(msg.getUsername() != null ? msg.getUsername() : "")
                .setTime(msg.getTime() != null ? msg.getTime().getTime() : 0)
                .setTurnInfo(msg.getTurnInfo() != null ? msg.getTurnInfo() : "")
                .setMessage(msg.getMessage() != null ? msg.getMessage() : "")
                .setColor(toProtoMessageColor(msg.getColor()))
                .setMessageType(toProtoMessageType(msg.getMessageType()))
                .build();
    }

    public static MessageColor toProtoMessageColor(ChatMessage.MessageColor color) {
        if (color == null) return MessageColor.MESSAGE_COLOR_UNSPECIFIED;
        switch (color) {
            case BLACK: return MessageColor.MESSAGE_COLOR_BLACK;
            case RED: return MessageColor.MESSAGE_COLOR_RED;
            case GREEN: return MessageColor.MESSAGE_COLOR_GREEN;
            case BLUE: return MessageColor.MESSAGE_COLOR_BLUE;
            case ORANGE: return MessageColor.MESSAGE_COLOR_ORANGE;
            case YELLOW: return MessageColor.MESSAGE_COLOR_YELLOW;
            default: return MessageColor.MESSAGE_COLOR_UNSPECIFIED;
        }
    }

    public static MessageType toProtoMessageType(ChatMessage.MessageType type) {
        if (type == null) return MessageType.MESSAGE_TYPE_UNSPECIFIED;
        switch (type) {
            case USER_INFO: return MessageType.MESSAGE_TYPE_USER_INFO;
            case STATUS: return MessageType.MESSAGE_TYPE_STATUS;
            case GAME: return MessageType.MESSAGE_TYPE_GAME;
            case TALK: return MessageType.MESSAGE_TYPE_TALK;
            case WHISPER_FROM: return MessageType.MESSAGE_TYPE_WHISPER_FROM;
            case WHISPER_TO: return MessageType.MESSAGE_TYPE_WHISPER_TO;
            default: return MessageType.MESSAGE_TYPE_UNSPECIFIED;
        }
    }

    // GameEndView conversion
    public static GameEndViewProto toProtoGameEndView(GameEndView view) {
        if (view == null) return GameEndViewProto.getDefaultInstance();
        GameEndViewProto.Builder builder = GameEndViewProto.newBuilder()
                .setYouWon(view.hasWon());
        return builder.build();
    }
}
