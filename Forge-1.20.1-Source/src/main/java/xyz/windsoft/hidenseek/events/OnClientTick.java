package xyz.windsoft.hidenseek.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.hidenseek.network.ClientToServer_ItemCooldownConfirmation;
import xyz.windsoft.hidenseek.network.ModPacketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This class do actions on Players on each Tick of the Client.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnClientTick {

    //Private final variables
    private final Map<Item, Boolean> ITEMS_ON_COOLDOWN_ON_LAST_TICK = new HashMap<>();
    private final List<Item> ITEMS_TO_CLEAR_FROM_HASHMAP = new ArrayList<>();

    //Public events

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        //If is not the END phase, stop here
        if (event.phase != TickEvent.Phase.END)
            return;

        //Get the Local Player data
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        //If don't have a Local Player, stop here
        if (localPlayer == null)
            return;

        //Get the map of Cooldowns, for this Player
        ItemCooldowns playerCooldowns = localPlayer.getCooldowns();
        //Detect items currently on Cooldown, and items that was on Cooldown and don't is now, on the Player Inventory
        for (ItemStack currentStack : localPlayer.getInventory().items)
            if (currentStack.isEmpty() == false){
                //Get the information if the item is on Cooldown now
                boolean isOnCooldown = playerCooldowns.isOnCooldown(currentStack.getItem());
                //If is now in Cooldown...
                if (isOnCooldown == true)
                    if (ITEMS_ON_COOLDOWN_ON_LAST_TICK.containsKey(currentStack.getItem()) == false)
                        ITEMS_ON_COOLDOWN_ON_LAST_TICK.put(currentStack.getItem(), true);   //<- Add to the list of Items on Cooldown on last Tick
                //If is not on Cooldown...
                if (isOnCooldown == false)
                    if (ITEMS_ON_COOLDOWN_ON_LAST_TICK.containsKey(currentStack.getItem()) == true)
                        if (ITEMS_ON_COOLDOWN_ON_LAST_TICK.get(currentStack.getItem()).booleanValue() == true){
                            //Send a Packet to request if the Cooldown was really finished on the Server...
                            ModPacketHandler.SendToServer(new ClientToServer_ItemCooldownConfirmation(Item.getId(currentStack.getItem())));
                            //Now that we found that this Item was on Cooldown on last Tick, but now, is not in Cooldown, remove it fromn the HashMap
                            ITEMS_ON_COOLDOWN_ON_LAST_TICK.remove(currentStack.getItem());
                        }
            }

        //Check if have any Item on the Hashmap, that don't exists more on the Player inventory
        ITEMS_ON_COOLDOWN_ON_LAST_TICK.forEach((currentItem, onCooldownOnLastTick) -> {
            //Prepare the response...
            boolean wasFoundCurrentItem = false;
            //Iterate on the Player inventory, to check if the current item exists...
            for (ItemStack currentStack : localPlayer.getInventory().items)
                if (currentStack.isEmpty() == false)
                    if (currentStack.is(currentItem) == true){
                        wasFoundCurrentItem = true;
                        break;
                    }
            //If the item was not found on the Inventory, add the item to the List of pending items to be cleared from the Hashmap
            if (wasFoundCurrentItem == false)
                ITEMS_TO_CLEAR_FROM_HASHMAP.add(currentItem);
        });
        //If have items to be cleared from the Hashmap, clear it...
        if (ITEMS_TO_CLEAR_FROM_HASHMAP.size() > 0){
            //Remove all needed items from the Hashmap
            for (Item currentItem : ITEMS_TO_CLEAR_FROM_HASHMAP)
                if (ITEMS_ON_COOLDOWN_ON_LAST_TICK.containsKey(currentItem) == true)
                    ITEMS_ON_COOLDOWN_ON_LAST_TICK.remove(currentItem);
            //Clear the list of pending items to be cleared...
            ITEMS_TO_CLEAR_FROM_HASHMAP.clear();
        }
    }
}