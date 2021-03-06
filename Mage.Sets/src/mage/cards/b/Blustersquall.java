
package mage.cards.b;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.abilities.keyword.OverloadAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.ControllerPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;


/**
 *
 * @author LevelX2
 */
public final class Blustersquall extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creature you don't control");

    static {
        filter.add(new ControllerPredicate(TargetController.NOT_YOU));
    }

    public Blustersquall(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{U}");


        // Tap target creature you don't control.
        this.getSpellAbility().addTarget(new TargetCreaturePermanent(filter));
        this.getSpellAbility().addEffect(new TapTargetEffect());

        // Overload {3}{U} (You may cast this spell for its overload cost. If you do, change its text by replacing all instances of "target" with "each.")
        this.addAbility(new OverloadAbility(this, new BlustersqallTapAllEffect(filter), new ManaCostsImpl("{3}{U}")));

    }

    public Blustersquall(final Blustersquall card) {
        super(card);
    }

    @Override
    public Blustersquall copy() {
        return new Blustersquall(this);
    }
}

class BlustersqallTapAllEffect extends OneShotEffect {

    protected FilterCreaturePermanent filter;

    public BlustersqallTapAllEffect(FilterCreaturePermanent filter) {
        super(Outcome.Tap);
        this.filter = filter;
        staticText = "Tap each creature you don't control";
    }

    public BlustersqallTapAllEffect(final BlustersqallTapAllEffect effect) {
        super(effect);
        this.filter = effect.filter;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Permanent creature : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source.getSourceId(), game)) {
            creature.tap(game);
        }
        return true;
    }

    @Override
    public BlustersqallTapAllEffect copy() {
        return new BlustersqallTapAllEffect(this);
    }
}
