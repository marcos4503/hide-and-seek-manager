package xyz.windsoft.hidenseek.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.hidenseek.item.ModItems;

/*
 * This class do actions on Players that collect items.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnItemPickup {

    //Public events

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
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
        ItemStack collectedItemStack = event.getItem().getItem();

        //If the collected Item Stack is a Adrenaline Injection, play sound of collect
        if (collectedItemStack.is(ModItems.ADRENALINE_INJECTION.get()) == true)
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
    }
}