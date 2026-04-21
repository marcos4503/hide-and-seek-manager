package xyz.windsoft.hidenseek.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/*
 * This class do actions on Players when they try to open their inventories.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnClientOpenScreen {

    //Public events

    @SubscribeEvent
    public void onClientOpenScreen(ScreenEvent.Opening event) {
        //Get the Local Player data
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        Screen requestedScreen = event.getScreen();

        //If is trying to open the Inventory, while have a Carved Pumpkin on Head, just stop it
        if (localPlayer != null)
            if (requestedScreen instanceof InventoryScreen)
                if (localPlayer.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN) == true)
                    event.setCanceled(true);
    }
}