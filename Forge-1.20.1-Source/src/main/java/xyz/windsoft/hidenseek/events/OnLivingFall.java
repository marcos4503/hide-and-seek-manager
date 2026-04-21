package xyz.windsoft.hidenseek.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.hidenseek.effect.ModEffects;

/*
 * This class do actions when a Entity receives Fall Damage.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnLivingFall {

    //Public events

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;

        //If the entity of this event, is not a Server Player, cancel
        if ((event.getEntity() instanceof ServerPlayer) == false)
            return;



        //Get the Server Player data
        ServerPlayer serverPlayer = ((ServerPlayer) event.getEntity());

        //If have the effect of Fall Arrest...
        if (serverPlayer.hasEffect(ModEffects.FALL_ARREST.get()) == true){
            //Calculate the new tolerance of Fall
            float gameDefaultTolerance = 3.0f;
            float extraTolerance = 3.5f;
            //Get this Fall distance
            float thisFallDistance = event.getDistance();
            //Calculate the new Fall distance
            float newFallDistance = Math.max(0.0f, (thisFallDistance - (gameDefaultTolerance + extraTolerance)));
            //Apply the new Fall distance, on this Fall
            event.setDistance(newFallDistance);
        }

        //Set the new fall damage
        event.setDamageMultiplier(0.1f);
    }
}