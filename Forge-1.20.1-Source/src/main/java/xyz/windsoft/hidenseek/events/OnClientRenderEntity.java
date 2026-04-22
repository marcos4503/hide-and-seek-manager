package xyz.windsoft.hidenseek.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/*
 * This class do actions when is rendered a Entity on Client.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnClientRenderEntity {

    //Private cache variables
    private String PLAYER_UUID_TAG_TO_SEARCH = "";
    private String PLAYER_NICKNAME_TO_SEARCH = "";

    //Public events

    @SubscribeEvent
    public void onRenderEntity(RenderLivingEvent.Pre<?, ?> event) {
        //Get needed data
        Minecraft minecraftInstance = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraftInstance.player;

        //If have a empty data, stop here
        if (localPlayer == null || minecraftInstance.gameMode == null)
            return;

        //Get more data
        GameType currentGameMode = minecraftInstance.gameMode.getPlayerMode();

        //If not is in Spectator, stop here
        if (currentGameMode != GameType.SPECTATOR)
            return;

        //Get extra data
        Entity entityToBeRendered = event.getEntity();

        //If don't have the Tag to be searched on cache, create it
        if (PLAYER_UUID_TAG_TO_SEARCH.equals("") == true)
            PLAYER_UUID_TAG_TO_SEARCH = (("owner_uuid_" + localPlayer.getUUID().toString()));
        //If don't have the Nickname to be searched on cache, create it
        if (PLAYER_NICKNAME_TO_SEARCH.equals("") == true)
            PLAYER_NICKNAME_TO_SEARCH = localPlayer.getDisplayName().getString();

        //If is a Allay or Vex...
        if ((entityToBeRendered instanceof Allay) == true || (entityToBeRendered instanceof Vex) == true)
            if (entityToBeRendered.hasCustomName() == true)
                if (entityToBeRendered.getCustomName().getString().equals(PLAYER_NICKNAME_TO_SEARCH) == true)
                    event.setCanceled(true);  //<- If the Allay/Vex have a Name corresponding to this Local Player Nickname, means that this Entity represents this Local Player, so, stop the renderization of this Allay/Vex
    }
}