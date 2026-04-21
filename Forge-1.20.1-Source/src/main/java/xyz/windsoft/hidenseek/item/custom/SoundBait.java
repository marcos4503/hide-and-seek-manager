package xyz.windsoft.hidenseek.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import xyz.windsoft.hidenseek.config.Config;
import xyz.windsoft.hidenseek.entity.custom.SoundBaitEntity;
import xyz.windsoft.hidenseek.events.OnRenderGui;
import xyz.windsoft.hidenseek.network.ModPacketHandler;
import xyz.windsoft.hidenseek.network.ServerToClient_GameProgressAdrenalinePacket;

import javax.annotation.Nullable;
import java.util.List;

/*
 * This class creates the custom behavior for the item "Sound Bait"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class SoundBait extends Item {

    //Public methods

    public SoundBait(Item.Properties pProperties) {
        //Repass the properties to parent class of this class
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderSoundBaitAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderSoundBaitCooldown;

        //Add a base description
        tooltip.add(Component.translatable("tooltip.hidenseek.sound_bait.desc").withStyle(ChatFormatting.BLUE));

        //Show the Cost and Cooldown
        tooltip.add(Component.literal(" "));
        tooltip.add(Component.translatable("tooltip.hidenseek.general.adrenaline_cost").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(ITEM_ADRENALINE_COST + " ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable("tooltip.hidenseek.general.adrenaline_points").withStyle(ChatFormatting.DARK_GRAY)));
        tooltip.add(Component.translatable("tooltip.hidenseek.general.cooldown").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(ITEM_COOLDOWN_SECONDS + " ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable("tooltip.hidenseek.general.seconds_abr").withStyle(ChatFormatting.DARK_GRAY)));

        //If is holding SHIFT...
        if (Screen.hasShiftDown() == true){
            tooltip.add(Component.translatable("tooltip.hidenseek.general.bait_arm_when").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("" + "").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.bait_arm_on").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.bait_when_armed_trigger_after").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("1.5" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.seconds_abr").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.bait_sound_dist").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("~32" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.blocks").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.bait_disappear").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("5" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.seconds_abr").withStyle(ChatFormatting.DARK_GRAY)));
        }
        //If not is holding SHIFT...
        if (Screen.hasShiftDown() == false){
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.shift").withStyle(ChatFormatting.GOLD));
        }

        //Repass this call to the Parent of this class, for the game run the needed base steps
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        //Inform to when this item is being used by "startUsingItem()", should run a animation of Spear cast, before launch
        return UseAnim.SPEAR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderSoundBaitAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderSoundBaitCooldown;

        //Get the ItemStack being used
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        //If is not Client side...
        if (pLevel.isClientSide() == false){
            //Try to get the ServerPlayer data
            ServerPlayer serverPlayer = null;
            if (pPlayer instanceof ServerPlayer serverPlayerInst)
                serverPlayer = serverPlayerInst;

            //If was found a ServerPlayer, continue...
            if (serverPlayer != null){
                //Get Player NBT data
                CompoundTag playerPersistentNbt = serverPlayer.getPersistentData();

                //If found information about Adrenaline Points on the Player...
                if (playerPersistentNbt.contains("adrenalinePoints") == true){
                    //Get the current quantity of Adrenaline Points
                    int currentAdrenalinePoints = playerPersistentNbt.getInt("adrenalinePoints");

                    //If have enough Adrenaline Points...
                    if (currentAdrenalinePoints >= ITEM_ADRENALINE_COST){
                        //If have a Speed effect...
                        if (serverPlayer.hasEffect(MobEffects.MOVEMENT_SPEED) == true){
                            //Get effect info
                            MobEffectInstance mobEffectInstance = serverPlayer.getEffect(MobEffects.MOVEMENT_SPEED);
                            //Save it to Player NBT
                            playerPersistentNbt.putInt("beforeUse_soundBait_speedDurationRemaing", mobEffectInstance.getDuration());
                            playerPersistentNbt.putInt("beforeUse_soundBait_speedAmplifier", mobEffectInstance.getAmplifier());
                            //Remove the effect
                            serverPlayer.removeEffect(MobEffects.MOVEMENT_SPEED);
                        }
                        //Inform that the Player was started using the Sound Bait, casting it before launch...
                        serverPlayer.startUsingItem(pUsedHand);

                        //Return use success for Server
                        return InteractionResultHolder.success(itemStack);
                    }
                }
            }
        }
        //If is Client side...
        if (pLevel.isClientSide() == true){
            //If the item is not Ready, cancel the use on Client...
            if (GetItemNotReadyState(itemStack, null, null, 0) == 1.0f)
                return InteractionResultHolder.fail(itemStack);

            //Return a default state for Client...
            return InteractionResultHolder.consume(itemStack);
        }

        //Consume and cancel the interaction here, and inform success...
        return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        //Inform the max use duration (ticks) of this item. This is basically a infinite use, while the Player hold the MB2. This is a behavior similar to Bow/Trident, for example
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderSoundBaitAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderSoundBaitCooldown;

        //If not is the logical server, stop here
        if (level.isClientSide() == true)
            return;

        //If the entity of this event, is not a Server Player, cancel
        if ((entity instanceof ServerPlayer) == false)
            return;

        //Get the Server Player data
        ServerPlayer serverPlayer = ((ServerPlayer) entity);

        //Get the Power To Launch, obtained using the cast time (item use time) before release the MB2 with item in hand
        float powerToLaunch = 0.0f;
        int useDurationTicks = (this.getUseDuration(stack) - timeLeft);
        float useDurationSeconds = ((float)useDurationTicks / 20.0f);
        float useDurationSecondsSquareExponentialCurve = (useDurationSeconds * useDurationSeconds + useDurationSeconds * 2.0f) / 3.0f; //<- Create a exponential curve that make the charge slow on start and faster when near of end
        powerToLaunch = Math.min(useDurationSecondsSquareExponentialCurve, 1.0f);

        //Prepare the Sound Bait Entity
        SoundBaitEntity soundBaitEntity = new SoundBaitEntity(level, entity);
        soundBaitEntity.addTag("entity_to_clear");
        //Launch the Sound Bait Entity using the Power To Launch
        soundBaitEntity.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0f, (powerToLaunch * 1.5f), 1.0f);
        level.addFreshEntity(soundBaitEntity);

        //Get Player NBT data
        CompoundTag playerPersistentNbt = serverPlayer.getPersistentData();
        //Load the current Adrenaline Points of the Player
        int currentAdrenalinePoints = 0;
        if (playerPersistentNbt.contains("adrenalinePoints") == true)
            currentAdrenalinePoints = playerPersistentNbt.getInt("adrenalinePoints");
        //Reduce the Adrenaline Points of the Player
        int newCurrentAdrenalinePoints = (currentAdrenalinePoints - ITEM_ADRENALINE_COST);
        playerPersistentNbt.putInt("adrenalinePoints", newCurrentAdrenalinePoints);
        //Send a packet to the Player, to inform new count of Adrenaline Points...
        ModPacketHandler.SendToPlayer(new ServerToClient_GameProgressAdrenalinePacket(newCurrentAdrenalinePoints), serverPlayer);
        //Set the new cooldown for the item
        serverPlayer.getCooldowns().addCooldown(this, (ITEM_COOLDOWN_SECONDS * 20));
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        //If not is the logical server, stop here
        if (entity.level().isClientSide() == true)
            return;

        //If the entity of this event, is not a Server Player, cancel
        if ((entity instanceof ServerPlayer) == false)
            return;

        //Get the Server Player data
        ServerPlayer serverPlayer = ((ServerPlayer) entity);

        //Get Player NBT data
        CompoundTag playerPersistentNbt = serverPlayer.getPersistentData();
        //Restore the Speed effect, if have
        if (playerPersistentNbt.contains("beforeUse_soundBait_speedDurationRemaing") == true){
            //Get effect info
            int speedRemaing = playerPersistentNbt.getInt("beforeUse_soundBait_speedDurationRemaing");
            int speedAmplifier = playerPersistentNbt.getInt("beforeUse_soundBait_speedAmplifier");
            //Restore the effect
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, speedRemaing, speedAmplifier, false, false));
            //Delete the data in NBT
            if (playerPersistentNbt.contains("beforeUse_soundBait_speedDurationRemaing") == true)
                playerPersistentNbt.remove("beforeUse_soundBait_speedDurationRemaing");
            if (playerPersistentNbt.contains("beforeUse_soundBait_speedAmplifier") == true)
                playerPersistentNbt.remove("beforeUse_soundBait_speedAmplifier");
        }
    }

    //Public auxiliar methods

    public float GetItemNotReadyState(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed){
        //Prepare the value to return
        float toReturn = 0.0f;

        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderSoundBaitAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderSoundBaitCooldown;

        //If the Player have less Adrenaline Cost than the required by the Item, set "not_ready" to TRUE
        if (OnRenderGui.progress_adrenalinePoints < ITEM_ADRENALINE_COST)
            toReturn = 1.0f;

        //Return the value
        return toReturn;
    }
}