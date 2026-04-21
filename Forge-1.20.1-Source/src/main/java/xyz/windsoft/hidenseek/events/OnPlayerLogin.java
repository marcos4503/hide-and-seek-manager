package xyz.windsoft.hidenseek.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import virtuoel.pehkui.api.ScaleTypes;
import xyz.windsoft.hidenseek.utils.GameManagerLogic;

/*
 * This class do actions on Players that logins on World.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnPlayerLogin {

    //Public events

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
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

        //Reset player XP
        serverPlayer.setExperienceLevels(0);
        serverPlayer.setExperiencePoints(0);

        //Reset player stats
        serverPlayer.getInventory().clearContent();
        serverPlayer.setGameMode(GameType.ADVENTURE);
        serverPlayer.setHealth(20.0f);
        serverPlayer.getFoodData().setFoodLevel(20);
        serverPlayer.getFoodData().setSaturation(20.0f);
        serverPlayer.removeAllEffects();
        CompoundTag playerPersistentNbt = serverPlayer.getPersistentData();
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
        ScaleTypes.BASE.getScaleData(serverPlayer).setTargetScale(1.0f);

        //If have a running game, set the Player to Spectator
        if (GameManagerLogic.GetCurrentGameStage() == GameManagerLogic.GameStage.Preparation || GameManagerLogic.GetCurrentGameStage() == GameManagerLogic.GameStage.Count ||
                GameManagerLogic.GetCurrentGameStage() == GameManagerLogic.GameStage.Progress || GameManagerLogic.GetCurrentGameStage() == GameManagerLogic.GameStage.Celebration ||
                GameManagerLogic.GetCurrentGameStage() == GameManagerLogic.GameStage.Finished)
            serverPlayer.setGameMode(GameType.SPECTATOR);
    }
}