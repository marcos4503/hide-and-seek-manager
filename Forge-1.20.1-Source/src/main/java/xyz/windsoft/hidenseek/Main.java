package xyz.windsoft.hidenseek;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import xyz.windsoft.hidenseek.block.ModBlockEntities;
import xyz.windsoft.hidenseek.block.ModBlocks;
import xyz.windsoft.hidenseek.config.Config;
import xyz.windsoft.hidenseek.effect.ModEffects;
import xyz.windsoft.hidenseek.entity.ModEntities;
import xyz.windsoft.hidenseek.events.*;
import xyz.windsoft.hidenseek.inventory.ModCreativeTab;
import xyz.windsoft.hidenseek.item.ModItems;
import xyz.windsoft.hidenseek.network.ModPacketHandler;
import xyz.windsoft.hidenseek.screen.ModMenuToScreenConnector;
import xyz.windsoft.hidenseek.screen.ModMenuTypes;
import xyz.windsoft.hidenseek.sounds.ModSounds;

/*
 * This class is the Entry Point for this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main
{
    //Public classes
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        //Can use "@Mod.EventBusSubscriber" to automatically register all static methods in the class annotated with @SubscribeEvent...

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            //Some client setup code
            LOGGER.info("Hide And Seek Manager mod starting on client... >> {}", Minecraft.getInstance().getUser().getName());

            //Register the mod needed events, in forge mod event bus (ONLY FOR CLIENT)
            MinecraftForge.EVENT_BUS.register(new OnClientTick());
            MinecraftForge.EVENT_BUS.register(new OnClientOpenScreen());
            MinecraftForge.EVENT_BUS.register(new OnClientPlaySoundEvent());

            //Run needed code on Main Thread...
            event.enqueueWork(() -> {
                //Start the register of the needed mod items additional things
                ModItems.RegisterForClientOnly(event);
            });

            //Do the connections between mod Menus and Screens
            ModMenuToScreenConnector.DoConnections(event);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            //Start the reigster of the needed entities additional things
            ModEntities.RegisterRendererForClientOnly(event);
        }
    }

    //Public static variables
    public static final String MODID = "hidenseek";
    private static final Logger LOGGER = LogUtils.getLogger();

    //Public methods

    public Main(FMLJavaModLoadingContext context) {
        //Get the mod event bus
        IEventBus modEventBus = context.getModEventBus();

        //Register the mod needed events, in forge mod event bus
        MinecraftForge.EVENT_BUS.register(new OnServerStarted());
        MinecraftForge.EVENT_BUS.register(new OnServerTick());
        MinecraftForge.EVENT_BUS.register(new OnPlayerLogin());
        MinecraftForge.EVENT_BUS.register(new OnPlayerClone());
        MinecraftForge.EVENT_BUS.register(new OnDamage());
        MinecraftForge.EVENT_BUS.register(new OnLivingFall());
        MinecraftForge.EVENT_BUS.register(new OnPlayerAttack());
        MinecraftForge.EVENT_BUS.register(new OnRightClickBlock());
        MinecraftForge.EVENT_BUS.register(new OnPlayerTossItem());
        MinecraftForge.EVENT_BUS.register(new OnItemPickup());
        MinecraftForge.EVENT_BUS.register(new OnPlayerTick());
        MinecraftForge.EVENT_BUS.register(new OnRenderGui());

        //Start the register of the mod custom creative tab
        ModCreativeTab.Register(modEventBus);
        //Start the register of the needed mod items
        ModItems.Register(modEventBus);
        //Start the register of the needed mod blocks
        ModBlocks.Register(modEventBus);
        //Start the register of the needed mod block entities
        ModBlockEntities.Register(modEventBus);
        //Start the register of the needed mod menus
        ModMenuTypes.Register(modEventBus);
        //Start the register of the needed mod sounds events
        ModSounds.Register(modEventBus);
        //Start the register of the needed mod effects
        ModEffects.Register(modEventBus);
        //Start the register of the needed mod entities
        ModEntities.Register(modEventBus);

        //Register the "CommonSetup" method for modloading
        modEventBus.addListener(this::CommonSetup);

        //Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //Register the mod ForgeConfigSpec, for Forge can create and load the config file
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        //Can use "@SubscribeEvent" and let the Event Bus discover methods to call...

        //Do something when the server starts
        LOGGER.info("Hide And Seek Manager mod starting on server...");
    }

    //Private methods

    private void CommonSetup(final FMLCommonSetupEvent event) {
        //Do enqueued work
        event.enqueueWork(() -> {
            //Start the register of the mod custom packets
            ModPacketHandler.Register();
        });

        //Some common setup code
        LOGGER.info("Hide And Seek Manager mod starting!");
        LOGGER.info("Configs loaded...");
        LOGGER.info("gameCountTime: " + Config.gameCountTime);
    }
}
