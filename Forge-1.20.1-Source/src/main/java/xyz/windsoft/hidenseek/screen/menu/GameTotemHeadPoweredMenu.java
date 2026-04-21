package xyz.windsoft.hidenseek.screen.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import xyz.windsoft.hidenseek.block.ModBlocks;
import xyz.windsoft.hidenseek.block.entity.GameTotemHeadPoweredBlockEntity;
import xyz.windsoft.hidenseek.screen.ModMenuTypes;
import xyz.windsoft.hidenseek.utils.PlayerScore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
 * This class handle the logic of the Screen "Game Totem Head", and runs in the Client and the Server to manage
 * the logic of the UI to interact with the Player.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class GameTotemHeadPoweredMenu extends AbstractContainerMenu {

    //Private cache variables
    private long lastPlayerScoresRequestTime = 0;
    private List<PlayerScore> lastPlayerScoresRequest = null;

    //Private final variables
    private final Level level;
    private final ContainerData data;

    //Public final variables
    public final GameTotemHeadPoweredBlockEntity blockEntity;

    //Public methods

    public GameTotemHeadPoweredMenu(int pContainerID, Inventory inventory, FriendlyByteBuf extraData){
        //Repass this call to the main constructor of this class
        this(pContainerID, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    public GameTotemHeadPoweredMenu(int pContainerID, Inventory inventory, BlockEntity blockEntity, ContainerData data){
        //Repass this call to the parent of this class
        super(ModMenuTypes.GAME_TOTEM_HEAD_POWERED_MENU.get(), pContainerID);

        //Try to validate the container size of the Block Entity. If is not the expected, throw a error
        checkContainerSize(inventory, 2);

        //Fill the reference variables of this class
        this.blockEntity = ((GameTotemHeadPoweredBlockEntity) blockEntity);
        this.level = inventory.player.level();
        this.data = data;

        //Render special Slots in the UI, that is linked to the Player inventory, to allow the Player to view and edit their inventory in this Block Entity Menu
        AddPlayerInventory(inventory);
        AddPlayerHotbar(inventory);

        //Request the Capability of "Item Handler", and if the Block Entity have this Capability, get the "LazyItemHandler" of the Block Entity...
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            //Render special Slots in the UI, that is linked to the Block Entity container, to allow Player to view and edit the Container in this Block Entity Menu
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 149, 28));
            this.addSlot(new SlotItemHandler(iItemHandler, 1, 149, 76){
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }   //<- Don't allow a Player to put item in the Output Slot...

                @Override
                public boolean mayPickup(Player player) { return true; }     //<- Allow a Player to get the item in the Output Slot...
            });
        });

        //Inform the Container Data of this Block Entity, to the Data Slots of this Menu
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        //Check in the Block and Block Entity, if the access to it is still valid (example: Player still near to the block) and return if the access is still valid
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.GAME_TOTEM_HEAD_POWERED_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        //This method handles the Items Stack quick movements, that is movement of Item Stack while using SHIFT, in-game. This method code was created by the "diesieben07", and not need to be edited.
        //The only variable that you need to modify in this method, is the variable "TE_INVENTORY_SLOT_COUNT" below, that is the inventory size of this Block Entity.
        int TE_INVENTORY_SLOT_COUNT = 2;

        // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
        // must assign a slot number to each of the slots used by the GUI.
        // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
        // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
        //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
        //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
        //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
        int HOTBAR_SLOT_COUNT = 9;
        int PLAYER_INVENTORY_ROW_COUNT = 3;
        int PLAYER_INVENTORY_COLUMN_COUNT = 9;
        int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
        int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
        int VANILLA_FIRST_SLOT_INDEX = 0;
        int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

        //Logic code...
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem())
            return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        //Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            //This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            //This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        //If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(pPlayer, sourceStack);
        return copyOfSourceStack;
    }

    //Private auxiliar methods

    private void AddPlayerInventory(Inventory playerInventory){
        //Define the space from point 0 of the texture, to the point of start of the inventory slots, in the texture, and spacing between slots
        int playerInventoryRow2SpacingPx = 140;   //(Slots 9~17)
        int playerInventoryRow3SpacingPx = 158;   //(Slots 18~26)
        int playerInventoryRow4SpacingPx = 176;   //(Slots 27~36)
        int slotSizePx = 16;
        int slotSeparatorPx = 2;

        //Render all slots of the Player Inventory, row by row
        int row1FirstSlotIndex = 9;
        for (int i = row1FirstSlotIndex; i < (row1FirstSlotIndex + 9); i++)
            this.addSlot(new Slot(playerInventory, i, (8 + ((i - row1FirstSlotIndex) * (slotSizePx + slotSeparatorPx))), playerInventoryRow2SpacingPx));
        int row2FirstSlotIndex = 18;
        for (int i = row2FirstSlotIndex; i < (row2FirstSlotIndex + 9); i++)
            this.addSlot(new Slot(playerInventory, i, (8 + ((i - row2FirstSlotIndex) * (slotSizePx + slotSeparatorPx))), playerInventoryRow3SpacingPx));
        int row3FirstSlotIndex = 27;
        for (int i = row3FirstSlotIndex; i < (row3FirstSlotIndex + 9); i++)
            this.addSlot(new Slot(playerInventory, i, (8 + ((i - row3FirstSlotIndex) * (slotSizePx + slotSeparatorPx))), playerInventoryRow4SpacingPx));
    }

    private void AddPlayerHotbar(Inventory playerInventory){
        //Define the space from point 0 of the texture, to the point of start of the hotbar slots, in the texture, and spacing between slots
        int playerInventoryRow1SpacingPx = 198;   //(Slots 0~8)
        int slotSizePx = 16;
        int slotSeparatorPx = 2;

        //Render all slots of the Player Hotbar
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(playerInventory, i, (8 + i * (slotSizePx + slotSeparatorPx)), playerInventoryRow1SpacingPx));
    }

    private void RequestAndUpdatePlayerScores(){
        //Get the current millis time
        long currentMillisTime = System.currentTimeMillis();

        //If has not passed 5 seconds or more, since the last request, stop here
        if ((currentMillisTime - lastPlayerScoresRequestTime) < 5000)
            return;

        //Get a reference for PlayerScores, from the Game Totem Block Entity
        List<PlayerScore> totemPlayerScoresUnorganized = blockEntity.playersScores;

        //If the request list don't exists, create it
        if (lastPlayerScoresRequest == null)
            lastPlayerScoresRequest = new ArrayList<>();
        //Clear the last request list
        lastPlayerScoresRequest.clear();
        //Fill the List of PlayerScores, with fresh data, organizing the scores from Major to Minor (first, uses the Score as criteria, and if found some equal Score, use the Nickname as criteria)
        lastPlayerScoresRequest = new ArrayList<>(totemPlayerScoresUnorganized.stream().sorted(Comparator.comparingInt(PlayerScore::GetScore).reversed().thenComparing(PlayerScore::GetNickname)).toList());

        //Inform the current time of this request
        lastPlayerScoresRequestTime = currentMillisTime;
    }

    //Public auxiliar methods

    public BlockPos GetCurrentBlockEntityPosition(){
        //Return the current BlockPos of this BlockEntity binded to this Menu
        return blockEntity.getBlockPos();
    }

    public boolean isProgressGoing(){
        //Check the Container Data of this Block Entity, and return it
        return data.get(0) > 0;
    }

    public int GetScaledProgress(){
        //Get information needed to do the calc
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSizePx = 26;

        //Calculate and return the current size of the progress arrow to be rendered in this Menu UI, taking into consideration, the max size of the arrow in UI, current progress and max progress...
        return ((maxProgress != 0 && progress != 0) ? (progress * progressArrowSizePx / maxProgress) : 0);
    }

    public boolean isGameRunning(){
        //Prepare the value to return
        boolean toReturn = false;

        //Get the game running value
        if (this.data.get(2) == 1)
            toReturn = true;

        //Return the value
        return toReturn;
    }

    public boolean isNecessaryTheLightPumpkin(){
        //Return the value that indicates if is necessary the light pumpkin on the Screen
        return blockEntity.useLightPumpkim;
    }

    public PlayerScore GetScoreboardInfoAboutPosition(int position){
        //Prepare value to return
        PlayerScore toReturn = new PlayerScore("", -1);

        //Update the PlayerScores in cache
        RequestAndUpdatePlayerScores();

        //Get the score at positions...
        if (position == 1)
            if (lastPlayerScoresRequest.size() >= 1)
                toReturn = lastPlayerScoresRequest.get(0);
        if (position == 2)
            if (lastPlayerScoresRequest.size() >= 2)
                toReturn = lastPlayerScoresRequest.get(1);
        if (position == 3)
            if (lastPlayerScoresRequest.size() >= 3)
                toReturn = lastPlayerScoresRequest.get(2);

        //Return the value
        return toReturn;
    }

    public void GetScoreboardInfoAndSendTheTop10(){
        //Get the Player reference
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        //Send the header
        localPlayer.displayClientMessage(Component.translatable("gui.hidenseek.game_totem_head_powered_screen.complete_leaderboard_start"), false);
        //Send the top 10
        for (int i = 0; i < 10; i++)
            if (lastPlayerScoresRequest.size() >= (i + 1))
                localPlayer.displayClientMessage(Component.literal(String.valueOf((i + 1)) + ". " + lastPlayerScoresRequest.get(i).GetNickname() + ": " + lastPlayerScoresRequest.get(i).GetScore() + "pts"), false);
        //Send the footer
        localPlayer.displayClientMessage(Component.translatable("gui.hidenseek.game_totem_head_powered_screen.complete_leaderboard_end"), false);
    }
}