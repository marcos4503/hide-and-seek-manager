package xyz.windsoft.hidenseek.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.hidenseek.block.ModBlocks;
import xyz.windsoft.hidenseek.utils.GameManagerLogic;

/*
 * This class do actions on Players when a Player interact with the Game Totem Head Powered.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnRightClickBlock {

    //Public events

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;

        //If the entity of this event, is not a Server Player, cancel
        if ((event.getEntity() instanceof ServerPlayer) == false)
            return;



        //Get the Server Player data
        ServerPlayer serverPlayer = ((ServerPlayer) event.getEntity());
        //Get interaction data
        BlockPos interactBlockPos = event.getPos();
        BlockState interactBlockState = event.getLevel().getBlockState(interactBlockPos);

        //If is not in the Adventure mode, stop here
        if (serverPlayer.gameMode.getGameModeForPlayer() != GameType.ADVENTURE)
            return;

        //If is not interacting with Game Totem Head Powered, stop here
        if (interactBlockState.is(ModBlocks.GAME_TOTEM_HEAD_POWERED_BLOCK.get()) == false)
            return;

        //If the game is not running now, stop here
        if (GameManagerLogic.GetCurrentGameStage() != GameManagerLogic.GameStage.Progress)
            return;

        //If this Player is a Seeker, stop here
        if (serverPlayer.getUUID().equals(GameManagerLogic.GetFirstSeekerUUID()) == true)
            return;

        //Put the Player in spectator mode...
        //Reset the Player HP
        serverPlayer.setHealth(20.0f);
        //Change the Game Mode to spectator mode
        serverPlayer.setGameMode(GameType.SPECTATOR);
        //Play a sound effect of Totem Use
        ((ServerLevel)serverPlayer.level()).playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
        ((ServerLevel)serverPlayer.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, serverPlayer.getX(), (serverPlayer.getY() + 1.0f), serverPlayer.getZ(), 25, 0.5f, 0.5f, 0.5f, 0.1f);
        //Play a sound of Totem Use for all other Players, too
        GameManagerLogic.PlaySoundForAllPlayersExceptFor(serverPlayer, SoundEvents.TOTEM_USE, 0.35f, 1.2f);
        //Send a message to Player
        serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 10));
        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("gui.hidenseek.game_stage.progress.hidder_self_saved").withStyle(ChatFormatting.DARK_GREEN)));
        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("gui.hidenseek.game_stage.progress.hidder_self_saved_subtitle")));
        //Add a information to the Player
        CompoundTag playerPersistentNbt = serverPlayer.getPersistentData();
        playerPersistentNbt.putBoolean("wasSelfSaved", true);
        //Add a Allay to represent this Player on Spectator
        GameManagerLogic.AddAllayToRepresentServerPlayer(serverPlayer);
    }
}
