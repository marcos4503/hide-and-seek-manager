package xyz.windsoft.hidenseek.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;
import xyz.windsoft.hidenseek.entity.ModEntities;
import xyz.windsoft.hidenseek.item.ModItems;
import xyz.windsoft.hidenseek.network.ModPacketHandler;
import xyz.windsoft.hidenseek.network.ServerToPlayer_GameProgressSoundBaitFeedbackPacket;
import xyz.windsoft.hidenseek.utils.GameManagerLogic;

/*
 * This class creates the custom behavior for the entity of "Sound Bait"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class SoundBaitEntity extends ThrowableItemProjectile {

    //Private static final Entity Data Accessor keys. This Entity Data Accessors stores data in Server for each instance of this Entity in Server, and sync these data for each Client that see a instance of this Entity
    private static final EntityDataAccessor<Boolean> IS_PLANTED_SYNCED_DATA = SynchedEntityData.defineId(SoundBaitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_NOISING_SYNCED_DATA = SynchedEntityData.defineId(SoundBaitEntity.class, EntityDataSerializers.BOOLEAN);

    //Private variables
    private int elapsedTicksPlanted = 0;

    //Public methods

    public SoundBaitEntity(EntityType<? extends SoundBaitEntity> type, Level level) {
        //Repass this call to Parent class of this Entity, when the game is creating this Entity
        super(type, level);
    }

    public SoundBaitEntity(Level level, LivingEntity ownerEntity) {
        //Repass this call to Parent class of this Entity, when this Entity is being created by a item of a Player
        super(ModEntities.SOUND_BAIT.get(), ownerEntity, level);

        //Force this Entity to have their default scale, on create it
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
        scaleData.setTargetScale(1.0f);
        scaleData.setScaleTickDelay(0);
        scaleData.tick();
    }

    @Override
    protected void defineSynchedData() {
        //Repass this call to the Parent class, to game run needed steps
        super.defineSynchedData();
        //Register the Entity Data Accessor keys for this Entity instance, with default values...
        //All the Entitiy Data Accessor data of this Entity instance, will be automatically synced from Server to Clients that see this Entity instance...
        this.entityData.define(IS_PLANTED_SYNCED_DATA, false);
        this.entityData.define(IS_NOISING_SYNCED_DATA, false);
    }

    @Override
    protected Item getDefaultItem() {
        //Inform the item source of this Throwable Item Projectile, for the case of the game need of this reference
        return ModItems.SOUND_BAIT.get();
    }

    @Override
    protected float getGravity() {
        //Inform the gravity that should be applyied to this item. This determinate the parabollical curve of this Item Projectile, when Thrown
        return 0.04f;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        //Repass the call to the Parent of this class, to run the needed base game steps
        super.onHit(hitResult);

        //If not is the logical server, stop here
        if (this.level().isClientSide() == true)
            return;

        //If was collided with a Block...
        if (hitResult.getType() == HitResult.Type.BLOCK){
            //Do a re-position of this Entity, instantly in the exact point of the impact, before the next frame on Clients, be rendered. This avoid that the Clients try to do a extrapolation, rendering this Entity more below than really is.
            this.setPos(this.getX(), hitResult.getLocation().y + 0.02f, this.getZ());
            //Get the delta movement of this Entity
            Vec3 movementDelta = this.getDeltaMovement();
            double movementDeltaMagnitude = movementDelta.length();

            //If have enough momentum to bounce
            if (movementDeltaMagnitude > 0.15f && this.onGround() == false){
                //Prepare the Vector3 of the bounce to be applyied
                double bounceX = movementDelta.x;
                double bounceY = movementDelta.y;
                double bounceZ = movementDelta.z;
                //Detect if was collided with a real Block
                boolean wasCollidedWithRealBlock = ((hitResult instanceof BlockHitResult blockHitResult) ? true : false);
                //If was really collided with a Block...
                if (wasCollidedWithRealBlock == true) {
                    //Get the face of the Block that was collided with
                    Direction collisionFace = ((BlockHitResult)hitResult).getDirection();
                    //Revert the Vector3 bounce, relative to the face of collision of the Block
                    if (collisionFace.getAxis() == Direction.Axis.X) { bounceX = (bounceX * -1.0f); }
                    if (collisionFace.getAxis() == Direction.Axis.Y) { bounceY = (bounceY * -1.0f); }
                    if (collisionFace.getAxis() == Direction.Axis.Z) { bounceZ = (bounceZ * -1.0f); }
                }
                //If was not collided with a Block...
                if (wasCollidedWithRealBlock == false){
                    //Rever the entire Vector3 bounce
                    bounceX = (bounceX * -1.0f);
                    bounceY = (bounceY * -1.0f);
                    bounceZ = (bounceZ * -1.0f);
                }
                //Apply the Vector3 of the bounce, in the speed of the Entity, but with a speed converted to 35% of the original
                this.setDeltaMovement(new Vec3(bounceX, bounceY, bounceZ).scale(0.35f));
                //Inform to Clients that the speed of this Entity was changed abruptly. This avoid that the Clients try to do a extrapolation, rendering this Entity more below than really is.
                this.hasImpulse = true;
                this.hurtMarked = true;
                //Move the Entity a little to up, to ensure that the Entity is not clipping at ground
                this.setPos(this.getX(), hitResult.getLocation().y + 0.15f, this.getZ());
            }
            //If not have enough momentum to bounce...
            if (movementDeltaMagnitude <= 0.15f || this.onGround() == true){
                //Stop the movement
                this.setDeltaMovement(Vec3.ZERO);
                //Inform that is stopped and planted
                this.setNoGravity(true);
                this.hasImpulse = false;
                this.setOnGround(true);
                this.setPlanted(true);
                //Move the Entity a little to up, to ensure that the Entity is not clipping at groud
                this.setPos(this.getX(), hitResult.getLocation().y + 0.15f, this.getZ());
            }
        }
        //If was collided with a Entity...
        if (hitResult.getType() == HitResult.Type.ENTITY){
            //...
        }
    }

    @Override
    public void tick() {
        //Repass this call to the Parent class of this, to game run the base steps
        super.tick();

        //If not is the logical server, stop here
        if (this.level().isClientSide() == true)
            return;

        //If is planted, force this Entity to still stopped
        if (this.isPlanted() == true)
            this.setDeltaMovement(Vec3.ZERO);

        //If is planted and on ground...
        if (this.isPlanted() == true && this.onGround() == true){
            //After 1.5 seconds (30 ticks), play the noise
            if (elapsedTicksPlanted == 30){
                //Inform that is noising
                this.setNoising(true);
                //If this Entity have a Owner, and is a Player, send a Packet to Player, to show a Feedback of this noising
                if (this.getOwner() != null)
                    if (this.getOwner() instanceof ServerPlayer ownerServerPlayer){
                        //Prepare the distance of this Bait to the Seeker
                        int distanceToSeeker = -1;
                        //If have a game running, and exists a Seeker...
                        if (GameManagerLogic.GetCurrentGameStage() == GameManagerLogic.GameStage.Progress && GameManagerLogic.GetFirstSeekerServerPlayer() != null)
                            distanceToSeeker = ((int)this.position().distanceTo(GameManagerLogic.GetFirstSeekerServerPlayer().position()));
                        //Send the packet
                        ModPacketHandler.SendToPlayer(new ServerToPlayer_GameProgressSoundBaitFeedbackPacket(distanceToSeeker), ownerServerPlayer);
                    }
                //Render a Skulk Sensor particle above this Entity
                BlockPos targetPos = this.blockPosition().above(1);
                BlockPositionSource target = new BlockPositionSource(targetPos);
                VibrationParticleOption particleData = new VibrationParticleOption(target, 20);
                for (int i = 0; i < 8; i++)
                    ((ServerLevel) this.level()).sendParticles(particleData, this.getX(), this.getY() + 1.0f, this.getZ(), 1, 0, 0, 0, 0.0f);
                //Get a random sound
                RandomSource random = this.level().getRandom();
                SoundEvent randomSound = GameManagerLogic.GetHidderClues()[random.nextInt(GameManagerLogic.GetHidderClues().length)];
                //Play the sound
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), randomSound, SoundSource.PLAYERS, 2.0f, 0.8f + random.nextFloat() * 0.4f);
            }
            //After 2.5 seconds (50 ticks), stop the noising mode
            if (elapsedTicksPlanted == 50){
                //Inform that is not noising
                this.setNoising(false);
            }
            //After 6.5 seconds (130 ticks), discard this entity
            if (elapsedTicksPlanted == 130){
                //Discard this entity from world
                this.discard();
                //Play particles on discard
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.0f, this.getZ(), 15, 0.2f, 0.2f, 0.2f, 0.02f);
            }

            //Increase the tick elapsed timer
            elapsedTicksPlanted += 1;
        }
    }

    //Public auxiliar methods

    public void setPlanted(boolean planted) {
        //Define the new value for the Entity Data Accessor of this Entity instance...
        this.entityData.set(IS_PLANTED_SYNCED_DATA, planted);
    }

    public boolean isPlanted() {
        //Return the current value from the Entity Data Accessor from this Entity instance...
        return this.entityData.get(IS_PLANTED_SYNCED_DATA);
    }

    public void setNoising(boolean noising){
        //Define the new value for the Entity Data Accessor of this Entity instance...
        this.entityData.set(IS_NOISING_SYNCED_DATA, noising);
    }

    public boolean isNoising(){
        //Return the current value from the Entity Data Accessor from this Entity instance...
        return this.entityData.get(IS_NOISING_SYNCED_DATA);
    }
}