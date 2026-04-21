package xyz.windsoft.hidenseek.inventory;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.block.ModBlocks;
import xyz.windsoft.hidenseek.item.ModItems;

/*
 * This class is responsible by the dedicated creative tab of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModCreativeTab {

    //Public static final variables
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);

    //Public static variables
    public static RegistryObject<CreativeModeTab> HIDE_AND_SEEK_TAB = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the creative mode tabs of this mod in the event bus of Forge
        CREATIVE_MODE_TABS.register(eventBus);

        //Create the "Hide And Seek" custom creative tab
        HIDE_AND_SEEK_TAB = CREATIVE_MODE_TABS.register("hide_and_seek_tab", () ->
        {
            //Prepare and set up the tab
            CreativeModeTab creativeModeTab = CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> { return ModItems.MOD_BADGE.get().getDefaultInstance(); })  //<- Use the "Mod Badge" as tab icon
                    .title(Component.literal("Hide And Seek Manager"))
                    .displayItems((params, output) -> {
                        //Add items to display in this custom creative tab
                        output.accept(ModItems.MOD_BADGE.get());
                        output.accept(ModItems.MOD_VERSION_CHECKER.get());
                        output.accept(ModItems.ADRENALINE_INJECTION.get());
                        output.accept(ModItems.WHISTLE.get());
                        output.accept(ModItems.RAGE_BAITER.get());
                        output.accept(ModItems.TOTEM_TRACKER.get());
                        output.accept(ModItems.SMOKE_BOMB.get());
                        output.accept(ModItems.ZERO_GRAVITY.get());
                        output.accept(ModItems.LEAP_OF_FAITH.get());
                        output.accept(ModItems.CAMOUFLAGE.get());
                        output.accept(ModItems.LADDER_SPECIALIST.get());
                        output.accept(ModItems.SOUND_BAIT.get());
                        output.accept(ModItems.BREATHING_UNDERWATER.get());
                        output.accept(ModBlocks.GAME_TOTEM_HEAD_DEPRECATED_BLOCK.get());
                        output.accept(ModBlocks.GAME_TOTEM_HEAD_POWERED_BLOCK.get());
                        output.accept(ModBlocks.GAME_TOTEM_BODY_BLOCK.get());
                        output.accept(ModBlocks.GAME_TOTEM_SIDE_BLOCK.get());
                        output.accept(ModBlocks.GAME_TOTEM_PILLAR_BLOCK.get());
                    })
                    .build();
            return creativeModeTab;
        });
    }
}
