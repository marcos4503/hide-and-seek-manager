package xyz.windsoft.hidenseek.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/*
 * This class do actions on Players, when they drop some item.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnPlayerTossItem {

    //Public events

    @SubscribeEvent
    public void onPlayerTossItem(ItemTossEvent event) {
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;

        //If the entity of this event, is not a Server Player, cancel
        if ((event.getPlayer() instanceof ServerPlayer) == false)
            return;



        //Get the Server Player data
        ServerPlayer serverPlayer = ((ServerPlayer) event.getPlayer());
        ItemStack droppedItem = event.getEntity().getItem();

        //Send the item back to the Player
        boolean recovered = serverPlayer.getInventory().add(droppedItem);
        serverPlayer.containerMenu.broadcastChanges();

        //If the item was successfully recovered, warn the Player
        if (recovered == true)
            serverPlayer.displayClientMessage(Component.translatable("gui.hidenseek.game_stage.any.cant_drop_items").withStyle(ChatFormatting.RED), true);

        //Cancel the creation of the item entity, result of the drop, in the world
        event.setCanceled(true);
    }
}