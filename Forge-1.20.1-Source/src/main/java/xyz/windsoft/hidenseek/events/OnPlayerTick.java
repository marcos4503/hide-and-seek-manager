package xyz.windsoft.hidenseek.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import xyz.windsoft.hidenseek.effect.ModEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * This class do actions on Players when the Player Tick is running.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnPlayerTick {

    //Public static enums
    public static enum LadderMoveDirection {
        Up,
        Stationary,
        Down
    }

    //Private final variables
    private final Map<UUID, Double> lastPlayerYPositions = new HashMap<>();

    //Public events

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        //If not is the logical server, stop here
        //if (FMLEnvironment.dist == Dist.CLIENT)
        //    return;

        //If is not the END of tick, stop here
        if (event.phase != TickEvent.Phase.END)
            return;

        //If the entity is null, stop here
        if (event.player == null)
            return;

        //If not is the logical server, stop here
        if (event.side == LogicalSide.CLIENT)
            return;

        //If the entity of this event, is not a Server Player, cancel
        if ((event.player instanceof ServerPlayer) == false)
            return;



        //Get the Server Player data
        ServerPlayer serverPlayer = ((ServerPlayer) event.player);
        UUID playerUuid = serverPlayer.getUUID();

        //If the Player is not climbing, stop here...
        if (serverPlayer.onClimbable() == false)
            return;
        //If the Player don't have the Effect of Ladder Specialist, stop here...
        if (serverPlayer.hasEffect(ModEffects.LADDER_SPECIALIST.get()) == false)
            return;

        //Get Player informations when climbing...
        double rawRealCurrentMotionDeltaY = (serverPlayer.getY() - lastPlayerYPositions.getOrDefault(playerUuid, serverPlayer.getY()));
        LadderMoveDirection realCurrentMotion = ((rawRealCurrentMotionDeltaY >= 0.02f) ? LadderMoveDirection.Up : ((rawRealCurrentMotionDeltaY <= -0.02f) ? LadderMoveDirection.Down : LadderMoveDirection.Stationary));
        boolean isCrouchingNow = serverPlayer.isShiftKeyDown();
        boolean isBodyFacingLadder = isPlayerBodyFacingLadder(serverPlayer);
        boolean isLookingUp = ((serverPlayer.getXRot() <= -45.0f) ? true : false);
        boolean isLookingHorizon = ((serverPlayer.getXRot() > -45.0f && serverPlayer.getXRot() < 45.0f) ? true : false);
        boolean isLookingDown = ((serverPlayer.getXRot() >= 45.0f) ? true : false);

        //If is moving up through Ladder...
        if (isCrouchingNow == false && realCurrentMotion == LadderMoveDirection.Up && isLookingUp == true && isBodyFacingLadder == true){
            //Get the needed Positions informations
            BlockPos playerBlockPos = serverPlayer.blockPosition();
            BlockPos playerHeadBlockPos = playerBlockPos.above();
            BlockPos playerAboveHeadBlockPos = playerHeadBlockPos.above(2);
            BlockState playerAboveHeadBlockState = serverPlayer.level().getBlockState(playerAboveHeadBlockPos);
            //Check if above Player Head have a Ladder...
            boolean haveLadderAboveHead = (playerAboveHeadBlockState.getBlock() instanceof LadderBlock);
            //If above the Player have Ladders to be climbed...
            if (haveLadderAboveHead == true){
                //Add a temporary and fast Levitation Effect, to give a Boost for the Player when moving up
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 3, 1, false, false));
                //Spawn a Boost particle
                ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.ELECTRIC_SPARK, serverPlayer.getX(), (serverPlayer.getY() + 0.0f), serverPlayer.getZ(), 2, 0.1f, 0.1f, 0.1f, 0.05f);
            }
            //If above the Player don't have Ladders to be climbed...
            if (haveLadderAboveHead == false)
                if (serverPlayer.hasEffect(MobEffects.LEVITATION) == true)   //<- If the Player have a residual Levitation, added by this code...
                    if (serverPlayer.getEffect(MobEffects.LEVITATION).getDuration() <= 3){
                        //Clear the Levitation
                        serverPlayer.removeEffect(MobEffects.LEVITATION);
                        //Reset the momentum in Y axis of the Player
                        Vec3 currentMotion = serverPlayer.getDeltaMovement();
                        serverPlayer.setDeltaMovement(currentMotion.x, 0.0f, currentMotion.z);
                        //Warn to the Client, that the speed was changed abruptly
                        serverPlayer.hurtMarked = true;
                        //Spawn a end Boost particles
                        ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.SMOKE, serverPlayer.getX(), (serverPlayer.getY() + 0.0f), serverPlayer.getZ(), 50, 0.1f, 0.1f, 0.1f, 0.05f);
                    }
        }
        //If is moving down through Ladder...
        if (isCrouchingNow == false && realCurrentMotion == LadderMoveDirection.Down && isLookingDown == true){
            //Get the needed positions informations
            BlockPos playerBlockPos = serverPlayer.blockPosition();
            BlockPos playerHeadBlockPos = playerBlockPos.above();
            BlockPos playerAboveHeadBlockPos = playerHeadBlockPos.above(1);
            BlockState playerAboveHeadBlockState = serverPlayer.level().getBlockState(playerAboveHeadBlockPos);
            BlockPos playerBelowFootBlockPos = playerBlockPos.below(2);
            BlockState playerBelowFootBlockState = serverPlayer.level().getBlockState(playerBelowFootBlockPos);
            //Check if above Player Head have a Ladder...
            boolean haveLadderAboveHead = (playerAboveHeadBlockState.getBlock() instanceof LadderBlock);
            //Check if below Player Foot have a Ladder...
            boolean haveLadderBelowFood = (playerBelowFootBlockState.getBlock() instanceof LadderBlock);
            //If above and below the Player have Ladders to be climbed...
            if (haveLadderAboveHead == true && haveLadderBelowFood == true){
                //Do a micro-teleport to down direction, in the Ladder
                serverPlayer.teleportTo(serverPlayer.getX(), (serverPlayer.getY() - 0.4f), serverPlayer.getZ());
                //Reset the fall distance, to avoid fall damage to Player
                serverPlayer.fallDistance = 0.0f;
                //Warn to the Client, that the speed was changed abruptly
                serverPlayer.hurtMarked = true;
                //Spawn a Boost particle
                ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.CLOUD, serverPlayer.getX(), (serverPlayer.getY() - 0.5f), serverPlayer.getZ(), 2, 0.1f, 0.1f, 0.1f, 0.05f);
                ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.CLOUD, serverPlayer.getX(), (serverPlayer.getY() - 0.0f), serverPlayer.getZ(), 2, 0.1f, 0.1f, 0.1f, 0.05f);
                //For each 3 ticks...
                if (serverPlayer.tickCount % 3 == 0){
                    //Play sounds of Boost
                    serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.LADDER_STEP, SoundSource.PLAYERS, 0.15f, 1.0f);
                    serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.WOOL_STEP, SoundSource.PLAYERS, 0.3f, 1.0f);
                }
            }
        }

        //Save the updated current Y position of this Player...
        lastPlayerYPositions.put(playerUuid, serverPlayer.getY());
    }

    //Private auxiliar methods

    private boolean isPlayerBodyFacingLadder(ServerPlayer serverPlayer){
        //Prepare the response to return
        boolean toReturn = false;

        //Get informations about Block that the Player is inside ou above, now
        BlockPos currentBlockPos = serverPlayer.blockPosition();
        BlockState currentBlockPosState = serverPlayer.level().getBlockState(currentBlockPos);
        //If the Player is in a Ladder...
        if (currentBlockPosState.getBlock() instanceof LadderBlock) {
            //Get the direction where the Ladder is facing at
            Direction ladderFacing = currentBlockPosState.getValue(LadderBlock.FACING);
            //Get the opposite direction of where the Ladder is facing at, where this direction is the direction that the Player body should face at
            Direction ladderFacingOpposite = ladderFacing.getOpposite();

            //Get the direction where Player body is facing now
            Direction playerFacing = serverPlayer.getDirection();

            //If the Player body is facing at the Ladder, inform it
            if (playerFacing == ladderFacingOpposite)
                toReturn = true;
        }

        //Return the value
        return toReturn;
    }
}