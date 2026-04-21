package xyz.windsoft.hidenseek.screen;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xyz.windsoft.hidenseek.screen.screen.GameTotemHeadPoweredScreen;

/*
 * This class is responsible by the connection of the mod Menus and respective Screens, being that, Screens display the user interface in clients,
 * and Menus are responsible for the data and logic behind that display, like managing inventory slots and syncing data with the server, and are common
 * in Client and Server.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModMenuToScreenConnector {

    //Public static methods

    public static void DoConnections(FMLClientSetupEvent event){
        //Do the connection between Menus and Screens of this mod
        MenuScreens.register(ModMenuTypes.GAME_TOTEM_HEAD_POWERED_MENU.get(), GameTotemHeadPoweredScreen::new);
    }
}
