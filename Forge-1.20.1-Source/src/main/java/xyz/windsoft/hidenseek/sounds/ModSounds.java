package xyz.windsoft.hidenseek.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.hidenseek.Main;

/*
 * This class is responsible by the registering of sounds of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModSounds {

    //Public static final variables
    public static final DeferredRegister<SoundEvent> SOUNDS_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MODID);

    //Public static variables
    public static RegistryObject<SoundEvent> WHISTLE_0 = null;
    public static RegistryObject<SoundEvent> WHISTLE_1 = null;
    public static RegistryObject<SoundEvent> WHISTLE_2 = null;
    public static RegistryObject<SoundEvent> WHISTLE_3 = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for sounds events of this mod in the event bus of Forge
        SOUNDS_EVENTS.register(eventBus);

        //Register the "Whistle 0" Sound Event...
        WHISTLE_0 = SOUNDS_EVENTS.register("whistle0", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "whistle0")));
        //Register the "Whistle 1" Sound Event...
        WHISTLE_1 = SOUNDS_EVENTS.register("whistle1", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "whistle1")));
        //Register the "Whistle 2" Sound Event...
        WHISTLE_2 = SOUNDS_EVENTS.register("whistle2", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "whistle2")));
        //Register the "Whistle 3" Sound Event...
        WHISTLE_3 = SOUNDS_EVENTS.register("whistle3", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "whistle3")));
    }
}