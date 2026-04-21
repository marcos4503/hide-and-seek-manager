package xyz.windsoft.hidenseek.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/*
 * This class creates the custom behavior for the effect of "Ladder Specialist"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class LadderSpecialistEffect extends MobEffect {

    //Public methods

    public LadderSpecialistEffect() {
        //Repass the properties to parent class of this class
        super(MobEffectCategory.BENEFICIAL, 0x3BA3D0); //<- Use a blue for the effect
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        //Alwayes return "true", because the code of this Effect will be processed in "onLivingTick"...
        return true;
    }
}