package xyz.windsoft.hidenseek.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.effect.custom.FallArrest;
import xyz.windsoft.hidenseek.effect.custom.LadderSpecialistEffect;

/*
 * This class is responsible by the registering of effects of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModEffects {

    //Public static final variables
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Main.MODID);

    //Public static variables
    public static RegistryObject<MobEffect> LADDER_SPECIALIST = null;
    public static RegistryObject<MobEffect> FALL_ARREST = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for effects of this mod in the event bus of Forge
        EFFECTS.register(eventBus);

        //Register the "Ladder Specialist" Effect...
        LADDER_SPECIALIST = EFFECTS.register("ladder_specialist", LadderSpecialistEffect::new);
        //Register the "Fall Arrest" Effect...
        FALL_ARREST = EFFECTS.register("fall_arrest", FallArrest::new);
    }
}