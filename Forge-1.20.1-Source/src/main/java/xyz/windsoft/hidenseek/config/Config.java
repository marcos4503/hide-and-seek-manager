package xyz.windsoft.hidenseek.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.windsoft.hidenseek.Main;

import java.util.stream.Collectors;

/*
 * This class handle the mod configuration using the Forge Configuration API
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    //Private static constant variables
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    //Private static constant variables, that is the configs available to the user
    private static final ForgeConfigSpec.IntValue GUI_Y_OFFSET = BUILDER
            .comment("The offset of the Y axis, for GUI elements. (Type of INT. Range of 0~255.)")
            .defineInRange("guiYOffset", 0, 0, 255);
    private static final ForgeConfigSpec.IntValue GUI_ADRENALINE_X_OFFSET = BUILDER
            .comment("The offset of the X axis, for GUI elements of Adrenaline Points. (Type of INT. Range of -512~512.)")
            .defineInRange("guiAdrenalineXOffset", 0, -512, 512);
    private static final ForgeConfigSpec.IntValue GAME_COUNT_TIME = BUILDER
            .comment("The initial game count in seconds. (Type of INT. Range of 1~99999999999.)")
            .defineInRange("gameCountTime", 32, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue GAME_MINUTES_TO_REVEAL = BUILDER
            .comment("The minutes needed to reveal all Players. (Type of INT. Range of 1~99999999999.)")
            .defineInRange("gameMinutesToReveal", 1, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue GAME_SECONDS_TO_REVEAL = BUILDER
            .comment("The seconds needed to reveal all Players. (Type of INT. Range of 1~99999999999.)")
            .defineInRange("gameSecondsToReveal", 30, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue GAME_ANTI_CAMPING_RANGE = BUILDER
            .comment("If a Hidder is within this distance of the Game Totem Head when the match begins, it will be killed immediately. (Type of INT. Range of 1~99999999999.)")
            .defineInRange("gameAntiCampingRange", 10, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MAX_ADRENALINE_INJECTIONS_SPAWN = BUILDER
            .comment("The max number of Adrenaline Injections to keep existing in-game. (Type of INT. Range of 0~10.)")
            .defineInRange("maxAdrenalineInjectionsSpawn", 1, 0, 10);
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT1 = BUILDER
            .comment("The Seeker item, in slot 1 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot1", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT2 = BUILDER
            .comment("The Seeker item, in slot 2 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot2", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT3 = BUILDER
            .comment("The Seeker item, in slot 3 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot3", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT4 = BUILDER
            .comment("The Seeker item, in slot 4 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot4", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT5 = BUILDER
            .comment("The Seeker item, in slot 5 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot5", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT6 = BUILDER
            .comment("The Seeker item, in slot 6 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot6", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT7 = BUILDER
            .comment("The Seeker item, in slot 7 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot7", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT8 = BUILDER
            .comment("The Seeker item, in slot 8 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot8", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_HOTBAR_SLOT9 = BUILDER
            .comment("The Seeker item, in slot 9 of Hotbar. (Type of STRING. Use this format: \"minecraft:diamond_sword:nbt{Damage: 20}:qty{1}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerHotbarSlot9", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.BooleanValue SEEKER_REFILL_INVENTORY_SLOTS = BUILDER
            .comment("If enabled, items that are for the Seeker Inventory will be refilled whenever there is a revelation of Players. (Type of BOOL. true/false.)")
            .define("seekerRefillInventorySlots", false);
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT1 = BUILDER
            .comment("The Seeker item, in slot 1 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot1", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT2 = BUILDER
            .comment("The Seeker item, in slot 2 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot2", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT3 = BUILDER
            .comment("The Seeker item, in slot 3 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot3", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT4 = BUILDER
            .comment("The Seeker item, in slot 4 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot4", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT5 = BUILDER
            .comment("The Seeker item, in slot 5 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot5", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT6 = BUILDER
            .comment("The Seeker item, in slot 6 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot6", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT7 = BUILDER
            .comment("The Seeker item, in slot 7 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot7", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT8 = BUILDER
            .comment("The Seeker item, in slot 8 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot8", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.ConfigValue<String> SEEKER_INVENTORY_SLOT9 = BUILDER
            .comment("The Seeker item, in slot 9 of Hotbar. (Type of STRING. Use this format: \"minecraft:arrow:nbt{}:qty{32}\". Escape \" with \\\". Use \"/data get entity @s SelectedItem\" in-game, to get the ID+NBT.)")
            .define("seekerInventorySlot9", "minecraft:air:nbt{}:qty{1}");
    private static final ForgeConfigSpec.IntValue HIDDER_WHISTLE_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use of the Whistle, by Hidders. (Type of INT. Range of 5~180.)")
            .defineInRange("hidderWhistleCooldown", 60, 5, 180);
    private static final ForgeConfigSpec.IntValue HIDDER_WHISTLE_ADRENALINE_INCREASE = BUILDER
            .comment("The increase of adrenaline, when use the Whistle. (Type of INT. Range of 2~10.)")
            .defineInRange("hidderWhistleAdrenalineIncrease", 5, 2, 10);
    private static final ForgeConfigSpec.IntValue HIDDER_WHISTLE_REVEAL_DURATION = BUILDER
            .comment("The reveal time (seconds) on use the Whistle. (Type of INT. Range of 2~10.)")
            .defineInRange("hidderWhistleRevealDuration", 5, 2, 10);
    private static final ForgeConfigSpec.IntValue HIDDER_RAGE_BAITER_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use of the Rage Baiter, by Hidders. (Type of INT. Range of 5~180.)")
            .defineInRange("hidderRageBaiterCooldown", 30, 5, 180);
    private static final ForgeConfigSpec.IntValue HIDDER_RAGE_BAITER_ADRENALINE_INCREASE = BUILDER
            .comment("The increase of adrenaline, when use the Rage Baiter. (Type of INT. Range of 1~5.)")
            .defineInRange("hidderRageBaiterAdrenalineIncrease", 1, 1, 5);
    private static final ForgeConfigSpec.IntValue ADRENALINE_INJECTION_INCREASE = BUILDER
            .comment("The increase of adrenaline, when collect a Adrenaline Injection. (Type of INT. Range of 1~5.)")
            .defineInRange("adrenalineInjectionIncrease", 1, 1, 5);
    private static final ForgeConfigSpec.IntValue HIDDER_TOTEM_TRACKER_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Totem Tracker, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderTotemTrackerCooldown", 120, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_TOTEM_TRACKER_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Totem Tracker, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderTotemTrackerAdrenalineCost", 4, 1, 100);
    private static final ForgeConfigSpec.IntValue HIDDER_SMOKE_BOMB_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Smoke Bomb, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderSmokeBombCooldown", 15, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_SMOKE_BOMB_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Smoke Bomb, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderSmokeBombAdrenalineCost", 5, 1, 100);
    private static final ForgeConfigSpec.IntValue HIDDER_ZERO_GRAVITY_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Zero Gravity, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderZeroGravityCooldown", 90, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_ZERO_GRAVITY_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Zero Gravity, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderZeroGravityAdrenalineCost", 12, 1, 100);
    private static final ForgeConfigSpec.IntValue HIDDER_LEAP_OF_FAITH_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Leap of Faith, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderLeapOfFaithCooldown", 5, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_LEAP_OF_FAITH_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Leap of Faith, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderLeapOfFaithAdrenalineCost", 3, 1, 100);
    private static final ForgeConfigSpec.IntValue HIDDER_CAMOUFLAGE_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Camouflage, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderCamouflageCooldown", 12, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_CAMOUFLAGE_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Camouflage, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderCamouflageAdrenalineCost", 15, 1, 100);
    private static final ForgeConfigSpec.IntValue HIDDER_LADDER_SPECIALIST_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Ladder Specialist, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderLadderSpecialistCooldown", 45, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_LADDER_SPECIALIST_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Ladder Specialist, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderLadderSpecialistAdrenalineCost", 7, 1, 100);
    private static final ForgeConfigSpec.IntValue HIDDER_SOUND_BAIT_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Sound Bait, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderSoundBaitCooldown", 100, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_SOUND_BAIT_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Sound Bait, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderSoundBaitAdrenalineCost", 5, 1, 100);
    private static final ForgeConfigSpec.IntValue HIDDER_BREATHING_UNDERWATER_COOLDOWN = BUILDER
            .comment("The cooldown (seconds) for use the Breathing Underwater, by Hidders. (Type of INT. Range of 1~600.)")
            .defineInRange("hidderBreathingUnderwaterCooldown", 60, 1, 600);
    private static final ForgeConfigSpec.IntValue HIDDER_BREATHING_UNDERWATER_ADRENALINE_COST = BUILDER
            .comment("The cost of Adrenaline Points to use the Breathing Underwater, by Hidders. (Type of INT. Range of 1~100.)")
            .defineInRange("hidderBreathingUnderwaterAdrenalineCost", 2, 1, 100);

    //Public static constant variables
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    //Public static variables
    public static int guiYOffset = -1;
    public static int guiAdrenalineXOffset = -1;
    public static int gameCountTime = -1;
    public static int gameMinutesToReveal = -1;
    public static int gameSecondsToReveal = -1;
    public static int gameAntiCampingRange = -1;
    public static int maxAdrenalineInjectionsSpawn = -1;
    public static String seekerHotbarSlot1 = "";
    public static String seekerHotbarSlot2 = "";
    public static String seekerHotbarSlot3 = "";
    public static String seekerHotbarSlot4 = "";
    public static String seekerHotbarSlot5 = "";
    public static String seekerHotbarSlot6 = "";
    public static String seekerHotbarSlot7 = "";
    public static String seekerHotbarSlot8 = "";
    public static String seekerHotbarSlot9 = "";
    public static boolean seekerRefillInventorySlots = false;
    public static String seekerInventorySlot1 = "";
    public static String seekerInventorySlot2 = "";
    public static String seekerInventorySlot3 = "";
    public static String seekerInventorySlot4 = "";
    public static String seekerInventorySlot5 = "";
    public static String seekerInventorySlot6 = "";
    public static String seekerInventorySlot7 = "";
    public static String seekerInventorySlot8 = "";
    public static String seekerInventorySlot9 = "";
    public static int hidderWhistleCooldown = -1;
    public static int hidderWhistleAdrenalineIncrease = -1;
    public static int hidderWhistleRevealDuration = -1;
    public static int hidderRageBaiterCooldown = -1;
    public static int hidderRageBaiterAdrenalineIncrease = -1;
    public static int adrenalineInjectionIncrease = -1;
    public static int hidderTotemTrackerCooldown = -1;
    public static int hidderTotemTrackerAdrenalineCost = -1;
    public static int hidderSmokeBombCooldown = -1;
    public static int hidderSmokeBombAdrenalineCost = -1;
    public static int hidderZeroGravityCooldown = -1;
    public static int hidderZeroGravityAdrenalineCost = -1;
    public static int hidderLeapOfFaithCooldown = -1;
    public static int hidderLeapOfFaithAdrenalineCost = -1;
    public static int hidderCamouflageCooldown = -1;
    public static int hidderCamouflageAdrenalineCost = -1;
    public static int hidderLadderSpecialistCooldown = -1;
    public static int hidderLadderSpecialistAdrenalineCost = -1;
    public static int hidderSoundBaitCooldown = -1;
    public static int hidderSoundBaitAdrenalineCost = -1;
    public static int hidderBreathingUnderwaterCooldown = -1;
    public static int hidderBreathingUnderwaterAdrenalineCost = -1;

    //Public events

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        //Get the configs loaded from file
        guiYOffset = GUI_Y_OFFSET.get();
        guiAdrenalineXOffset = GUI_ADRENALINE_X_OFFSET.get();
        gameCountTime = GAME_COUNT_TIME.get();
        gameMinutesToReveal = GAME_MINUTES_TO_REVEAL.get();
        gameSecondsToReveal = GAME_SECONDS_TO_REVEAL.get();
        gameAntiCampingRange = GAME_ANTI_CAMPING_RANGE.get();
        maxAdrenalineInjectionsSpawn = MAX_ADRENALINE_INJECTIONS_SPAWN.get();
        seekerHotbarSlot1 = SEEKER_HOTBAR_SLOT1.get();
        seekerHotbarSlot2 = SEEKER_HOTBAR_SLOT2.get();
        seekerHotbarSlot3 = SEEKER_HOTBAR_SLOT3.get();
        seekerHotbarSlot4 = SEEKER_HOTBAR_SLOT4.get();
        seekerHotbarSlot5 = SEEKER_HOTBAR_SLOT5.get();
        seekerHotbarSlot6 = SEEKER_HOTBAR_SLOT6.get();
        seekerHotbarSlot7 = SEEKER_HOTBAR_SLOT7.get();
        seekerHotbarSlot8 = SEEKER_HOTBAR_SLOT8.get();
        seekerHotbarSlot9 = SEEKER_HOTBAR_SLOT9.get();
        seekerRefillInventorySlots = SEEKER_REFILL_INVENTORY_SLOTS.get();
        seekerInventorySlot1 = SEEKER_INVENTORY_SLOT1.get();
        seekerInventorySlot2 = SEEKER_INVENTORY_SLOT2.get();
        seekerInventorySlot3 = SEEKER_INVENTORY_SLOT3.get();
        seekerInventorySlot4 = SEEKER_INVENTORY_SLOT4.get();
        seekerInventorySlot5 = SEEKER_INVENTORY_SLOT5.get();
        seekerInventorySlot6 = SEEKER_INVENTORY_SLOT6.get();
        seekerInventorySlot7 = SEEKER_INVENTORY_SLOT7.get();
        seekerInventorySlot8 = SEEKER_INVENTORY_SLOT8.get();
        seekerInventorySlot9 = SEEKER_INVENTORY_SLOT9.get();
        seekerInventorySlot9 = SEEKER_INVENTORY_SLOT9.get();
        hidderWhistleCooldown = HIDDER_WHISTLE_COOLDOWN.get();
        hidderWhistleAdrenalineIncrease = HIDDER_WHISTLE_ADRENALINE_INCREASE.get();
        hidderWhistleRevealDuration = HIDDER_WHISTLE_REVEAL_DURATION.get();
        hidderRageBaiterCooldown = HIDDER_RAGE_BAITER_COOLDOWN.get();
        hidderRageBaiterAdrenalineIncrease = HIDDER_RAGE_BAITER_ADRENALINE_INCREASE.get();
        adrenalineInjectionIncrease = ADRENALINE_INJECTION_INCREASE.get();
        hidderTotemTrackerCooldown = HIDDER_TOTEM_TRACKER_COOLDOWN.get();
        hidderTotemTrackerAdrenalineCost = HIDDER_TOTEM_TRACKER_ADRENALINE_COST.get();
        hidderSmokeBombCooldown = HIDDER_SMOKE_BOMB_COOLDOWN.get();
        hidderSmokeBombAdrenalineCost = HIDDER_SMOKE_BOMB_ADRENALINE_COST.get();
        hidderZeroGravityCooldown = HIDDER_ZERO_GRAVITY_COOLDOWN.get();
        hidderZeroGravityAdrenalineCost = HIDDER_ZERO_GRAVITY_ADRENALINE_COST.get();
        hidderLeapOfFaithCooldown = HIDDER_LEAP_OF_FAITH_COOLDOWN.get();
        hidderLeapOfFaithAdrenalineCost = HIDDER_LEAP_OF_FAITH_ADRENALINE_COST.get();
        hidderCamouflageCooldown = HIDDER_CAMOUFLAGE_COOLDOWN.get();
        hidderCamouflageAdrenalineCost = HIDDER_CAMOUFLAGE_ADRENALINE_COST.get();
        hidderLadderSpecialistCooldown = HIDDER_LADDER_SPECIALIST_COOLDOWN.get();
        hidderLadderSpecialistAdrenalineCost = HIDDER_LADDER_SPECIALIST_ADRENALINE_COST.get();
        hidderSoundBaitCooldown = HIDDER_SOUND_BAIT_COOLDOWN.get();
        hidderSoundBaitAdrenalineCost = HIDDER_SOUND_BAIT_ADRENALINE_COST.get();
        hidderBreathingUnderwaterCooldown = HIDDER_BREATHING_UNDERWATER_COOLDOWN.get();
        hidderBreathingUnderwaterAdrenalineCost = HIDDER_BREATHING_UNDERWATER_ADRENALINE_COST.get();
    }
}