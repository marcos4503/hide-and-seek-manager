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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.hidenseek.utils.GameManagerLogic;

/*
 * This class do actions on Players when a Player receive Damage.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnDamage {

    //Public events

    @SubscribeEvent
    public void onDamage(LivingHurtEvent event){
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

        //If have a game running...
        if (GameManagerLogic.GetCurrentGameStage() == GameManagerLogic.GameStage.Progress){
            //If the Player have Invisibility, clear it
            if (serverPlayer.hasEffect(MobEffects.INVISIBILITY) == true){
                //Clear ONLY the Invisiblity effect
                serverPlayer.removeEffect(MobEffects.INVISIBILITY);
                //Play the particles of exiting the Invisibility
                ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.POOF, serverPlayer.getX(), serverPlayer.getY() + 0.5f, serverPlayer.getZ(), 15, 0.3f, 0.5f, 0.3f, 0.0f);
                ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.PORTAL, serverPlayer.getX(), serverPlayer.getY() + 0.5f, serverPlayer.getZ(), 30, 0.5f, 0.5f, 0.5f, 0.0f);
            }

            //If the damage will kill the Player, cancel it ant put the Player in spectator mode
            if (event.getAmount() >= serverPlayer.getHealth()){
                //Cancel the damage
                event.setCanceled(true);
                //Reset the Player HP
                serverPlayer.setHealth(20.0f);
                serverPlayer.setRemainingFireTicks(0);
                serverPlayer.clearFire();
                serverPlayer.setInvulnerable(true);
                //Change the Game Mode to spectator mode
                serverPlayer.setGameMode(GameType.SPECTATOR);
                //Play a sound effect of Explosion
                ((ServerLevel)serverPlayer.level()).playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);
                ((ServerLevel)serverPlayer.level()).sendParticles(ParticleTypes.PORTAL, serverPlayer.getX(), (serverPlayer.getY() + 1.0f), serverPlayer.getZ(), 40, 0.2f, 0.5f, 0.2f, 0.1f);
                //Show a Lightning on the Death local
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(((ServerLevel)serverPlayer.level()));
                if (lightningBolt != null){
                    lightningBolt.moveTo(Vec3.atBottomCenterOf(serverPlayer.blockPosition().below()));
                    lightningBolt.setVisualOnly(true);
                    ((ServerLevel)serverPlayer.level()).addFreshEntity(lightningBolt);
                }
                //Send a message to Player
                serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 10));
                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("gui.hidenseek.game_stage.progress.hidder_eliminated").withStyle(ChatFormatting.DARK_RED)));
                serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("gui.hidenseek.game_stage.progress.hidder_eliminated_subtitle")));
                //Teleport the Player to front of Game Totem
                BlockPos frontBlock = GameManagerLogic.GetBlockAtFrontOfGameTotem();
                serverPlayer.teleportTo(((ServerLevel)serverPlayer.level()), frontBlock.getX(), frontBlock.getY(), frontBlock.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
                //Add a information to the Player
                CompoundTag playerPersistentNbt = serverPlayer.getPersistentData();
                playerPersistentNbt.putBoolean("wasEliminated", true);
                //Reset the ivulnerability, after a safe ensured
                serverPlayer.setInvulnerable(false);
            }
        }
        //If don't have a game running...
        if (GameManagerLogic.GetCurrentGameStage() != GameManagerLogic.GameStage.Progress)
            event.setCanceled(true);   //<- Cancel the damage
    }
}