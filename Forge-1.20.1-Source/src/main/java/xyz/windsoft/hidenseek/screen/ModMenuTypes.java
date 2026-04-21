package xyz.windsoft.hidenseek.screen;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.screen.menu.GameTotemHeadPoweredMenu;

/*
 * This class is responsible by the registering of Menus of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModMenuTypes {

    //Public static final variables
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Main.MODID);

    //Public static variables
    public static RegistryObject<MenuType<GameTotemHeadPoweredMenu>> GAME_TOTEM_HEAD_POWERED_MENU = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for menus of this mod in the event bus of Forge
        MENUS.register(eventBus);

        //Register the "Game Totem Head Powered" menu...
        GAME_TOTEM_HEAD_POWERED_MENU = RegisterMenuType("game_totem_head_powered_menu", GameTotemHeadPoweredMenu::new);
    }

    //Private static auxiliar methods

    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> RegisterMenuType(String name, IContainerFactory<T> factory){
        //Register the Menu creating a Forge Menu Type using the Container Factory
        return MENUS.register(name, () -> { return IForgeMenuType.create(factory); });
    }
}
