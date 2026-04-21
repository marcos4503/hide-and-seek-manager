package xyz.windsoft.hidenseek.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

/*
 * This class do actions when is played ANY Sound on Client.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnClientPlaySoundEvent {

    //Private cache variables
    private Set<ResourceLocation> STEP_SOUNDS_REFERENCE_CACHE = null;

    //Public events

    @SubscribeEvent
    public void onClientPlaySoundEvent(PlaySoundEvent event) {
        //Get informations
        Level clientCurrentLevel = Minecraft.getInstance().level;
        SoundInstance soundThatWillPlay = event.getSound();
        ResourceLocation soundLocationThatWillPlay = soundThatWillPlay.getLocation();
        Vec3 soundWillPlayAt = new Vec3(soundThatWillPlay.getX(), soundThatWillPlay.getY(), soundThatWillPlay.getZ());

        //If have a null input, cancel here
        if (clientCurrentLevel == null || soundThatWillPlay == null || soundLocationThatWillPlay == null)
            return;

        //If was not initialized the Step Sounds cache, initialize it
        if (STEP_SOUNDS_REFERENCE_CACHE == null)
            InitializeStepSounds();

        //If not is a Walk/Run step sound, stop here...
        if (isStepSound(soundLocationThatWillPlay) == false)
            return;

        //Prepare the Entity search hitbox
        AABB searchHitBox = new AABB(soundWillPlayAt.x, soundWillPlayAt.y, soundWillPlayAt.z, soundWillPlayAt.x, soundWillPlayAt.y, soundWillPlayAt.z).inflate(0.5f, 0.5f, 0.5f);
        //Prepare informations about the place where the Sound will play
        final int[] totalExistingPlayers = new int[]{ 0 };
        final int[] totalWalkingPlayers = new int[]{ 0 };
        final int[] totalVisiblePlayers = new int[] { 0 };
        final int[] totalInvisiblePlayers = new int[]{ 0 };
        //Get all Players in the Entity search hitbox and collect the informations (using a consumer for more performance)
        clientCurrentLevel.getEntities(EntityTypeTest.forClass(Player.class), searchHitBox, clientPlayer -> {
            //Increase the counter of existing Players
            totalExistingPlayers[0] += 1;
            //If the current being analyzed Player is walking, increase the counter
            if (isPlayerMoving(clientPlayer) == true)
                totalWalkingPlayers[0] += 1;
            //If the current being analyzed Player is invisible, increase the counter
            if (clientPlayer.isInvisible() == true)
                totalInvisiblePlayers[0] += 1;
            //Continue the search...
            return false;
        });
        //Get all Players in the Entity search hitbox, that are visible
        totalVisiblePlayers[0] = (totalExistingPlayers[0] - totalInvisiblePlayers[0]);

        //Prepare the response if should play this Sound
        boolean shouldPlayThisSound = true;
        //Check if have at least one Player visible at the Sound position, and walking (on scenarios where have two Players ocuppying same block, but only is walking invisible, this check will allow the sound to play. But it's ok)
        if (totalWalkingPlayers[0] >= 1 && totalVisiblePlayers[0] == 0)
            shouldPlayThisSound = false;
        //Additional check for scenarios where the alogrithm thinks that don't have players walking
        if (totalWalkingPlayers[0] == 0 && totalVisiblePlayers[0] == 0)
            shouldPlayThisSound = false;

        //If is not allowed to Play this Sound, stop this
        if (shouldPlayThisSound == false)
            event.setSound(null);
    }

    //Private auxiliar methods

    private void InitializeStepSounds(){
        //If is already initialized, stop here
        if (STEP_SOUNDS_REFERENCE_CACHE != null)
            return;

        //Initialize the cache variable
        STEP_SOUNDS_REFERENCE_CACHE = new HashSet<>();

        //Iterate with all Sounds registered in the game, and put a reference in the Cache, for each Step Sound found....
        for (SoundEvent soundEvent : BuiltInRegistries.SOUND_EVENT)
            if (soundEvent.getLocation().getPath().contains("step") == true)
                STEP_SOUNDS_REFERENCE_CACHE.add(soundEvent.getLocation());
    }

    private boolean isStepSound(ResourceLocation soundToCompare){
        //Prepare the value to return
        boolean toReturn = false;

        //If the Sound Event is equal to someone of the Sound Events in the Set, inform true
        if (soundToCompare != null)
            if (STEP_SOUNDS_REFERENCE_CACHE.contains(soundToCompare) == true)
                toReturn = true;

        //Return the value
        return toReturn;
    }

    private boolean isPlayerMoving(Player clientPlayer) {
        //Prepare the value to return
        boolean toReturn = false;

        //Calculate the difference from the current position and past position of the past frame
        double deltaX = (clientPlayer.getX() - clientPlayer.xo);
        double deltaZ = (clientPlayer.getZ() - clientPlayer.zo);

        //Calculate the distance using square root for more performance
        double horizontalDistanceSq = deltaX * deltaX + deltaZ * deltaZ;

        //If is moving, inform to return true
        if (horizontalDistanceSq >= 0.001f)
            toReturn = true;

        //Return the value
        return toReturn;
    }
}