
package mage.cards.r;

import java.util.UUID;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DoUnlessAnyPlayerPaysEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.cards.CardImpl;
import mage.constants.CardType;
import mage.filter.common.FilterCreatureCard;
import mage.target.common.TargetCardInYourGraveyard;

/**
 *
 * @author Ryan-Saklad
 */

public final class SoulStrings extends CardImpl {

    public SoulStrings(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.SORCERY},"{X}{B}");

        // Return two target creature cards from your graveyard to your hand unless any player pays X.
        Effect effect = new DoUnlessAnyPlayerPaysEffect(new ReturnFromGraveyardToHandTargetEffect(), source.getManaCostsToPay().getX());
        this.getSpellAbility().addTarget(new TargetCardInYourGraveyard(2, new FilterCreatureCard("creature cards from your graveyard")));
        effect.setText("Return two target creature cards from your graveyard to your hand unless any player pays {X}");
        
    }

    public SoulStrings(final SoulStrings card) {
        super(card);
    }

    @Override
    public SoulStrings copy() {
        return new SoulStrings(this);
    }
}
