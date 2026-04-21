package xyz.windsoft.hidenseek.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/*
 * This class creates the custom behavior for the effect of "Fall Arrest"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class FallArrest extends MobEffect {

    //Public methods

    public FallArrest() {
        //Repass the properties to parent class of this class
        super(MobEffectCategory.BENEFICIAL, 0xD5D907); //<- Use a yellow for the effect
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        //Alwayes return "true", because the code of this Effect will be processed in "onLivingFall"...
        return true;
    }
}