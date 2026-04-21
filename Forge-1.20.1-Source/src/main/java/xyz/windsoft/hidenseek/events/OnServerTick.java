package xyz.windsoft.hidenseek.events;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import xyz.windsoft.hidenseek.utils.GameManagerLogic;

/*
 * This class do actions on each Tick of Server. Is mainly used to run the Hide'n Seek game logic.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [X] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnServerTick {

    //Public events

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event){
        //If not is the logical server, stop here
        //if (FMLEnvironment.dist == Dist.CLIENT)
        //    return;

        //If is not the END of tick, stop here
        if (event.phase != TickEvent.Phase.END)
            return;



        //Do a tick in the Game Manager Logic
        GameManagerLogic.Tick(event);
    }
}