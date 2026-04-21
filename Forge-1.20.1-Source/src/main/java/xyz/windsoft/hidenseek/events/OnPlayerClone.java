package xyz.windsoft.hidenseek.events;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/*
 * This class do actions on Players that respawns or are cloned by any means.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [X] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnPlayerClone {

    //Public events

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event){
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
        boolean thisCloningIsByDeath = event.isWasDeath();
        ServerPlayer oldServerPlayer = ((ServerPlayer) event.getOriginal());
        ServerPlayer newServerPlayer = ((ServerPlayer) event.getEntity());

        //Show a log, on death
        if (thisCloningIsByDeath == true)
            LogUtils.getLogger().info("The player " + newServerPlayer.getName().getString() + " was respawned.");
    }
}