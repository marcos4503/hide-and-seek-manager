package xyz.windsoft.hidenseek.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.windsoft.hidenseek.config.Config;
import xyz.windsoft.hidenseek.events.OnRenderGui;
import xyz.windsoft.hidenseek.network.ModPacketHandler;
import xyz.windsoft.hidenseek.network.ServerToClient_GameProgressAdrenalinePacket;

import javax.annotation.Nullable;
import java.util.List;

/*
 * This class creates the custom behavior for the item "Breathing Underwater"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class BreathingUnderwater extends Item {

    //Public methods

    public BreathingUnderwater(Item.Properties pProperties) {
        //Repass the properties to parent class of this class
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderBreathingUnderwaterAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderBreathingUnderwaterCooldown;

        //Add a base description
        tooltip.add(Component.translatable("tooltip.hidenseek.breathing_underwater.desc").withStyle(ChatFormatting.BLUE));

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
            tooltip.add(Component.translatable("tooltip.hidenseek.general.effect_duration").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("5" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.minutes_abr").withStyle(ChatFormatting.DARK_GRAY)));
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
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderBreathingUnderwaterAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderBreathingUnderwaterCooldown;

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
                        //If the Player already have the Underwater Breathing effect, remove it
                        if (serverPlayer.hasEffect(MobEffects.WATER_BREATHING) == true)
                            serverPlayer.removeEffect(MobEffects.WATER_BREATHING);
                        //Apply the Underwater Breathing effect
                        serverPlayer.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 6000, 0, false, false));
                        //Play a sound of the effect being applyied
                        serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.PLAYERS, 1.0f, 1.0f);

                        //Reduce the Adrenaline Points of the Player
                        int newCurrentAdrenalinePoints = (currentAdrenalinePoints - ITEM_ADRENALINE_COST);
                        playerPersistentNbt.putInt("adrenalinePoints", newCurrentAdrenalinePoints);
                        //Send a packet to the Player, to inform new count of Adrenaline Points...
                        ModPacketHandler.SendToPlayer(new ServerToClient_GameProgressAdrenalinePacket(newCurrentAdrenalinePoints), serverPlayer);
                        //Set the new cooldown for the item
                        pPlayer.getCooldowns().addCooldown(this, (ITEM_COOLDOWN_SECONDS * 20));

                        //Return use success for Server
                        return InteractionResultHolder.sidedSuccess(itemStack, false);
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
            return InteractionResultHolder.sidedSuccess(itemStack, true);
        }

        //Consume and cancel the interaction here, and inform success...
        return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide());
    }

    //Public auxiliar methods

    public float GetItemNotReadyState(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        //Prepare the value to return
        float toReturn = 0.0f;

        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderBreathingUnderwaterAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderBreathingUnderwaterCooldown;

        //If the Player have less Adrenaline Cost than the required by the Item, set "not_ready" to TRUE
        if (OnRenderGui.progress_adrenalinePoints < ITEM_ADRENALINE_COST)
            toReturn = 1.0f;

        //Return the value
        return toReturn;
    }
}