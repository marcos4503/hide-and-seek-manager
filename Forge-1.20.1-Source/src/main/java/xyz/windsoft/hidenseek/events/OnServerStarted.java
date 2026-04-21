package xyz.windsoft.hidenseek.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.hidenseek.utils.GameManagerLevelData;
import xyz.windsoft.hidenseek.utils.GameManagerLogic;

/*
 * This class do actions when the Server is Started and Ready to play.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnServerStarted {

    //Public events

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        //Get the Overworld Level fresh reference
        ServerLevel serverLevel = event.getServer().getLevel(Level.OVERWORLD);

        //If the level is null, stop here
        if (serverLevel == null)
            return;

        //Read the information of NBT of the Level, to check if have a game that was running, on the last Server session
        GameManagerLevelData data = GameManagerLevelData.get(serverLevel);

        //If don't have a game running in the last session, stop here
        if (data.isGameRunning() == false)
            return;

        //Force the Finish of the State Machine of the game
        GameManagerLogic.ForceRunFinishOfTheGameStateMachine(data.GetGameTotemHeadPos());
    }
}
