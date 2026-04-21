package xyz.windsoft.hidenseek.entity;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.effect.custom.LadderSpecialistEffect;
import xyz.windsoft.hidenseek.entity.client.SoundBaitEntityRenderer;
import xyz.windsoft.hidenseek.entity.custom.SoundBaitEntity;

/*
 * This class is responsible by the registering of Entities of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModEntities {

    //Public static final variables
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Main.MODID);

    //Public static variables
    public static RegistryObject<EntityType<SoundBaitEntity>> SOUND_BAIT = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for entities of this mod in the event bus of Forge
        ENTITIES.register(eventBus);

        //Register the "Sound Bait" Entity...
        SOUND_BAIT = ENTITIES.register("sound_bait", () -> EntityType.Builder.<SoundBaitEntity>of(SoundBaitEntity::new, MobCategory.MISC)
                .sized(0.4f, 0.4f)
                .clientTrackingRange(128)
                .updateInterval(5)
                .build("sound_bait"));
    }

    public static void RegisterRendererForClientOnly(EntityRenderersEvent.RegisterRenderers event){
        //Register the Renderer that will be used by the "Sound Bait" entity...
        event.registerEntityRenderer(ModEntities.SOUND_BAIT.get(), SoundBaitEntityRenderer::new);
    }
}