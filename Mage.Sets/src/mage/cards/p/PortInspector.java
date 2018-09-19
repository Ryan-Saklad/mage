
package mage.cards.p;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.BecomesBlockedByCreatureTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

/**
 *
 * @author Ryan-Saklad
 */

public final class PortInspector extends CardImpl {

    public PortInspector(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{U}");
        this.subtype.add(SubType.HUMAN);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // Whenever Port Inspector becomes blocked, look at defending player's hand.
        this.addAbility(new BecomesBlockedByCreatureTriggeredAbility(new PortInspectorLookEffect(), false));
    }

    public PortInspector(final PortInspector card) {
        super(card);
    }

    @Override
    public PortInspector copy() {
        return new PortInspector(this);
    }
}

class PortInspectorLookEffect extends OneShotEffect {

    public PortInspectorLookEffect() {
        super(Outcome.Discard);
        this.staticText = "look at defending player's hand";
    }

    public PortInspectorLookEffect(final PortInspectorLookEffect effect) {
        super(effect);
    }

    @Override
    public PortInspectorLookEffect copy() {
        return new PortInspectorLookEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent blockingCreature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (blockingCreature != null) {
            Player opponent = game.getPlayer(blockingCreature.getControllerId());
            if (opponent != null) {
                game.getPlayer(this.controllerId()).lookAtCards(opponent.getHand())
                return true;
            }
        }
        return false;
    }
