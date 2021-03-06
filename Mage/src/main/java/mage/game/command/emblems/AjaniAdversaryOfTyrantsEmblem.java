package mage.game.command.emblems;

import mage.abilities.common.BeginningOfEndStepTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.command.Emblem;
import mage.game.permanent.token.CatToken2;

/**
 *
 * @author TheElk801
 */
public class AjaniAdversaryOfTyrantsEmblem extends Emblem {

    // −7: You get an emblem with "At the beginning of your end step, create three 1/1 white Cat creature tokens with lifelink."
    public AjaniAdversaryOfTyrantsEmblem() {
        this.setName("Emblem Ajani");
        this.setExpansionSetCodeForImage("M19");
        this.getAbilities().add(new BeginningOfEndStepTriggeredAbility(
                Zone.COMMAND, new CreateTokenEffect(new CatToken2(), 3),
                TargetController.YOU, null, false
        ));
    }
}
