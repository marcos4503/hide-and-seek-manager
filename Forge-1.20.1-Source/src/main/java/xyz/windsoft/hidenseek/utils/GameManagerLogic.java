package xyz.windsoft.hidenseek.utils;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.SubStringSource;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.block.custom.GameTotemHeadPoweredBlock;
import xyz.windsoft.hidenseek.block.entity.GameTotemHeadPoweredBlockEntity;
import xyz.windsoft.hidenseek.config.Config;
import xyz.windsoft.hidenseek.effect.ModEffects;
import xyz.windsoft.hidenseek.item.ModItems;
import xyz.windsoft.hidenseek.network.*;
import com.mojang.brigadier.StringReader;
import xyz.windsoft.hidenseek.sounds.ModSounds;

import java.util.*;
import java.util.function.Predicate;

/*
 * This class will manage the Hide'n Seek game matches, giving points, managing the duration, etc. This class run
 * only in Server, and send packets to Players, to update the game status to their Clients.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [X] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class GameManagerLogic {

    //Private constant variables
    private static SoundEvent[] HIDDER_CLUES = {SoundEvents.VILLAGER_AMBIENT, SoundEvents.PANDA_SNEEZE, SoundEvents.BEE_LOOP, SoundEvents.WOOD_STEP, SoundEvents.CHICKEN_HURT, SoundEvents.GOAT_SCREAMING_AMBIENT, SoundEvents.GHAST_AMBIENT};
    private static SoundEvent[] HIDDER_WHISTLE = {ModSounds.WHISTLE_0.get(), ModSounds.WHISTLE_1.get(), ModSounds.WHISTLE_2.get(), ModSounds.WHISTLE_3.get()};

    //Private static cache variables (not needed to be reseted on game finished)
    private static UUID lastPlayerSeeker = null;
    private static HashMap<Item, Integer> itemsAndTheirCooldowns = null;
    private static int ticksUntilClearDroppedItems = -1;

    //Private static cache variables
    private static int ticksPassedInCurrentStage = 0;

    //Private static variables
    private static ServerPlayer startedByPlayer = null;
    private static String startedByNick = "";
    private static GameTotemHeadPoweredBlockEntity targetGameTotem = null;
    private static BlockPos targetGameTotemPosition = null;
    private static long startTimeInMillis = -1;
    private static boolean isGameBadgeCollectedSuccessfully = false;

    //Public static variables of Game State Machine
    public static enum GameStage {
        StandBy,
        WaitingPlayers,
        Preparation,
        Count,
        Progress,
        Celebration,
        Finished
    }
    public static enum GameResult{
        Unknown,
        SeekersWin,
        HiddersWin,
        Draw
    }

    //Private static variables of Game State Machine
    private static GameStage currentGameState = GameStage.StandBy;
    private static HashMap<Allay, ServerPlayer> allaysOfSpectators = null;
    private static HashMap<Vex, ServerPlayer> vexesOfSpectators = null;
    private static ServerBossEvent hiddersLeftBossBar = null;
    private static Scoreboard gameScoreboard = null;
    private static Objective gameScoreboardObjective = null;
    private static List<BlockPos> goldBlocksFoundList = null;
    private static BlockPos lastGoldBlockSelected = null;
    private static List<ServerPlayer> seekersList = null;
    private static List<ServerPlayer> hiddersList = null;
    private static int gameCountTime = 0;
    private static boolean addedPlayersToBossBar = false;
    private static boolean sendedWarnOfProgressStage = false;
    private static int ticksElapsedToDecreaseRevealTime = 0;
    private static int remaingMinutesUntilRevealPlayers = 0;
    private static int remaingSecondsUntilRevealPlayers = 0;
    private static GameResult finalGameResult = GameResult.Unknown;
    private static int celebrationInteractionsElapsed = 0;
    private static int finishedInteractionsElapsed = 0;

    //Public static methods

    public static void StartGame(ServerPlayer startedByServerPlayer, BlockPos gameTotemHeadPoweredBlockPosition, long gameStartTimeInMillis){
        //If is not server, stop here
        if (startedByServerPlayer.level().isClientSide() == true)
            return;

        //Store the data
        startedByPlayer = startedByServerPlayer;
        startedByNick = startedByServerPlayer.getName().getString();
        targetGameTotemPosition = gameTotemHeadPoweredBlockPosition;
        startTimeInMillis = gameStartTimeInMillis;

        //Load the BlockEntity that has the Menu of the Game Totem Head Powered
        if (startedByPlayer.level().getBlockEntity(targetGameTotemPosition) instanceof GameTotemHeadPoweredBlockEntity)
            targetGameTotem = ((GameTotemHeadPoweredBlockEntity) startedByPlayer.level().getBlockEntity(targetGameTotemPosition));
        //If the Block Entity of the Totem is null, stop here
        if (targetGameTotem == null)
            return;

        //Access the item store capability of the Totem...
        targetGameTotem.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            //If the input slot is empty, stop here
            if (iItemHandler.getStackInSlot(0).isEmpty() == true)
                return;

            //If a Game Badge is not present in the input slot, stop here
            if (iItemHandler.getStackInSlot(0).is(ModItems.MOD_BADGE.get()) == false)
                return;

            //Erase the Game Badge present in the Input Slot
            if (iItemHandler instanceof IItemHandlerModifiable modifiableHandler)
                modifiableHandler.setStackInSlot(0, ItemStack.EMPTY);

            //Warn to the server that the block was changed, and need to be saved
            targetGameTotem.setChanged();
            //Warn to the server, to send a update packet for all Clients that area near to this BlockEntity, to receive the new synced state
            startedByPlayer.level().sendBlockUpdated(targetGameTotemPosition, targetGameTotem.getBlockState(), targetGameTotem.getBlockState(), 3);   //<- Use the flag "3" to ensure that all Players near, receive the update

            //Inform that the Game Badge was collected successfully
            isGameBadgeCollectedSuccessfully = true;
        });

        //If the Game Bade collection, was failed, send a warn and stop here
        if (isGameBadgeCollectedSuccessfully == false){
            //Send a message to the Player
            startedByPlayer.displayClientMessage(Component.translatable("gui.hidenseek.game_totem_head_powered_screen.start_game_badge_error").withStyle(ChatFormatting.RED), false);
            //Cancel here
            return;
        }

        //Change the current Game State to next...
        currentGameState = GameStage.WaitingPlayers;

        //Inform to the Game Totem Head Powered BlockEntity, that the game was started
        targetGameTotem.SetGameAsStarted(startedByServerPlayer.level(), targetGameTotemPosition, targetGameTotem.getBlockState());

        //Launch a Firework at the Game Totem
        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag explosionTag = new CompoundTag();
        explosionTag.putByte("Type", (byte) 1);                               //<- 1 = Large Ball (Grande)
        explosionTag.putIntArray("Colors", new int[] { 0xFF5555, 0x55FF55 }); //<- Red and Green
        explosionTag.putBoolean("Flicker", true);                             //<- Flicker effect
        ListTag explosionsList = new ListTag();
        explosionsList.add(explosionTag);
        CompoundTag fireworksTag = fireworkStack.getOrCreateTagElement("Fireworks");
        fireworksTag.put("Explosions", explosionsList);
        fireworksTag.putByte("Flight", (byte) 1);                             //<- Low flying time (1 = Explodes fast)
        FireworkRocketEntity rocket = new FireworkRocketEntity(startedByPlayer.level(), targetGameTotemPosition.getX(), (targetGameTotemPosition.getY() + 1.0f), targetGameTotemPosition.getZ(), fireworkStack);
        startedByServerPlayer.level().addFreshEntity(rocket);
        startedByServerPlayer.level().playSound(null, targetGameTotemPosition, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 5.0F, 1.0F);

        //Create the Boss Bar that will be used during the game
        hiddersLeftBossBar = new ServerBossEvent(Component.literal("?"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        //Crate the Scoreboard of the game
        gameScoreboard = startedByServerPlayer.level().getScoreboard();

        //Save the information to NBT of the Level, to inform that the game was started
        GameManagerLevelData.get(((ServerLevel)startedByServerPlayer.level())).SetGameRunning(true);
        GameManagerLevelData.get(((ServerLevel)startedByServerPlayer.level())).SetGameTotemHeadPos(targetGameTotemPosition);
    }

    public static void Tick(TickEvent.ServerTickEvent event){
        //If not is the logical server, stop here
        //if (FMLEnvironment.dist != Dist.DEDICATED_SERVER)
        //    return;

        //If is needed to clear all items on the next tick, do it
        if (ticksUntilClearDroppedItems > -1){
            //Do the clearing, if it's time
            if (ticksUntilClearDroppedItems == 0)
                ClearAllDroppedItemsNow(event);
            //Decrease the ticks counter
            ticksUntilClearDroppedItems -= 1;
        }

        //Do the processing on Spectator Allays and Vexes, if necessary
        ProcessAllSpectatorAllaysAndVexesOnServerTick(event);

        //Create the Stage Machine, runner (1 Second = 20 Ticks, Tick = 50ms)
        switch (currentGameState){
            case StandBy:
                //If has elapsed 10 Ticks (500ms), process this stage...
                if (ticksPassedInCurrentStage >= 10){
                    //Run the desired stage, and get response
                    GameStage stageToSkipTo = ProcessStage_Delayed_StandBy(event);
                    //Reset the timer
                    ticksPassedInCurrentStage = 0;
                    //If is desired to skip to another stage, skip to it
                    if (stageToSkipTo != currentGameState)
                        currentGameState = stageToSkipTo;
                }
                break;
            case WaitingPlayers:
                //If has elapsed 10 Ticks (500ms), process this stage...
                if (ticksPassedInCurrentStage >= 10){
                    //Run the desired stage, and get response
                    GameStage stageToSkipTo = ProcessStage_Delayed_WaitingPlayers(event);
                    //Reset the timer
                    ticksPassedInCurrentStage = 0;
                    //If is desired to skip to another stage, skip to it
                    if (stageToSkipTo != currentGameState)
                        currentGameState = stageToSkipTo;
                }
                break;
            case Preparation:
                //If has elapsed 4 Ticks (200ms), process this stage...
                if (ticksPassedInCurrentStage >= 4){
                    //Run the desired stage, and get response
                    GameStage stageToSkipTo = ProcessStage_Delayed_Preparation(event);
                    //Reset the timer
                    ticksPassedInCurrentStage = 0;
                    //If is desired to skip to another stage, skip to it
                    if (stageToSkipTo != currentGameState)
                        currentGameState = stageToSkipTo;
                }
                break;
            case Count:
                //If has elapsed 20 Ticks (1000ms), process this stage...
                if (ticksPassedInCurrentStage >= 20){
                    //Run the desired stage, and get response
                    GameStage stageToSkipTo = ProcessStage_Delayed_Count(event);
                    //Reset the timer
                    ticksPassedInCurrentStage = 0;
                    //If is desired to skip to another stage, skip to it
                    if (stageToSkipTo != currentGameState)
                        currentGameState = stageToSkipTo;
                }
                break;
            case Progress:
                //If has elapsed 5 Ticks (250ms), process this stage...
                if (ticksPassedInCurrentStage >= 5){
                    //Run the desired stage, and get response
                    GameStage stageToSkipTo = ProcessStage_Delayed_Progress(event);
                    //Reset the timer
                    ticksPassedInCurrentStage = 0;
                    //If is desired to skip to another stage, skip to it
                    if (stageToSkipTo != currentGameState)
                        currentGameState = stageToSkipTo;
                }
                break;
            case Celebration:
                //If has elapsed 5 Ticks (250ms), process this stage...
                if (ticksPassedInCurrentStage >= 5){
                    //Run the desired stage, and get response
                    GameStage stageToSkipTo = ProcessStage_Delayed_Celebration(event);
                    //Reset the timer
                    ticksPassedInCurrentStage = 0;
                    //If is desired to skip to another stage, skip to it
                    if (stageToSkipTo != currentGameState)
                        currentGameState = stageToSkipTo;
                }
                break;
            case Finished:
                //If has elapsed 5 Ticks (250ms), process this stage...
                if (ticksPassedInCurrentStage >= 5){
                    //Run the desired stage, and get response
                    GameStage stageToSkipTo = ProcessStage_Delayed_Finished(event);
                    //Reset the timer
                    ticksPassedInCurrentStage = 0;
                    //If is desired to skip to another stage, skip to it
                    if (stageToSkipTo != currentGameState)
                        currentGameState = stageToSkipTo;
                }
                break;
        }

        //Increase the count of ticks elapsed, in current stage (1 Second = 20 Ticks, Tick = 50ms)
        ticksPassedInCurrentStage += 1;
    }

    public static GameStage ProcessStage_Delayed_StandBy(TickEvent.ServerTickEvent event){
        //Prepare the value to return
        GameStage toReturn = GameStage.StandBy;

        //Send a packet to all Players, to update UI
        ModPacketHandler.SendToAllClients(new ServerToClient_GameStandByPacket(Component.translatable("gui.hidenseek.game_stage.standby.welcome_message").getString()));

        //Return the value
        return toReturn;
    }

    public static GameStage ProcessStage_Delayed_WaitingPlayers(TickEvent.ServerTickEvent event){
        //Prepare the value to return
        GameStage toReturn = GameStage.WaitingPlayers;

        //Get the list of online Players
        List<ServerPlayer> onlinePlayers = ((ServerLevel) startedByPlayer.level()).players();

        //If have less than 2 Players, send a Packet to update the Players
        if (onlinePlayers.size() < 2)
            ModPacketHandler.SendToAllClients(new ServerToClient_GameWaitingPlayersPacket(Component.translatable("gui.hidenseek.game_stage.waitingplayers.waiting_message", onlinePlayers.size()).getString()));

        //If have 2 Players or more, go to next stage
        if (onlinePlayers.size() >= 2)
            toReturn = GameStage.Preparation;

        //Return the value
        return toReturn;
    }

    public static GameStage ProcessStage_Delayed_Preparation(TickEvent.ServerTickEvent event){
        //Prepare the value to return
        GameStage toReturn = GameStage.Preparation;

        //Get the list of Gold Blocks found in the map
        goldBlocksFoundList = FindGoldBlocks(startedByPlayer.level(), targetGameTotemPosition, 100);

        //Get the list of online Players
        List<ServerPlayer> onlinePlayers = ((ServerLevel) startedByPlayer.level()).players();

        //Prepare to decide the Seeker
        int decisionLoopIteractions = 0;
        int decidedSeekerIndex = 0;
        //Start the Seeker decision loop...
        while (true == true){
            //Increase the loop interactions counter
            decisionLoopIteractions += 1;
            //If was reached the max interactions, break the loop here
            if (decisionLoopIteractions >= 100)
                break;
            //Select a random index of the Player list, to be a Seeker...
            decidedSeekerIndex = startedByPlayer.level().random.nextInt(onlinePlayers.size());
            //If the index of the decided Player points to a Player that was not the Seeker in the last match, break this loop and follow the game
            if (onlinePlayers.get(decidedSeekerIndex).getUUID().equals(lastPlayerSeeker) == false){
                //Inform the new last player seeker
                lastPlayerSeeker = onlinePlayers.get(decidedSeekerIndex).getUUID();
                //Break this loop
                break;
            }
            //If the index of the decided Player points to a Player that already was the Seeker in the last match, force a new loop iteraction
            if (onlinePlayers.get(decidedSeekerIndex).getUUID().equals(lastPlayerSeeker) == true)
                continue;
        }
        //Build the list of Seekers and Hidders
        hiddersList = new ArrayList<>();
        for (int i = 0; i < onlinePlayers.size(); i++)
            if (i != decidedSeekerIndex)
                hiddersList.add(onlinePlayers.get(i));
        seekersList = new ArrayList<>();
        seekersList.add(onlinePlayers.get(decidedSeekerIndex));

        //Clear the inventory of Players and reset Players stats
        for (int i = 0; i < hiddersList.size(); i++){
            hiddersList.get(i).getInventory().clearContent();
            onlinePlayers.get(i).setGameMode(GameType.ADVENTURE);
            onlinePlayers.get(i).setHealth(20.0f);
            onlinePlayers.get(i).getFoodData().setFoodLevel(20);
            onlinePlayers.get(i).getFoodData().setSaturation(20.0f);
        }

        //Reduce size of the Hidders, and give mobility effects for them
        for (int i = 0; i < hiddersList.size(); i++){
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(hiddersList.get(i));
            scaleData.setTargetScale(0.2f);
            scaleData.setScaleTickDelay(20);
            hiddersList.get(i).addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999999, 16, false, false));
            hiddersList.get(i).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 999999999, 8, false, false));
            hiddersList.get(i).addEffect(new MobEffectInstance(MobEffects.JUMP, 999999999, 5, false, false));
            hiddersList.get(i).addEffect(new MobEffectInstance(ModEffects.LADDER_SPECIALIST.get(), ((Config.gameCountTime + 5) * 20), 0, false, false));
        }
        //Give weapons/items for the Seekers
        for (int i = 0; i < seekersList.size(); i++){
            seekersList.get(i).getInventory().clearContent();
            GiveItemToPlayer(Config.seekerHotbarSlot1, 0, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot2, 1, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot3, 2, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot4, 3, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot5, 4, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot6, 5, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot7, 6, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot8, 7, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerHotbarSlot9, 8, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot1, 27, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot2, 28, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot3, 29, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot4, 30, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot5, 31, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot6, 32, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot7, 33, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot8, 34, seekersList.get(i), false);
            GiveItemToPlayer(Config.seekerInventorySlot9, 35, seekersList.get(i), false);
            //Ensure that the Client receive the new inventory visually to display it
            EnsureThatTheInventoryIsUpdatedOnClient(seekersList.get(i), false);
        }

        //Get the BlockPos of front of the GameTotem
        BlockPos frontOfGameTotem = GetBlockAtFrontOfGameTotem();
        //Teleport all Players to the front of the Game Totem
        for (int i = 0; i < seekersList.size(); i++)
            seekersList.get(i).teleportTo(frontOfGameTotem.getX(), frontOfGameTotem.getY(), frontOfGameTotem.getZ());
        for (int i = 0; i < hiddersList.size(); i++)
            hiddersList.get(i).teleportTo(frontOfGameTotem.getX(), frontOfGameTotem.getY(), frontOfGameTotem.getZ());

        //Reset the remaing time until reveal
        remaingMinutesUntilRevealPlayers = Config.gameMinutesToReveal;
        remaingSecondsUntilRevealPlayers = Config.gameSecondsToReveal;

        //Inform to skip to next stage
        toReturn = GameStage.Count;

        //Return the value
        return toReturn;
    }

    public static GameStage ProcessStage_Delayed_Count(TickEvent.ServerTickEvent event){
        //Prepare the value to return
        GameStage toReturn = GameStage.Count;

        //Increase the Game Count Time
        gameCountTime += 1;
        String gameCountTimeStr = "";

        //If the count is not ended
        if (gameCountTime < (Config.gameCountTime - 1))
            gameCountTimeStr = String.valueOf((Config.gameCountTime - (gameCountTime + 1)));
        //If the count was almost ending
        if (gameCountTime == (Config.gameCountTime - 1))
            gameCountTimeStr = Component.translatable("gui.hidenseek.game_stage.count.end_count").getString();
        //If the count was ended
        if (gameCountTime == Config.gameCountTime){
            gameCountTimeStr = Component.translatable("gui.hidenseek.game_stage.count.end_count").getString();
            toReturn = GameStage.Progress;
            //If a Hidder is too close to Game Totem, force him to look at down
            for (int i = 0; i < hiddersList.size(); i++)
                if (isPlayerOnline(hiddersList.get(i)) == true)
                    if (GetDistanceToTotem(hiddersList.get(i), targetGameTotemPosition) < Config.gameAntiCampingRange){
                        float yaw = hiddersList.get(i).getYRot();
                        hiddersList.get(i).setXRot(90.0f);
                        hiddersList.get(i).setYRot(yaw);
                        hiddersList.get(i).connection.teleport(hiddersList.get(i).getX(), hiddersList.get(i).getY(), hiddersList.get(i).getZ(), yaw, 90.0f);
                    }
        }

        //Calculate a color for the Count
        String gameCountColorStr = "WHITE";
        if (gameCountTime >= (Config.gameCountTime - 11))
            gameCountColorStr = "GOLDEN";
        if (gameCountTime >= (Config.gameCountTime - 6))
            gameCountColorStr = "RED";
        if (gameCountTime >= Config.gameCountTime)
            gameCountColorStr = "GREEN";

        //Send update to all Seekers
        for (int i = 0; i < seekersList.size(); i++)
            ModPacketHandler.SendToPlayer(new ServerToClient_GameCountPacket(Component.translatable("gui.hidenseek.game_stage.count.role_announce_seeker").getString(),
                            Component.translatable("gui.hidenseek.game_stage.count.role_subannounce_seeker").getString(),
                            "RED",
                            gameCountTimeStr,
                            gameCountColorStr,
                            (Config.gameCountTime - gameCountTime),
                            Config.gameCountTime),
                    seekersList.get(i));
        //Send update to all Hidders
        for (int i = 0; i < hiddersList.size(); i++)
            ModPacketHandler.SendToPlayer(new ServerToClient_GameCountPacket(Component.translatable("gui.hidenseek.game_stage.count.role_announce_hidder").getString(),
                            Component.translatable("gui.hidenseek.game_stage.count.role_subannounce_hidder").getString(),
                            "BLUE",
                            gameCountTimeStr,
                            gameCountColorStr,
                            (Config.gameCountTime - gameCountTime),
                            Config.gameCountTime),
                    hiddersList.get(i));

        //Send a warn to all Hidders that are camping at Game Totem
        for (int i = 0; i < hiddersList.size(); i++)
            if (isPlayerOnline(hiddersList.get(i)) == true)
                if (GetDistanceToTotem(hiddersList.get(i), targetGameTotemPosition) < Config.gameAntiCampingRange){
                    //Send the warn...
                    hiddersList.get(i).displayClientMessage(Component.translatable("gui.hidenseek.game_stage.count.camping_warning").withStyle(ChatFormatting.RED), true);
                    //If remain 10 seconds to start the game, start sending sound packets of alert of camping
                    if (gameCountTime >= (Config.gameCountTime - 11)){
                        hiddersList.get(i).connection.send(new ClientboundSetTitlesAnimationPacket(0, 30, 0));
                        hiddersList.get(i).connection.send(new ClientboundSetTitleTextPacket(Component.translatable("gui.hidenseek.game_stage.count.camping_warning_enforce_title").withStyle(ChatFormatting.DARK_RED)));
                        hiddersList.get(i).connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("gui.hidenseek.game_stage.count.camping_warning_enforce_subtitle")));
                        ModPacketHandler.SendToPlayer(new ServerToClient_GameCountCampPacket(), hiddersList.get(i));
                    }
                }
        //Apply movement and view restrictions to Seekers
        for (int i = 0; i < seekersList.size(); i++){
            seekersList.get(i).addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
            seekersList.get(i).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false));
            if (seekersList.get(i).hasEffect(MobEffects.CONFUSION) == false)
                seekersList.get(i).addEffect(new MobEffectInstance(MobEffects.CONFUSION, (Config.gameCountTime * 20), 9, false, false));
            seekersList.get(i).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 255, false, false));
            seekersList.get(i).addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 200, false, false));
            //Add a Pumpkin to the Head of the Player
            seekersList.get(i).setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.CARVED_PUMPKIN));
            seekersList.get(i).containerMenu.broadcastChanges();
            //Render a effect of Angry Villager
            ((ServerLevel)startedByPlayer.level()).sendParticles(ParticleTypes.ANGRY_VILLAGER, seekersList.get(i).getX(), seekersList.get(i).getY() + 1.5f, seekersList.get(i).getZ(), 10, 0.5f, 0.5f, 0.5f, 0.1f);
        }

        //Return the value
        return toReturn;
    }

    public static GameStage ProcessStage_Delayed_Progress(TickEvent.ServerTickEvent event){
        //Prepare the value to return
        GameStage toReturn = GameStage.Progress;

        //Get the list of Online Players
        List<ServerPlayer> onlinePlayers = ((ServerLevel) startedByPlayer.level()).players();

        //Add all players to the Boss Bar, if not added
        if (addedPlayersToBossBar == false){
            for (int i = 0; i < onlinePlayers.size(); i++)
                hiddersLeftBossBar.addPlayer(onlinePlayers.get(i));
            addedPlayersToBossBar = true;
        }

        //If not was sended the warn that the game is in the "Progress" stage, send it
        if (sendedWarnOfProgressStage == false){
            //Send the packet to warn that is in the "Progress"
            ModPacketHandler.SendToAllClients(new ServerToClient_GameProgressPacket());
            //If exists a Hidder too close from the Game Totem, do a burst to him
            for (int i = 0; i < hiddersList.size(); i++)
                if (isPlayerOnline(hiddersList.get(i)) == true)
                    if (GetDistanceToTotem(hiddersList.get(i), targetGameTotemPosition) < Config.gameAntiCampingRange)
                        hiddersList.get(i).hurt(hiddersList.get(i).damageSources().magic(), 25.0f);   //<- Causes a huge damage, that allows the "OnDamage()" event, to run at the Player
            //Send a packet to each hidder, to inform each hidder of the Adrenaline Points that each one have
            for (int i = 0; i < hiddersList.size(); i++)
                if (isPlayerOnline(hiddersList.get(i)) == true){
                    //Get NBT data of this Player...
                    CompoundTag playerPersistentNbt = hiddersList.get(i).getPersistentData();
                    //Send the packet...
                    if (playerPersistentNbt.contains("adrenalinePoints") == true)
                        ModPacketHandler.SendToPlayer(new ServerToClient_GameProgressAdrenalinePacket(playerPersistentNbt.getInt("adrenalinePoints")), hiddersList.get(i));
                    if (playerPersistentNbt.contains("adrenalinePoints") == false)
                        ModPacketHandler.SendToPlayer(new ServerToClient_GameProgressAdrenalinePacket(0), hiddersList.get(i));
                }
            //Give the Hidders Items to each hidder
            for (int i = 0; i < hiddersList.size(); i++)
                if (isPlayerOnline(hiddersList.get(i)) == true){
                    //Force the Hidder to select the item 0 on Hotbar
                    hiddersList.get(i).getInventory().selected = 0;
                    hiddersList.get(i).connection.send(new ClientboundSetCarriedItemPacket(0));
                    //Give all items
                    GiveItemToPlayer(ModItems.RAGE_BAITER.get(), 1, 1, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.WHISTLE.get(), 1, 2, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.LEAP_OF_FAITH.get(), 1, 3, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.SMOKE_BOMB.get(), 1, 4, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.CAMOUFLAGE.get(), 1, 5, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.LADDER_SPECIALIST.get(), 1, 6, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.ZERO_GRAVITY.get(), 1, 7, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.SOUND_BAIT.get(), 1, 8, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.BREATHING_UNDERWATER.get(), 1, 34, hiddersList.get(i), false);
                    GiveItemToPlayer(ModItems.TOTEM_TRACKER.get(), 1, 35, hiddersList.get(i), false);
                    //Set a initial cooldown on items that are strong
                    hiddersList.get(i).getCooldowns().addCooldown(ModItems.WHISTLE.get(), (Config.hidderWhistleCooldown * 20));
                    //Ensure that the Client receive the new inventory visually to display it
                    EnsureThatTheInventoryIsUpdatedOnClient(hiddersList.get(i), false);
                }
            //Remove the Pumpkin from the Head of the Seeker
            for (int i = 0; i < seekersList.size(); i++)
                if (isPlayerOnline(seekersList.get(i)) == true){
                    seekersList.get(i).setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.AIR));
                    seekersList.get(i).containerMenu.broadcastChanges();
                }
            //Inform that the warn was sended
            sendedWarnOfProgressStage = true;
        }

        //Reset the stats of all Hidders in-game
        for (int i = 0; i < hiddersList.size(); i++){
            hiddersList.get(i).getFoodData().setFoodLevel(15);
            hiddersList.get(i).getFoodData().setSaturation(1.0f);
        }
        //Reset stats of all Seekers in-game
        for (int i = 0; i < seekersList.size(); i++){
            seekersList.get(i).setHealth(20.0f);
            seekersList.get(i).getFoodData().setFoodLevel(20);
            seekersList.get(i).getFoodData().setSaturation(20.0f);
        }

        //Check if the Seeker still connected
        boolean isSeekerOnline = isPlayerOnline(seekersList.get(0));
        //Count the number of Hidders connected
        int hiddersOnlineCount = 0;
        for (int i = 0; i < hiddersList.size(); i++)
            if (isPlayerOnline(hiddersList.get(i)) == true)
                hiddersOnlineCount += 1;
        //Count the number of Hidders Self Saved
        int hiddersOnlineAndSelfSavedCount = 0;
        for (int i = 0; i < hiddersList.size(); i++)
            if (isPlayerOnline(hiddersList.get(i)) == true)
                if (hiddersList.get(i).gameMode.getGameModeForPlayer() == GameType.SPECTATOR && hiddersList.get(i).getPersistentData().contains("wasSelfSaved") == true)
                    hiddersOnlineAndSelfSavedCount += 1;
        //Count the number of Hidders Eliminated
        int hiddersOnlineAndEliminatedCount = 0;
        for (int i = 0; i < hiddersList.size(); i++)
            if (isPlayerOnline(hiddersList.get(i)) == true)
                if (hiddersList.get(i).gameMode.getGameModeForPlayer() == GameType.SPECTATOR && hiddersList.get(i).getPersistentData().contains("wasEliminated") == true)
                    hiddersOnlineAndEliminatedCount += 1;
        //Count the number of Hidders Alive
        int hiddersOnlineAndAliveCount = 0;
        for (int i = 0; i < hiddersList.size(); i++)
            if (isPlayerOnline(hiddersList.get(i)) == true)
                if (hiddersList.get(i).gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
                    hiddersOnlineAndAliveCount += 1;
        //Count the number of Simple Spectators
        int simpleSpectatorsOnlineCount = 0;
        for (int i = 0; i < onlinePlayers.size(); i++)
            if (isPlayerOnline(onlinePlayers.get(i)) == true)
                if (onlinePlayers.get(i).gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
                    if (onlinePlayers.get(i).getPersistentData().contains("wasEliminated") == false && onlinePlayers.get(i).getPersistentData().contains("wasSelfSaved") == false)
                        simpleSpectatorsOnlineCount += 1;
        //Calculate the percent of Players Eliminated
        float playersEliminatedPercent = ((float)hiddersOnlineAndEliminatedCount / (float)hiddersOnlineCount);
        //Calculate the percent of Players Self Saved
        float playersSelfSavedPercent = ((float)hiddersOnlineAndSelfSavedCount / (float)hiddersOnlineCount);

        //If the game scoreboard, was not initialized yet, initialize
        if (gameScoreboardObjective == null){
            //Add the objective
            gameScoreboardObjective = gameScoreboard.addObjective("hidenseek_scb", ObjectiveCriteria.DUMMY, Component.translatable("gui.hidenseek.game_stage.progress.scoreboard_title"), ObjectiveCriteria.RenderType.INTEGER);
            gameScoreboard.setDisplayObjective(Scoreboard.DISPLAY_SLOT_SIDEBAR, gameScoreboardObjective);
            //Add the line of "hiddersTotalCount"
            PlayerTeam hiddersTotalCount = gameScoreboard.addPlayerTeam("hiddersTotalCount");
            gameScoreboard.addPlayerToTeam("§a", hiddersTotalCount);
            hiddersTotalCount.setPlayerPrefix(Component.literal("...").append(Component.literal("!!!")));
            gameScoreboard.getOrCreatePlayerScore("§a", gameScoreboardObjective).setScore(5);
            //Add the line of "hiddersAliveCount"
            PlayerTeam hiddersAliveCount = gameScoreboard.addPlayerTeam("hiddersAliveCount");
            gameScoreboard.addPlayerToTeam("§b", hiddersAliveCount);
            hiddersAliveCount.setPlayerPrefix(Component.literal("...").append(Component.literal("!!!")));
            gameScoreboard.getOrCreatePlayerScore("§b", gameScoreboardObjective).setScore(4);
            //Add the line of "hiddersEliminated"
            PlayerTeam hiddersEliminated = gameScoreboard.addPlayerTeam("hiddersEliminated");
            gameScoreboard.addPlayerToTeam("§c", hiddersEliminated);
            hiddersEliminated.setPlayerPrefix(Component.literal("...").append(Component.literal("!!!")));
            gameScoreboard.getOrCreatePlayerScore("§c", gameScoreboardObjective).setScore(3);
            //Add the line of "hiddersSelfSaved"
            PlayerTeam hiddersSelfSaved = gameScoreboard.addPlayerTeam("hiddersSelfSaved");
            gameScoreboard.addPlayerToTeam("§d", hiddersSelfSaved);
            hiddersSelfSaved.setPlayerPrefix(Component.literal("...").append(Component.literal("!!!")));
            gameScoreboard.getOrCreatePlayerScore("§d", gameScoreboardObjective).setScore(2);
            //Add the line of "simpleSpectators"
            PlayerTeam simpleSpectators = gameScoreboard.addPlayerTeam("simpleSpectators");
            gameScoreboard.addPlayerToTeam("§e", simpleSpectators);
            simpleSpectators.setPlayerPrefix(Component.literal("...").append(Component.literal("!!!")));
            gameScoreboard.getOrCreatePlayerScore("§e", gameScoreboardObjective).setScore(1);
        }
        //Update the Scoreboard
        if (gameScoreboardObjective != null){
            //Update the Scoreboard
            gameScoreboard.getPlayerTeam("hiddersTotalCount").setPlayerPrefix(Component.translatable("gui.hidenseek.game_stage.progress.scoreboard_hidders_total_count").append(Component.literal(": §6" + hiddersOnlineCount)));
            String hiddersOnlineAndAliveStr = (hiddersOnlineAndAliveCount + " (" + String.format("%.0f", (((float)hiddersOnlineAndAliveCount / (float)hiddersOnlineCount) * 100.0f)) + "%)");
            gameScoreboard.getPlayerTeam("hiddersAliveCount").setPlayerPrefix(Component.translatable("gui.hidenseek.game_stage.progress.scoreboard_hidders_alive_count").append(Component.literal(": §6" + hiddersOnlineAndAliveStr)));
            String hiddersEliminatedStr = (hiddersOnlineAndEliminatedCount + " (" + String.format("%.0f", (playersEliminatedPercent  * 100.0f)) + "%)");
            gameScoreboard.getPlayerTeam("hiddersEliminated").setPlayerPrefix(Component.translatable("gui.hidenseek.game_stage.progress.scoreboard_hidders_eliminated").append(Component.literal(": §6" + hiddersEliminatedStr)));
            String hiddersSelfSavedStr = (hiddersOnlineAndSelfSavedCount + " (" + String.format("%.0f", (playersSelfSavedPercent * 100.0f)) + "%)");
            gameScoreboard.getPlayerTeam("hiddersSelfSaved").setPlayerPrefix(Component.translatable("gui.hidenseek.game_stage.progress.scoreboard_hidders_self_saved").append(Component.literal(": §6" + hiddersSelfSavedStr)));
            gameScoreboard.getPlayerTeam("simpleSpectators").setPlayerPrefix(Component.translatable("gui.hidenseek.game_stage.progress.scoreboard_simple_spectators").append(Component.literal(": §6" + simpleSpectatorsOnlineCount)));
        }

        //Update the Boss Bar to show remaing Hidders Alive count
        hiddersLeftBossBar.setName(Component.translatable("gui.hidenseek.game_stage.progress.remaing_hidders_count", String.valueOf(hiddersOnlineAndAliveCount)));
        hiddersLeftBossBar.setProgress(((float)hiddersOnlineAndAliveCount / hiddersOnlineCount));

        //If the reveal timer is on zero, do the reveal and reset the timer
        if (remaingMinutesUntilRevealPlayers == 0 && remaingSecondsUntilRevealPlayers == 0){
            //Reveal the Seekers
            for (int i = 0; i < seekersList.size(); i++)
                seekersList.get(i).addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
            //Reveal the Hidders with a random sound
            for (int i = 0; i < hiddersList.size(); i++)
                if (hiddersList.get(i).gameMode.getGameModeForPlayer() != GameType.SPECTATOR){
                    //Get a random sound
                    RandomSource random = startedByPlayer.level().getRandom();
                    SoundEvent randomSound = HIDDER_CLUES[random.nextInt(HIDDER_CLUES.length)];
                    //Play the sound
                    startedByPlayer.level().playSound(null, hiddersList.get(i).getX(), hiddersList.get(i).getY(), hiddersList.get(i).getZ(), randomSound, SoundSource.PLAYERS, 1.0f, 0.8f + random.nextFloat() * 0.4f);
                }
            //Refill the itens of seeker inventory, if is desired...
            if (Config.seekerRefillInventorySlots == true)
                for (int i = 0; i < seekersList.size(); i++){
                    GiveItemToPlayer(Config.seekerInventorySlot1, 27, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot2, 28, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot3, 29, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot4, 30, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot5, 31, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot6, 32, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot7, 33, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot8, 34, seekersList.get(i), false);
                    GiveItemToPlayer(Config.seekerInventorySlot9, 35, seekersList.get(i), false);
                    //Broadcast the changes to the Inventory
                    seekersList.get(i).containerMenu.broadcastChanges();
                }
            //Reset the remaing time until reveal
            remaingMinutesUntilRevealPlayers = Config.gameMinutesToReveal;
            remaingSecondsUntilRevealPlayers = Config.gameSecondsToReveal;
        }
        //If was passed 4 iteractions (1 seconds), decrease the reveal timer
        if (ticksElapsedToDecreaseRevealTime == 4){
            remaingSecondsUntilRevealPlayers -= 1;
            if (remaingSecondsUntilRevealPlayers <= 0)
                remaingSecondsUntilRevealPlayers = 0;
            if (remaingSecondsUntilRevealPlayers == 0 && remaingMinutesUntilRevealPlayers >= 1) {
                remaingMinutesUntilRevealPlayers -= 1;
                remaingSecondsUntilRevealPlayers = 59;
            }
            //Reset the iteractions counter
            ticksElapsedToDecreaseRevealTime = 0;
        }
        //Build the remaing time until reveal string
        String remaingTimeUntilRevalStr = "";
        if (remaingMinutesUntilRevealPlayers < 10)
            remaingTimeUntilRevalStr += ("0" + remaingMinutesUntilRevealPlayers);
        if (remaingMinutesUntilRevealPlayers >= 10)
            remaingTimeUntilRevalStr += String.valueOf(remaingMinutesUntilRevealPlayers);
        remaingTimeUntilRevalStr += ":";
        if (remaingSecondsUntilRevealPlayers < 10)
            remaingTimeUntilRevalStr += ("0" + remaingSecondsUntilRevealPlayers);
        if (remaingSecondsUntilRevealPlayers >= 10)
            remaingTimeUntilRevalStr += String.valueOf(remaingSecondsUntilRevealPlayers);
        //Send the remaing time until reveal, to all Players
        for (int i = 0; i < seekersList.size(); i++)
            if (isPlayerOnline(seekersList.get(i)) == true)
                seekersList.get(i).displayClientMessage(Component.translatable("gui.hidenseek.game_stage.progress.remaing_time_until_reveal", remaingTimeUntilRevalStr).withStyle(ChatFormatting.GOLD), true);
        for (int i = 0; i < hiddersList.size(); i++)
            if (isPlayerOnline(hiddersList.get(i)) == true)
                hiddersList.get(i).displayClientMessage(Component.translatable("gui.hidenseek.game_stage.progress.remaing_time_until_reveal", remaingTimeUntilRevalStr).withStyle(ChatFormatting.GOLD), true);
        //Increase the interaction counter, of the reveal timer reducer
        ticksElapsedToDecreaseRevealTime += 1;

        //Prepare the count of Adrenaline Injections spawned now
        int adrenalineInjectionsExisting = 0;
        //Check in all Gold Blocks, by Adrenaline Injections existing
        for (int i = 0; i < goldBlocksFoundList.size(); i++){
            //Create a Hitbox of detection, above the current Gold Block, to get the Entities of type Item above the current Gold block
            List<ItemEntity> itemsFound = startedByPlayer.level().getEntitiesOfClass(ItemEntity.class, new AABB(goldBlocksFoundList.get(i)).move(0.0f, 1.0f, 0.0f));
            //Check items found, and if found a item of Adrenaline Injection, inform it
            for (ItemEntity itemEntity : itemsFound)
                if (itemEntity.getItem().is(ModItems.ADRENALINE_INJECTION.get()) == true)
                    adrenalineInjectionsExisting += 1;
        }
        //If have sufficient Gold Blocks, and is missing some Adrenaline Injections, check inventory of all Players
        if (goldBlocksFoundList.size() >= 2)
            if (adrenalineInjectionsExisting < Config.maxAdrenalineInjectionsSpawn){
                //Check inventory of all Seekers
                for (int i = 0; i < seekersList.size(); i++)
                    if (isPlayerOnline(seekersList.get(i)) == true){
                        //Get the Inventory of this Player
                        Inventory inventory = seekersList.get(i).getInventory();
                        //Search by Adrenaline Injection to consume...
                        for (int x = 0; x < inventory.getContainerSize(); x++)
                            if (inventory.getItem(x).isEmpty() == false && inventory.getItem(x).is(ModItems.ADRENALINE_INJECTION.get()) == true){
                                //Play sound of break
                                startedByPlayer.level().playSound(null, seekersList.get(i).blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 1.0f);
                                //Consume...
                                inventory.setItem(x, ItemStack.EMPTY);
                            }
                    }
                //Check inventory of all Hidders
                for (int i = 0; i < hiddersList.size(); i++)
                    if (isPlayerOnline(hiddersList.get(i)) == true){
                        //Get the Inventory of this Player
                        Inventory inventory = hiddersList.get(i).getInventory();
                        //Search by Adrenaline Injection to consume...
                        for (int x = 0; x < inventory.getContainerSize(); x++)
                            if (inventory.getItem(x).isEmpty() == false && inventory.getItem(x).is(ModItems.ADRENALINE_INJECTION.get()) == true){
                                //Get Player NBT data
                                CompoundTag playerPersistentNbt = hiddersList.get(i).getPersistentData();
                                //Add adrenaline to the Player
                                if (playerPersistentNbt.contains("adrenalinePoints") == true)
                                    playerPersistentNbt.putInt("adrenalinePoints", (playerPersistentNbt.getInt("adrenalinePoints") + Config.adrenalineInjectionIncrease));
                                if (playerPersistentNbt.contains("adrenalinePoints") == false)
                                    playerPersistentNbt.putInt("adrenalinePoints", Config.adrenalineInjectionIncrease);
                                //Send to Player, a packet to update the Adrenaline Points that have
                                ModPacketHandler.SendToPlayer(new ServerToClient_GameProgressAdrenalinePacket(playerPersistentNbt.getInt("adrenalinePoints")), hiddersList.get(i));
                                //Consume...
                                inventory.setItem(x, ItemStack.EMPTY);
                            }
                    }
            }
        //If have sufficient Gold Blocks, and exists less Adrenaline Injections than the required, spawn more one
        if (goldBlocksFoundList.size() >= 2)
            if (adrenalineInjectionsExisting < Config.maxAdrenalineInjectionsSpawn){
                //Get a randomized list of all Gold Blocks
                List<BlockPos> goldBlocksRandomized = new ArrayList<>(goldBlocksFoundList);
                Collections.shuffle(goldBlocksRandomized);
                BlockPos goldBlockSelected = goldBlocksRandomized.get(0);
                //If the current Gold Block selected is the same of the last, skip to next
                if (goldBlockSelected.equals(lastGoldBlockSelected) == true)
                    goldBlockSelected = goldBlocksRandomized.get(1);
                //If have air above the current selected Gold Block...
                if (startedByPlayer.level().getBlockState(goldBlockSelected.above()).is(Blocks.AIR) == true){
                    //Create the Item Entity, of the Adrenaline Injection
                    ItemStack itemStackToSpawn = new ItemStack(ModItems.ADRENALINE_INJECTION.get());
                    ItemEntity itemEntity = new ItemEntity(startedByPlayer.level(), (goldBlockSelected.getX() + 0.5f), (goldBlockSelected.getY() + 1.1f), (goldBlockSelected.getZ() + 0.5f), itemStackToSpawn);
                    itemEntity.setDeltaMovement(0.0f, 0.0f, 0.0f);
                    itemEntity.lifespan = 1200; //<- Set to auto destroy in 60 seconds
                    itemEntity.addTag("entity_to_clear");
                    //Add the Item Entity on the world
                    startedByPlayer.level().addFreshEntity(itemEntity);
                    //Play a sound of Spawn
                    startedByPlayer.level().playSound(null, itemEntity.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    //Emit particles of Spawn
                    ((ServerLevel) startedByPlayer.level()).sendParticles(ParticleTypes.SNEEZE, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 25, 0.2f, 0.2f, 0.2f, 0.05f);
                    //Inform the last Gold Block selected
                    lastGoldBlockSelected = goldBlockSelected;
                }
            }

        //If have no one Seeker Online...
        if (isSeekerOnline == false){
            //Define that Hidders wins
            finalGameResult = GameResult.HiddersWin;
            //Go to next Stage, and stop here
            return GameStage.Celebration;
        }
        //If have no one Hidder Online...
        if (hiddersOnlineCount == 0){
            //Define that Seekers wins
            finalGameResult = GameResult.SeekersWin;
            //Go to next Stage, and stop here
            return GameStage.Celebration;
        }
        //If have only one Hidder Online...
        if (hiddersOnlineCount == 1){
            //If the Hidder is Self Saved, Hidders wins...
            if (hiddersOnlineAndSelfSavedCount == 1 && hiddersOnlineAndAliveCount == 0){
                finalGameResult = GameResult.HiddersWin;
                return GameStage.Celebration;   //<- Go to next Stage, and stop here
            }
            //If the Hidder was Eliminated, Seekers wins...
            if (hiddersOnlineAndEliminatedCount == 1 && hiddersOnlineAndAliveCount == 0){
                finalGameResult = GameResult.SeekersWin;
                return GameStage.Celebration;   //<- Go to next Stage, and stop here
            }
        }
        //If have only two Hidders Online...
        if (hiddersOnlineCount == 2){
            //If one Hidder is Self Saved and one Hidder is Eliminated, is a Draw...
            if (hiddersOnlineAndSelfSavedCount == 1 && hiddersOnlineAndEliminatedCount == 1){
                finalGameResult = GameResult.Draw;
                return GameStage.Celebration;   //<- Go to next Stage, and stop here
            }
            //If all Hidders is Self Saved, so Hidders wins...
            if (hiddersOnlineAndSelfSavedCount == 2){
                finalGameResult = GameResult.HiddersWin;
                return GameStage.Celebration;   //<- Go to next Stage, and stop here
            }
            //If all Hidders was Eliminated, so Seekers wins...
            if (hiddersOnlineAndEliminatedCount == 2){
                finalGameResult = GameResult.SeekersWin;
                return GameStage.Celebration;   //<- Go to next Stage, and stop here
            }
        }
        //If have three or more Hidders Online...
        if (hiddersOnlineCount >= 3){
            //If the majority of Hidders was Self Saved, so Hidders win...
            if (playersSelfSavedPercent > 0.5f){
                finalGameResult = GameResult.HiddersWin;
                return GameStage.Celebration;   //<- Go to next Stage, and stop here
            }
            //If the majority of Hidders was Eliminated, so Seekers win...
            if (playersEliminatedPercent > 0.5f){
                finalGameResult = GameResult.SeekersWin;
                return GameStage.Celebration;   //<- Go to next Stage, and stop here
            }
        }

        //Return the value
        return toReturn;
    }

    public static GameStage ProcessStage_Delayed_Celebration(TickEvent.ServerTickEvent event){
        //Prepare the value to return
        GameStage toReturn = GameStage.Celebration;

        //If is the first interaction, send a "Game Over" message to all players, and register the new scores
        if (celebrationInteractionsElapsed == 0){
            for (int i = 0; i < seekersList.size(); i++)
                if (isPlayerOnline(seekersList.get(i)) == true)
                    seekersList.get(i).displayClientMessage(Component.translatable("gui.hidenseek.game_stage.celebration.game_over").withStyle(ChatFormatting.WHITE), true);
            for (int i = 0; i < hiddersList.size(); i++)
                if (isPlayerOnline(hiddersList.get(i)) == true)
                    hiddersList.get(i).displayClientMessage(Component.translatable("gui.hidenseek.game_stage.celebration.game_over").withStyle(ChatFormatting.WHITE), true);
            //If Hidders wins...
            if (finalGameResult == GameResult.HiddersWin){
                //Prepare the list of scores
                List<String> nicknames = new ArrayList<>();
                List<Integer> scores = new ArrayList<>();
                //Fill the lists...
                for (int i = 0; i < hiddersList.size(); i++){
                    nicknames.add(hiddersList.get(i).getName().getString());
                    scores.add(5);
                }
                //Register the new scores
                targetGameTotem.RegisterPlayersNewScores(nicknames.toArray(String[]::new), scores.stream().mapToInt(Integer::intValue).toArray(), startedByPlayer.level(), targetGameTotemPosition, targetGameTotem.getBlockState());
            }
            //If Seekers wins...
            if (finalGameResult == GameResult.SeekersWin){
                //Prepare the list of scores
                List<String> nicknames = new ArrayList<>();
                List<Integer> scores = new ArrayList<>();
                //Fill the lists...
                for (int i = 0; i < seekersList.size(); i++){
                    nicknames.add(seekersList.get(i).getName().getString());
                    scores.add(5);
                }
                //Register the new scores
                targetGameTotem.RegisterPlayersNewScores(nicknames.toArray(String[]::new), scores.stream().mapToInt(Integer::intValue).toArray(), startedByPlayer.level(), targetGameTotemPosition, targetGameTotem.getBlockState());
            }
        }

        //If is the first interaction, after 1 second, inform Winner to Clients
        if (celebrationInteractionsElapsed == 4){
            //If is a Draw
            if (finalGameResult == GameResult.Draw)
                ModPacketHandler.SendToAllClients(new ServerToClient_GameCelebrationPacket(Component.translatable("gui.hidenseek.game_stage.celebration.draw").getString(), "GOLDEN"));
            //If Hidders wins
            if (finalGameResult == GameResult.HiddersWin)
                ModPacketHandler.SendToAllClients(new ServerToClient_GameCelebrationPacket(Component.translatable("gui.hidenseek.game_stage.celebration.hidders_wins").getString(), "BLUE"));
            //If Seekers wins
            if (finalGameResult == GameResult.SeekersWin)
                ModPacketHandler.SendToAllClients(new ServerToClient_GameCelebrationPacket(Component.translatable("gui.hidenseek.game_stage.celebration.seekers_wins").getString(), "RED"));
        }

        //Run the VFX/SFX of end of the game (after 4 seconds)
        if (celebrationInteractionsElapsed >= 16)
            if (finalGameResult == GameResult.SeekersWin || finalGameResult == GameResult.HiddersWin){
                //Prepare the position to launch a Firework
                BlockPos launchPosition = targetGameTotemPosition;
                //Get the position to launch a Firework
                RandomSource random = startedByPlayer.level().random;
                int launchingRandomOffsetXZ = 10;
                int launchingRandomOffsetY = 5;
                int newX = ((targetGameTotemPosition.getX() - launchingRandomOffsetXZ) + random.nextInt((launchingRandomOffsetXZ * 2) + 1));
                int newY = (targetGameTotemPosition.getY() + 1 + random.nextInt(launchingRandomOffsetY + 1));
                int newZ = ((targetGameTotemPosition.getZ() - launchingRandomOffsetXZ) + random.nextInt((launchingRandomOffsetXZ * 2) + 1));
                launchPosition = new BlockPos(newX, newY, newZ);
                //Get a random Firework
                ItemStack randomFirework = FireworkFactory.GenerateRandomFirework();
                //Launch the Firework
                FireworkRocketEntity fireworkEntity = new FireworkRocketEntity(startedByPlayer.level(), launchPosition.getX(), launchPosition.getY(), launchPosition.getZ(), randomFirework);
                startedByPlayer.level().addFreshEntity(fireworkEntity);
            }

        //If is the last interaction, skip to next Stage (after 9 seconds)
        if (celebrationInteractionsElapsed >= 36)
            toReturn = GameStage.Finished;

        //Increase the interactions elapsed
        celebrationInteractionsElapsed += 1;

        //Return the value
        return toReturn;
    }

    public static GameStage ProcessStage_Delayed_Finished(TickEvent.ServerTickEvent event){
        //Prepare the value to return
        GameStage toReturn = GameStage.Finished;

        //Increase the interactions elapsed
        finishedInteractionsElapsed += 1;

        //If is the first interaction, inform Finished to Clients
        if (finishedInteractionsElapsed == 1)
            ModPacketHandler.SendToAllClients(new ServerToClient_GameFinishedPacket());

        //If is the last interaction, for this, do the Finish code...
        if (finishedInteractionsElapsed == 5){
            //Get a fresh reference for the ServerLevel
            ServerLevel serverLevel = event.getServer().getLevel(ServerLevel.OVERWORLD);
            //Get a fresh list of Online Players
            List<ServerPlayer> onlinePlayers = serverLevel.players();
            //Get the BlockPos, in front of the Game Totem
            BlockState gameTotemState = serverLevel.getBlockState(targetGameTotemPosition);
            BlockPos gameTotemFrontPos = targetGameTotemPosition.relative(gameTotemState.getValue(GameTotemHeadPoweredBlock.FACING));

            //Load the BlockEntity that has the Menu of the Game Totem Head Powered
            if (serverLevel.getBlockEntity(targetGameTotemPosition) instanceof GameTotemHeadPoweredBlockEntity)
                targetGameTotem = ((GameTotemHeadPoweredBlockEntity) serverLevel.getBlockEntity(targetGameTotemPosition));
            //Access the item store capability of the Totem...
            targetGameTotem.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                //Put the Game Badge on the Output Slot
                if (iItemHandler instanceof IItemHandlerModifiable modifiableHandler)
                    modifiableHandler.setStackInSlot(1, new ItemStack(ModItems.MOD_BADGE.get()));

                //Warn to the server that the block was changed, and need to be saved
                targetGameTotem.setChanged();
                //Warn to the server, to send a update packet for all Clients that area near to this BlockEntity, to receive the new synced state
                serverLevel.sendBlockUpdated(targetGameTotemPosition, targetGameTotem.getBlockState(), targetGameTotem.getBlockState(), 3);   //<- Use the flag "3" to ensure that all Players near, receive the update
            });
            //Inform to the Game Totem Head Powered BlockEntity, that the game was finished
            targetGameTotem.SetGameAsFinished(((Level)serverLevel), targetGameTotemPosition, targetGameTotem.getBlockState());

            //If have a reference for a Boss Bar created...
            if (hiddersLeftBossBar != null){
                //Delete the Boss Bar that was used during the Game
                hiddersLeftBossBar.removeAllPlayers();
                hiddersLeftBossBar.setVisible(false);
                hiddersLeftBossBar = null;
            }
            //Delete the Scoreboard that was used during the Game, if it exists
            Scoreboard scoreboard = event.getServer().getScoreboard();
            String[] teamsCreated = new String[]{ "hiddersTotalCount", "hiddersAliveCount", "hiddersEliminated", "hiddersSelfSaved", "simpleSpectators" };
            for (String teamName : teamsCreated){
                PlayerTeam currentTeam = scoreboard.getPlayerTeam(teamName);
                if (currentTeam == null)
                    continue;
                for (String playerName : currentTeam.getPlayers())
                    scoreboard.removePlayerFromTeam(playerName, currentTeam);
                scoreboard.removePlayerTeam(currentTeam);
            }
            Objective scoreboardObjective = scoreboard.getObjective("hidenseek_scb");
            if (scoreboardObjective != null)
                scoreboard.removeObjective(scoreboardObjective);
            //Try to delete all Entities that have the tag of "entity_to_clear"...
            try{
                //Iterate through all Entities found with the Tag to clear...
                serverLevel.getEntities().getAll().forEach(entity -> {
                    if (entity != null)
                        if (entity.getTags().contains("entity_to_clear"))
                            entity.discard();
                });
            }
            catch (Exception e) { LogUtils.getLogger().info("Hide'n Seek Manager: Error when clearing the Entities with tag of 'entity_to_clear'."); }

            //Teleport all Players to the front of the Game Totem
            for (int i = 0; i < onlinePlayers.size(); i++)
                onlinePlayers.get(i).teleportTo(gameTotemFrontPos.getX(), gameTotemFrontPos.getY(), gameTotemFrontPos.getZ());
            //Reset all Players
            for (int i = 0; i < onlinePlayers.size(); i++){
                onlinePlayers.get(i).setGameMode(GameType.ADVENTURE);
                onlinePlayers.get(i).getInventory().clearContent();
                onlinePlayers.get(i).setHealth(20.0f);
                onlinePlayers.get(i).getFoodData().setFoodLevel(20);
                onlinePlayers.get(i).getFoodData().setSaturation(20.0f);
                ScaleTypes.BASE.getScaleData(onlinePlayers.get(i)).setTargetScale(1.0f);
                onlinePlayers.get(i).removeAllEffects();
                CompoundTag playerPersistentNbt = onlinePlayers.get(i).getPersistentData();
                if (playerPersistentNbt.contains("wasEliminated") == true)
                    playerPersistentNbt.remove("wasEliminated");
                if (playerPersistentNbt.contains("wasSelfSaved") == true)
                    playerPersistentNbt.remove("wasSelfSaved");
                if (playerPersistentNbt.contains("adrenalinePoints") == true)
                    playerPersistentNbt.remove("adrenalinePoints");
                if (playerPersistentNbt.contains("beforeUse_soundBait_speedDurationRemaing") == true)
                    playerPersistentNbt.remove("beforeUse_soundBait_speedDurationRemaing");
                if (playerPersistentNbt.contains("beforeUse_soundBait_speedAmplifier") == true)
                    playerPersistentNbt.remove("beforeUse_soundBait_speedAmplifier");
                //Ensure that the Client receive the new inventory visually to display it
                EnsureThatTheInventoryIsUpdatedOnClient(onlinePlayers.get(i), false);
            }

            //Reset all Game Manager Logic, variables
            ticksPassedInCurrentStage = 0;
            startedByPlayer = null;
            startedByNick = "";
            targetGameTotem = null;
            targetGameTotemPosition = null;
            startTimeInMillis = -1;
            isGameBadgeCollectedSuccessfully = false;
            //currentGameState = GameStage.StandBy;
            allaysOfSpectators = null;
            vexesOfSpectators = null;
            hiddersLeftBossBar = null;
            gameScoreboard = null;
            gameScoreboardObjective = null;
            goldBlocksFoundList = null;
            lastGoldBlockSelected = null;
            seekersList = null;
            hiddersList = null;
            gameCountTime = 0;
            addedPlayersToBossBar = false;
            sendedWarnOfProgressStage = false;
            ticksElapsedToDecreaseRevealTime = 0;
            remaingMinutesUntilRevealPlayers = 0;
            remaingSecondsUntilRevealPlayers = 0;
            finalGameResult = GameResult.Unknown;
            celebrationInteractionsElapsed = 0;
            finishedInteractionsElapsed = 0;

            //Save the information to NBT of the Level, to inform that the game was finihed
            GameManagerLevelData.get(serverLevel).SetGameRunning(false);
            GameManagerLevelData.get(serverLevel).SetGameTotemHeadPos(new BlockPos(0, 0, 0));

            //Inform to skip to Stage of StandBy again
            toReturn = GameStage.StandBy;
        }

        //Return the value
        return toReturn;
    }

    //Public auxiliar methods

    public static GameStage GetCurrentGameStage(){
        //Return the current game stage
        return currentGameState;
    }

    public static BlockPos GetBlockOfGameTotem(){
        //Return the BlockPos of the Block of the Game Totem
        return targetGameTotemPosition;
    }

    public static BlockPos GetBlockAtFrontOfGameTotem(){
        //Return the BlockPos of the Block in front of the Game Totem
        //BlockPos frontPos = targetGameTotemPosition.relative(Direction.NORTH);
        BlockState gameTotemState = startedByPlayer.level().getBlockState(targetGameTotemPosition);
        BlockPos gameTotemFrontPos = targetGameTotemPosition.relative(gameTotemState.getValue(GameTotemHeadPoweredBlock.FACING));
        BlockState frontState = startedByPlayer.level().getBlockState(gameTotemFrontPos);
        Block frontBlock = frontState.getBlock();
        return gameTotemFrontPos;
    }

    public static double GetDistanceToTotem(ServerPlayer player, BlockPos totemPos) {
        //Get the center of the Game Totem Head block
        double targetX = totemPos.getX() + 0.5;
        double targetY = totemPos.getY();
        double targetZ = totemPos.getZ() + 0.5;

        //Calculate the exact and human-readable distance from target Player to the Game Totem
        double distSq = player.distanceToSqr(targetX, targetY, targetZ); //<- Calculate the distance²
        return Math.sqrt(distSq);   //<- Use square root to convert to human-readable distance
    }

    public static void GiveItemToPlayer(String itemIdString, int slot, ServerPlayer serverPlayer, boolean runNetworkCalls){
        //Try to give the item to the Player
        try {
            //Get string parts...
            String itemIdStr = itemIdString.split(":nbt\\{")[0];
            String itemNbtStr = ("{" + (itemIdString.split(":nbt\\{")[1].split("\\}:qty\\{")[0]) + "}");
            int itemQtyInt = Integer.parseInt(itemIdString.split("\\}:qty\\{")[1].replace("}", ""));

            //Try to load the item
            StringReader itemReader = new StringReader((itemIdStr + itemNbtStr));
            HolderLookup<Item> lookup = serverPlayer.level().holderLookup(Registries.ITEM);
            ItemParser.ItemResult result = ItemParser.parseForItem(lookup, itemReader);
            ItemStack itemStack = new ItemStack(result.item().value(), itemQtyInt);
            CompoundTag itemNbt = result.nbt();
            if (itemNbt != null)
                itemStack.setTag(itemNbt);
            //Apply the item to the Player
            serverPlayer.getInventory().setItem(slot, itemStack);
            if (runNetworkCalls == true)
                serverPlayer.containerMenu.broadcastChanges();
        }
        catch (Exception exception) {}
    }

    public static void GiveItemToPlayer(Item item, int quantity, int slot, ServerPlayer serverPlayer, boolean runNetworkCalls){
        //Try to load the item
        ItemStack itemStack = new ItemStack(item, quantity);
        //Apply the item to the Player
        serverPlayer.getInventory().setItem(slot, itemStack);
        if (runNetworkCalls == true)
            serverPlayer.containerMenu.broadcastChanges();
    }

    public static boolean isPlayerOnline(ServerPlayer serverPlayer){
        //Prepare the value to return
        boolean toReturn = true;

        //Check if is null
        if (serverPlayer == null)
            toReturn = false;
        //Check if have connection
        if (serverPlayer.connection == null)
            return false;
        //Check if was disconnected
        if (serverPlayer.hasDisconnected() == true)
            toReturn = false;

        //Return the result
        return toReturn;
    }

    public static UUID GetFirstSeekerUUID(){
        //Return the UUID of First Seeker
        return seekersList.get(0).getUUID();
    }

    public static ServerPlayer GetFirstSeekerServerPlayer(){
        //Return the reference for the First Seeker
        return seekersList.get(0);
    }

    public static void ForceRunFinishOfTheGameStateMachine(BlockPos gameTotemPosition){
        //Inform the Game Totem Head position
        targetGameTotemPosition = gameTotemPosition;

        //Force tho skip to Finish Game Stage
        currentGameState = GameStage.Finished;
    }

    public static void PlaySoundForAllPlayersExceptFor(ServerPlayer exceptionServerPlayer, SoundEvent soundToPlay, float volume, float pitch){
        //Play the sound for all Seekers
        for (int i = 0; i < seekersList.size(); i++)
            if (exceptionServerPlayer.getUUID() != seekersList.get(i).getUUID())
                ((ServerLevel)seekersList.get(i).level()).playSound(null, seekersList.get(i).getX(), seekersList.get(i).getY(), seekersList.get(i).getZ(), soundToPlay, SoundSource.PLAYERS, volume, pitch);
        //Play the sound for all Hidders
        for (int i = 0; i < hiddersList.size(); i++)
            if (exceptionServerPlayer.getUUID() != hiddersList.get(i).getUUID())
                ((ServerLevel)hiddersList.get(i).level()).playSound(null, hiddersList.get(i).getX(), hiddersList.get(i).getY(), hiddersList.get(i).getZ(), soundToPlay, SoundSource.PLAYERS, volume, pitch);
    }

    public static void PlayRageBaiterOfHidder(ServerPlayer originServerPlayer){
        //Get a random sound
        RandomSource random = originServerPlayer.level().getRandom();
        SoundEvent randomSound = HIDDER_CLUES[random.nextInt(HIDDER_CLUES.length)];
        //Play the sound
        originServerPlayer.level().playSound(null, originServerPlayer.getX(), originServerPlayer.getY(), originServerPlayer.getZ(), randomSound, SoundSource.PLAYERS, 1.0f, 0.8f + random.nextFloat() * 0.4f);
        //Render a Skulk Sensor particle above the origin player
        BlockPos targetPos = originServerPlayer.blockPosition().above(1);
        BlockPositionSource target = new BlockPositionSource(targetPos);
        VibrationParticleOption particleData = new VibrationParticleOption(target, 20);
        for (int i = 0; i < 8; i++)
            ((ServerLevel) originServerPlayer.level()).sendParticles(particleData, originServerPlayer.getX(), originServerPlayer.getY() + 1.0f, originServerPlayer.getZ(), 1, 0, 0, 0, 0.0f);
        //Get Player NBT data
        CompoundTag playerPersistentNbt = originServerPlayer.getPersistentData();
        //Add adrenaline to the Player
        if (playerPersistentNbt.contains("adrenalinePoints") == true)
            playerPersistentNbt.putInt("adrenalinePoints", (playerPersistentNbt.getInt("adrenalinePoints") + Config.hidderRageBaiterAdrenalineIncrease));
        if (playerPersistentNbt.contains("adrenalinePoints") == false)
            playerPersistentNbt.putInt("adrenalinePoints", Config.hidderRageBaiterAdrenalineIncrease);
        //Send to Player, a packet to update the Adrenaline Points that have
        ModPacketHandler.SendToPlayer(new ServerToClient_GameProgressAdrenalinePacket(playerPersistentNbt.getInt("adrenalinePoints")), originServerPlayer);
    }

    public static void PlayWhistleOfHidder(ServerPlayer originServerPlayer){
        //Get a random sound
        RandomSource random = originServerPlayer.level().getRandom();
        SoundEvent randomSound = HIDDER_WHISTLE[random.nextInt(HIDDER_WHISTLE.length)];
        //Play the sound (uses volume of 2.0f, for a range of 32 blocks)
        originServerPlayer.level().playSound(null, originServerPlayer.getX(), originServerPlayer.getY(), originServerPlayer.getZ(), randomSound, SoundSource.PLAYERS, 2.0f, 1.0f);
        //Reveal the Player temporarily
        originServerPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, (Config.hidderWhistleRevealDuration * 20), 0, false, false));
        //Get Player NBT data
        CompoundTag playerPersistentNbt = originServerPlayer.getPersistentData();
        //Add adrenaline to the Player
        if (playerPersistentNbt.contains("adrenalinePoints") == true)
            playerPersistentNbt.putInt("adrenalinePoints", (playerPersistentNbt.getInt("adrenalinePoints") + Config.hidderWhistleAdrenalineIncrease));
        if (playerPersistentNbt.contains("adrenalinePoints") == false)
            playerPersistentNbt.putInt("adrenalinePoints", Config.hidderWhistleAdrenalineIncrease);
        //Send to Player, a packet to update the Adrenaline Points that have
        ModPacketHandler.SendToPlayer(new ServerToClient_GameProgressAdrenalinePacket(playerPersistentNbt.getInt("adrenalinePoints")), originServerPlayer);
    }

    public static SoundEvent[] GetHidderClues(){
        //Return the Hidder Clues reference
        return HIDDER_CLUES;
    }

    public static List<BlockPos> FindGoldBlocks(Level level, BlockPos centerPos, int radius) {
        //Prepare the List of Gold Blocks to return
        List<BlockPos> toReturn = new ArrayList<>();

        //Set the bounds of the search
        int minX = centerPos.getX() - radius;
        int maxX = centerPos.getX() + radius;
        int minY = centerPos.getY() - radius;
        int maxY = centerPos.getY() + radius;
        int minZ = centerPos.getZ() - radius;
        int maxZ = centerPos.getZ() + radius;

        //Do iteraction in the search bound
        for (int x = minX; x <= maxX; x++)
            for (int z = minZ; z <= maxZ; z++) {
                //If the chunk here, is not loaded, stop here
                if (level.hasChunkAt(new BlockPos(x, 0, z)) == false)
                    continue;

                for (int y = minY; y <= maxY; y++) {
                    //Get the current pos of the current Block
                    BlockPos currentPos = new BlockPos(x, y, z);

                    //If the Block is a Gold Block, store it in the list
                    if (level.getBlockState(currentPos).is(Blocks.GOLD_BLOCK) == true)
                        toReturn.add(currentPos.immutable());
                }
            }

        //Return the list
        return toReturn;
    }

    public static void EnsureThatTheInventoryIsUpdatedOnClient(ServerPlayer serverPlayer, boolean runMoreAgressive){
        //Ensure that the changes was broadcasted
        serverPlayer.containerMenu.broadcastChanges();
        //If is desired to run more agressive...
        if (runMoreAgressive == true){
            //Increment the Server Player Inventory state ID, to ensure that the Client will recognize this Inventory as a new
            serverPlayer.containerMenu.incrementStateId();
            //Ensure that the Server was done all the logic steps on the Player Inventory, first
            serverPlayer.containerMenu.slotsChanged(serverPlayer.getInventory());
        }
        //Send the packet of Inventory changes
        serverPlayer.connection.send(new ClientboundContainerSetContentPacket(0, serverPlayer.containerMenu.getStateId(), serverPlayer.containerMenu.getItems(), serverPlayer.containerMenu.getCarried()));
    }

    public static void CheckIfTheItemCooldownIsReallyEndedOnServer(ServerPlayer serverPlayer, Item itemToCheck){
        //If the Hashmap of Items and their Cooldowns was not initialized, initialize it
        if (itemsAndTheirCooldowns == null){
            itemsAndTheirCooldowns = new HashMap<>();
            itemsAndTheirCooldowns.put(ModItems.WHISTLE.get(), Config.hidderWhistleCooldown);
            itemsAndTheirCooldowns.put(ModItems.RAGE_BAITER.get(), Config.hidderRageBaiterCooldown);
            itemsAndTheirCooldowns.put(ModItems.TOTEM_TRACKER.get(), Config.hidderTotemTrackerCooldown);
            itemsAndTheirCooldowns.put(ModItems.SMOKE_BOMB.get(), Config.hidderSmokeBombCooldown);
            itemsAndTheirCooldowns.put(ModItems.ZERO_GRAVITY.get(), Config.hidderZeroGravityCooldown);
            itemsAndTheirCooldowns.put(ModItems.LEAP_OF_FAITH.get(), Config.hidderLeapOfFaithCooldown);
            itemsAndTheirCooldowns.put(ModItems.CAMOUFLAGE.get(), Config.hidderCamouflageCooldown);
            itemsAndTheirCooldowns.put(ModItems.LADDER_SPECIALIST.get(), Config.hidderLadderSpecialistCooldown);
            itemsAndTheirCooldowns.put(ModItems.SOUND_BAIT.get(), Config.hidderSoundBaitCooldown);
            itemsAndTheirCooldowns.put(ModItems.BREATHING_UNDERWATER.get(), Config.hidderBreathingUnderwaterCooldown);
        }

        //if the Item requested by the Client, is unknown by this method, cancel here
        if (itemsAndTheirCooldowns.containsKey(itemToCheck) == false)
            return;

        //Check on this Server Side, the count of remaing ticks that this Item Cooldown have
        int remaingTicks = Math.round(((float)(itemsAndTheirCooldowns.get(itemToCheck).intValue() * 20) * serverPlayer.getCooldowns().getCooldownPercent(itemToCheck, 0.0f)));
        //Considering a network latency of upload and download of Client/Server, if this item have more than 6 Ticks (300ms) of remaing Cooldown, inform the Client of the remaing Cooldown
        if (remaingTicks > 6)
            serverPlayer.connection.send(new ClientboundCooldownPacket(itemToCheck, remaingTicks));
    }

    public static void AddAllayToRepresentServerPlayer(ServerPlayer serverPlayer){
        //Create a Allay with no AI, to represent this Player
        Allay specAllay = EntityType.ALLAY.create(serverPlayer.level());
        if (specAllay != null){
            specAllay.setCanPickUpLoot(false);
            specAllay.setNoAi(true);
            specAllay.goalSelector.getAvailableGoals().removeIf(goal -> true);
            specAllay.setInvulnerable(true);
            specAllay.setNoGravity(true);
            specAllay.noPhysics = true;
            specAllay.blocksBuilding = false;
            specAllay.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
            specAllay.setMaxUpStep(0.0f);
            specAllay.refreshDimensions();
            specAllay.setSilent(true);
            specAllay.setPersistenceRequired();
            specAllay.setCustomName(serverPlayer.getDisplayName());
            specAllay.setCustomNameVisible(false);
            specAllay.setPos(serverPlayer.position());
            specAllay.addTag("entity_to_clear");
            specAllay.addTag(("owner_uuid_" + serverPlayer.getUUID().toString()));
            serverPlayer.level().addFreshEntity(specAllay);
        }

        //If the Allays of Spectators HashMap was not initialized, initialize it
        if (allaysOfSpectators == null)
            allaysOfSpectators = new HashMap<>();

        //Add this Allay to the HashMap of Allays of Spectators
        allaysOfSpectators.put(specAllay, serverPlayer);
    }

    public static void AddVexToRepresentServerPlayer(ServerPlayer serverPlayer){
        //Create a Vex with no AI, to represent this Player
        Vex specVex = EntityType.VEX.create(serverPlayer.level());
        if (specVex != null){
            specVex.setCanPickUpLoot(false);
            specVex.setNoAi(true);
            specVex.goalSelector.getAvailableGoals().removeIf(goal -> true);
            specVex.setIsCharging(false);
            specVex.setInvulnerable(true);
            specVex.setNoGravity(true);
            specVex.noPhysics = true;
            specVex.blocksBuilding = false;
            specVex.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
            specVex.setMaxUpStep(0.0f);
            specVex.refreshDimensions();
            specVex.setSilent(true);
            specVex.setPersistenceRequired();
            specVex.setLimitedLife(0);
            specVex.setCustomName(serverPlayer.getDisplayName());
            specVex.setCustomNameVisible(false);
            specVex.setPos(serverPlayer.position());
            specVex.addTag("entity_to_clear");
            specVex.addTag(("owner_uuid_" + serverPlayer.getUUID().toString()));
            serverPlayer.level().addFreshEntity(specVex);
        }

        //If the Vex of Spectators HashMap was not initialized, initialize it
        if (vexesOfSpectators == null)
            vexesOfSpectators = new HashMap<>();

        //Add this Vex to the HashMap of Vexes of Spectators
        vexesOfSpectators.put(specVex, serverPlayer);
    }

    public static void ProcessAllSpectatorAllaysAndVexesOnServerTick(TickEvent.ServerTickEvent event){
        //Iterate through all Allays of Spectators, if have Allays for it
        if (allaysOfSpectators != null)
            for (Map.Entry<Allay, ServerPlayer> entry : allaysOfSpectators.entrySet()) {
                //Get the references
                Allay targetAllay = entry.getKey();
                ServerPlayer ownerServerPlayer = entry.getValue();
                //If have a null reference, skip to next
                if (targetAllay == null || ownerServerPlayer == null)
                    continue;
                //Prepare the Y offset of the Allay to the current Player position
                float[] yOffset = new float[]{ 0.0f };
                AABB searchA = new AABB((ownerServerPlayer.getX() - 0.5f), (ownerServerPlayer.getY() - 0.6f), (ownerServerPlayer.getZ() - 0.5f), (ownerServerPlayer.getX() + 0.5f), (ownerServerPlayer.getY() + 2.2f), (ownerServerPlayer.getZ() + 0.5f));
                ownerServerPlayer.level().getEntities(EntityTypeTest.forClass(Player.class), searchA, foundPlayer -> {
                    //If found a Player in the target place where the Allay is going, get the reference for the Found ServerPlayer...
                    if (foundPlayer instanceof ServerPlayer foundServerPlayer)
                        if (foundServerPlayer != ownerServerPlayer && foundServerPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR){
                            //Add a height in the Y where the Allay will be moved
                            yOffset[0] = (float)(foundPlayer.getEyePosition().y + 0.3d);
                            //Stop the search...
                            return true;
                        }
                    //Continue the search...
                    return false;
                });
                //Move the Allay to the current Player position and rotate their Body to represent the Player
                if (yOffset[0] == 0.0f)
                    targetAllay.moveTo(ownerServerPlayer.getX(), (ownerServerPlayer.getY() + 0.0f), ownerServerPlayer.getZ(), ownerServerPlayer.getYRot(), ownerServerPlayer.getXRot());
                if (yOffset[0] != 0.0f)
                    targetAllay.moveTo(ownerServerPlayer.getX(), yOffset[0], ownerServerPlayer.getZ(), ownerServerPlayer.getYRot(), ownerServerPlayer.getXRot());
                //Control the Head of the Allay, to represent the Player
                targetAllay.setYHeadRot(ownerServerPlayer.getYRot());
                targetAllay.setYBodyRot(ownerServerPlayer.getYRot());
            }

        //Iterate through all Vexes of Spectators, if have Vexes for it
        if (vexesOfSpectators != null)
            for (Map.Entry<Vex, ServerPlayer> entry : vexesOfSpectators.entrySet()){
                //Get the references
                Vex targetVex = entry.getKey();
                ServerPlayer ownerServerPlayer = entry.getValue();
                //If have a null reference, skip to next
                if (targetVex == null || ownerServerPlayer == null)
                    continue;
                //Move the Vex to the current Player position and rotate their Body to represent the Player
                targetVex.moveTo(ownerServerPlayer.getX(), (ownerServerPlayer.getY() + 0.0f), ownerServerPlayer.getZ(), ownerServerPlayer.getYRot(), ownerServerPlayer.getXRot());
                //Control the Head of the Vex, to represent the Player
                targetVex.setYHeadRot(ownerServerPlayer.getYRot());
                targetVex.setYBodyRot(ownerServerPlayer.getYRot());
            }
    }

    public static void ClearAllDroppedItemsOnNextTick(){
        //Inform that is needed to clear all items on next tick
        ticksUntilClearDroppedItems = 3;
    }

    public static void ClearAllDroppedItemsNow(TickEvent.ServerTickEvent event){
        //Try to delete all Dropped Items
        try{
            //Iterate through all ItemEntities existing...
            event.getServer().getLevel(ServerLevel.OVERWORLD).getEntities().getAll().forEach(entity -> {
                if (entity instanceof ItemEntity item)
                    item.discard();
            });
            //Log that all the Items was cleared
            LogUtils.getLogger().info("Hide'n Seek Manager: Dropped Items was cleared.");
        }
        catch (Exception e) { LogUtils.getLogger().info("Hide'n Seek Manager: Error when clearing the dropped Items."); }
    }
}