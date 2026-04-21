package xyz.windsoft.hidenseek.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.concurrent.atomic.AtomicReference;

/*
 * This class creates the custom behavior for the item "Mod Version Checker"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModVersionCheckerItem extends Item {

    //Public methods

    public ModVersionCheckerItem(Properties pProperties) {
        //Repass the properties to parent class of this class
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        //If is the logical server, stop here
        if (pContext.getLevel().isClientSide() == false)
            return InteractionResult.SUCCESS;

        //Notify the version to Player
        //NotifyTheVersionToPlayer(pContext.getPlayer());

        //Consume and cancel the interaction here, and inform success...
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        //If is the logical server, stop here
        if (pLevel.isClientSide() == false)
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, pPlayer.getItemInHand(pUsedHand));

        //Notify the version to Player
        NotifyTheVersionToPlayer(pPlayer);

        //Consume and cancel the interaction here, and inform success...
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, pPlayer.getItemInHand(pUsedHand));
    }

    //Private auxiliar methods

    private void NotifyTheVersionToPlayer(Player pPlayer){
        //Get the version of the mod
        AtomicReference<String> thisModVersion = new AtomicReference<>("Unknown");
        ModList.get().getModContainerById("hidenseek").ifPresent(modContainer -> {
            //Get details about the mod of the informed ID
            ModInfo modInfo = ((ModInfo) modContainer.getModInfo());
            String modVersion = modInfo.getVersion().toString();
            //Return the version
            thisModVersion.set(modVersion);
        });

        //Show to the Player, the mod version
        pPlayer.displayClientMessage(Component.translatable("clientMessage.hidenseek.mod_version_msg", thisModVersion.get()).withStyle(ChatFormatting.WHITE), false);
    }
}
