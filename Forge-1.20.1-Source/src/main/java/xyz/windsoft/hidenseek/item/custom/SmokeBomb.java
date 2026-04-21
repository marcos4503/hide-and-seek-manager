package xyz.windsoft.hidenseek.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import xyz.windsoft.hidenseek.config.Config;
import xyz.windsoft.hidenseek.effect.ModEffects;
import xyz.windsoft.hidenseek.events.OnRenderGui;
import xyz.windsoft.hidenseek.network.ModPacketHandler;
import xyz.windsoft.hidenseek.network.ServerToClient_GameProgressAdrenalinePacket;

import javax.annotation.Nullable;
import java.util.List;

/*
 * This class creates the custom behavior for the item "Smoke Bomb"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class SmokeBomb extends Item {

    //Public methods

    public SmokeBomb(Item.Properties pProperties) {
        //Repass the properties to parent class of this class
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderSmokeBombAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderSmokeBombCooldown;

        //Add a base description
        tooltip.add(Component.translatable("tooltip.hidenseek.smoke_bomb.desc").withStyle(ChatFormatting.BLUE));

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
            tooltip.add(Component.translatable("tooltip.hidenseek.general.smoke_bomb_sphere_radius").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("3.5" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.blocks").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.aoe_radius").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("4" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.blocks").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.aoe_duration").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("5" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.seconds_abr").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.blind_duration").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("4" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.seconds_abr").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.invisibility_duration").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("5" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.seconds_abr").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.fall_arrest_height").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("3.5" + " ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("tooltip.hidenseek.general.blocks").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.translatable("tooltip.hidenseek.general.fall_arrest_duration").withStyle(ChatFormatting.GRAY)
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
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderSmokeBombAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderSmokeBombCooldown;

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
                        //Add the effect of invisibility for the Player
                        serverPlayer.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false));
                        //Add the effect of fall arrest for the Player
                        serverPlayer.addEffect(new MobEffectInstance(ModEffects.FALL_ARREST.get(), 100, 0, false, false));
                        //Force the Hidder to select the item 0 on Hotbar. This will help to hide they hand
                        serverPlayer.getInventory().selected = 0;
                        serverPlayer.connection.send(new ClientboundSetCarriedItemPacket(0));
                        //Do a Spherical burst of Smoke
                        RandomSource random = serverPlayer.level().getRandom();
                        int density = 150;
                        float radius = 3.5f;
                        //Spawn all Smoke particles...
                        for (int i = 0; i < density; i++) {
                            //Generate a random degree
                            double u = random.nextDouble();
                            double v = random.nextDouble();
                            double theta = 2.0f * Math.PI * u;
                            //Generate a random distance
                            double phi = Math.acos(2.0f * v - 1.0f);
                            double r = radius * Math.pow(random.nextDouble(), 1.0f/3.0f);
                            //Get XYZ coordinates
                            double x = r * Math.sin(phi) * Math.cos(theta);
                            double y = r * Math.cos(phi); // Y original
                            double z = r * Math.sin(phi) * Math.sin(theta);
                            //Do filthering of Y, to allow only particles in Y positive
                            double finalY = (y < 0.0f) ? -y * 0.5f : y;
                            //Spawn the current interaction particle
                            ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, serverPlayer.getX() + 0.5f + x, serverPlayer.getY() + 0.2f + finalY, serverPlayer.getZ() + 0.5f + z, 3, 0.3f, 0.3f, 0.3f, 0.0f);
                        }
                        //Spawn a Area Effect Cloud, to take care of the Blindness Effect and basic particles
                        AreaEffectCloud smokeCloud = EntityType.AREA_EFFECT_CLOUD.create(serverPlayer.level());
                        if (smokeCloud != null) {
                            smokeCloud.setOwner(serverPlayer);
                            smokeCloud.setPos(serverPlayer.getX() + 0.5, serverPlayer.getY(), serverPlayer.getZ() + 0.5);
                            smokeCloud.setDuration(100); //<- 5 seconds
                            smokeCloud.setRadius(3.5f);  //<- Ray of Blindness
                            smokeCloud.setWaitTime(0);   //<- Insta apply the effect, when spawn the area
                            smokeCloud.setParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE);
                            //smokeCloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
                            smokeCloud.addTag("entity_to_clear");
                            serverPlayer.level().addFreshEntity(smokeCloud);
                        }
                        //Spawn a explosion particle
                        ((ServerLevel) serverPlayer.level()).sendParticles(ParticleTypes.EXPLOSION, serverPlayer.getX() + 0.5f, serverPlayer.getY() + 0.5f, serverPlayer.getZ() + 0.5f, 1, 0, 0, 0, 0);
                        //Apply Blindness to all Players in the radius of the Smoke Bomb, except this Player
                        List<Player> nearPlayers = serverPlayer.level().getEntitiesOfClass(Player.class, new AABB(smokeCloud.position(), smokeCloud.position()).inflate(4.0d));
                        for (int i = 0; i < nearPlayers.size(); i++)
                            if (nearPlayers.get(i) != null)
                                if (nearPlayers.get(i) instanceof ServerPlayer nearServerPlayer)
                                    if (nearServerPlayer.getUUID().equals(serverPlayer.getUUID()) == false)
                                        nearServerPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, false));
                        //Play a gunpowder explosion sound
                        serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundSource.PLAYERS, 2.0f, 1.1f);

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

            //Return a default state for Server...
            return InteractionResultHolder.fail(itemStack);
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

    public float GetItemNotReadyState(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed){
        //Prepare the value to return
        float toReturn = 0.0f;

        //Get item configurations
        int ITEM_ADRENALINE_COST = Config.hidderSmokeBombAdrenalineCost;
        int ITEM_COOLDOWN_SECONDS = Config.hidderSmokeBombCooldown;

        //If the Player have less Adrenaline Cost than the required by the Item, set "not_ready" to TRUE
        if (OnRenderGui.progress_adrenalinePoints < ITEM_ADRENALINE_COST)
            toReturn = 1.0f;

        //Return the value
        return toReturn;
    }
}