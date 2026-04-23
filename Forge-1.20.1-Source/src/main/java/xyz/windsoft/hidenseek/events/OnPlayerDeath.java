package xyz.windsoft.hidenseek.events;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.hidenseek.utils.GameManagerLogic;

/*
 * This class do actions on Players when a Player dead. This is a security mecanism that will works if the "OnDamage.class" real dead
 * prevention fails. The real dead of Players is something that never should happen if this minigame. If this happens, this script kicks
 * the dead Player, so the Game Manager Logic interprets it as a Player LogOut in mid of the game, preventing the match of being bugged.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnPlayerDeath {

    //Public events

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
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

        //Kick the Player
        serverPlayer.connection.disconnect(Component.literal("There was an Internal Server Error while processing your death. Please, try connecting again.").withStyle(ChatFormatting.GOLD));
        //Inform to the Server clear all items on next tick (useful to clear the dropped items by this Player)
        GameManagerLogic.ClearAllDroppedItemsOnNextTick();
    }
}