package xyz.windsoft.hidenseek.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.item.custom.*;

/*
 * This class is responsible by the registering of items of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModItems {

    //Public static final variables
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    //Public static variables
    public static RegistryObject<Item> MOD_BADGE = null;
    public static RegistryObject<Item> MOD_VERSION_CHECKER = null;
    public static RegistryObject<Item> ADRENALINE_INJECTION = null;
    public static RegistryObject<Item> WHISTLE = null;
    public static RegistryObject<Item> RAGE_BAITER = null;
    public static RegistryObject<Item> TOTEM_TRACKER = null;
    public static RegistryObject<Item> SMOKE_BOMB = null;
    public static RegistryObject<Item> ZERO_GRAVITY = null;
    public static RegistryObject<Item> LEAP_OF_FAITH = null;
    public static RegistryObject<Item> CAMOUFLAGE = null;
    public static RegistryObject<Item> LADDER_SPECIALIST = null;
    public static RegistryObject<Item> SOUND_BAIT = null;
    public static RegistryObject<Item> BREATHING_UNDERWATER = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for items of this mod in the event bus of Forge
        ITEMS.register(eventBus);

        //Register the "Mod Badge" item...
        MOD_BADGE = ITEMS.register("mod_badge", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(16);
            return new Item(props);
        });
        //Register the "Mod Version Checker" item...
        MOD_VERSION_CHECKER = ITEMS.register("mod_version_checker", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new ModVersionCheckerItem(props);
        });
        //Register the "Adrenaline Injection" item...
        ADRENALINE_INJECTION = ITEMS.register("adrenaline_injection", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new Item(props);
        });
        //Register the "Whistle" item...
        WHISTLE = ITEMS.register("whistle", () -> {
            //Set up th eitem properties and return it for registeer
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new WhistleItem(props);
        });
        //Register the "Rage Baiter" item...
        RAGE_BAITER = ITEMS.register("rage_baiter", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new RageBaiterItem(props);
        });
        //Register the "Totem Tracker" item...
        TOTEM_TRACKER = ITEMS.register("totem_tracker", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new TotemTracker(props);
        });
        //Register the "Smoke Bomb" item...
        SMOKE_BOMB = ITEMS.register("smoke_bomb", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new SmokeBomb(props);
        });
        //Register the "Zero Gravity" item...
        ZERO_GRAVITY = ITEMS.register("zero_gravity", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new ZeroGravity(props);
        });
        //Register the "Leap of Faith" item...
        LEAP_OF_FAITH = ITEMS.register("leap_of_faith", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new LeapOfFaith(props);
        });
        //Register the "Camouflage" item...
        CAMOUFLAGE = ITEMS.register("camouflage", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new Camouflage(props);
        });
        //Register the "Ladder Specialist" item...
        LADDER_SPECIALIST = ITEMS.register("ladder_specialist", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new LadderSpecialist(props);
        });
        //Register the "Sound Bait" item...
        SOUND_BAIT = ITEMS.register("sound_bait", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new SoundBait(props);
        });
        //Register the "Breathing Underwater" item...
        BREATHING_UNDERWATER = ITEMS.register("breathing_underwater", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(1);
            return new BreathingUnderwater(props);
        });
    }

    public static void RegisterForClientOnly(FMLClientSetupEvent event){
        //Register the property "not_ready" for "Totem Tracker" item...
        ItemProperties.register(ModItems.TOTEM_TRACKER.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((TotemTracker)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
        //Register the property "not_ready" for "Smoke Bomb" item...
        ItemProperties.register(ModItems.SMOKE_BOMB.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((SmokeBomb)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
        //Register the property "not_ready" for "Zero Gravity" item...
        ItemProperties.register(ModItems.ZERO_GRAVITY.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((ZeroGravity)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
        //Register the property "not_ready" for "Leap of Faith" item...
        ItemProperties.register(ModItems.LEAP_OF_FAITH.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((LeapOfFaith)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
        //Register the property "not_ready" for "Camouflage" item...
        ItemProperties.register(ModItems.CAMOUFLAGE.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((Camouflage)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
        //Register the property "not_ready" for "Ladder Specialist" item...
        ItemProperties.register(ModItems.LADDER_SPECIALIST.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((LadderSpecialist)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
        //Register the property "not_ready" for "Sound Bait" item...
        ItemProperties.register(ModItems.SOUND_BAIT.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((SoundBait)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
        //Register the property "not_ready" for "Breathing Underwater" item...
        ItemProperties.register(ModItems.BREATHING_UNDERWATER.get(), new ResourceLocation(Main.MODID, "not_ready"), (stack, level, entity, seed) -> { return ((BreathingUnderwater)stack.getItem()).GetItemNotReadyState(stack, level, entity, seed); });
    }
}
