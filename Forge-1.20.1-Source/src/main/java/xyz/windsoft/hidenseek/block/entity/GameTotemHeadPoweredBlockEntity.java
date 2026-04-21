package xyz.windsoft.hidenseek.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.windsoft.hidenseek.block.ModBlockEntities;
import xyz.windsoft.hidenseek.block.custom.GameTotemHeadPoweredBlock;
import xyz.windsoft.hidenseek.screen.menu.GameTotemHeadPoweredMenu;
import xyz.windsoft.hidenseek.utils.PlayerScore;

import java.util.ArrayList;
import java.util.List;

/*
 * This class creates the custom behavior for the block entity binded to block "Game Totem Head Powered"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class GameTotemHeadPoweredBlockEntity extends BlockEntity implements MenuProvider {

    //Private final variables
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);

    //Private static final variables
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    //Private variables
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    //Private variables that are automatically synced between Client and Server, by the Container Data of this Block Entity
    private int progress = 0;
    private int maxProgress = 78;
    private int isGameRunning = 0;

    //Protected variables
    protected final ContainerData data;

    //Public variables that are synced betweeen Client and Server, by the "sendBlockUpdated()/getUpdatePacket" called by the Server
    public boolean useLightPumpkim = false;
    public List<PlayerScore> playersScores = null;

    //Public methods

    public GameTotemHeadPoweredBlockEntity(BlockPos pPos, BlockState pBlockState) {
        //Repass the properties to parent class of this class
        super(ModBlockEntities.GAME_TOTEM_HEAD_POWERED_BLOCK_ENTITY.get(), pPos, pBlockState);

        //Register the Container Data for this Block Entity. Container Data is primally used to automatically sync, between Server and Client, items stored in inventory of a Block Entity. Container Data can be used to sync other type of data too.
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                //Get the value of the desired variable
                return switch (pIndex){
                    case 0 -> GameTotemHeadPoweredBlockEntity.this.progress;
                    case 1 -> GameTotemHeadPoweredBlockEntity.this.maxProgress;
                    case 2 -> GameTotemHeadPoweredBlockEntity.this.isGameRunning;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                //Set the desired value in the desired variable
                switch (pIndex){
                    case 0 -> GameTotemHeadPoweredBlockEntity.this.progress = pValue;
                    case 1 -> GameTotemHeadPoweredBlockEntity.this.maxProgress = pValue;
                    case 2 -> GameTotemHeadPoweredBlockEntity.this.isGameRunning = pValue;
                };
            }

            @Override
            public int getCount() {
                //Inform the Container Data size, of 2
                return 3;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        //Inform the name of this block, to display
        return Component.translatable("block.hidenseek.game_totem_head_powered_block");
    }

    @Override
    public void load(CompoundTag pTag) {
        //Repass this call to parent class of this class
        super.load(pTag);

        //Load this block data, when the Game is loading data
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("progress");
        isGameRunning = pTag.getInt("isGameRunning");
        //Load this block data, when the Game is loading data
        useLightPumpkim = pTag.getBoolean("useLightPumpkim");
        ListTag playersScoresRaw = pTag.getList("playersScores", Tag.TAG_COMPOUND);
        if (playersScores == null)
            playersScores = new ArrayList<>();
        playersScores.clear();
        for (int i = 0; i < playersScoresRaw.size(); i++){
            CompoundTag currentEntry = playersScoresRaw.getCompound(i);
            playersScores.add(new PlayerScore(currentEntry.getString("nickname"), currentEntry.getInt("score")));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        //Save this block data, when the Game is saving data
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("progress", progress);
        pTag.putInt("isGameRunning", isGameRunning);
        //Save this block data, when the Game is saving data
        pTag.putBoolean("useLightPumpkim", useLightPumpkim);
        ListTag playersScoresRaw = new ListTag();
        if (playersScores == null)
            playersScores = new ArrayList<>();
        for (int i = 0; i < playersScores.size(); i++){
            CompoundTag currentEntry = new CompoundTag();
            currentEntry.putString("nickname", playersScores.get(i).GetNickname());
            currentEntry.putInt("score", playersScores.get(i).GetScore());
            playersScoresRaw.add(currentEntry);
        }
        pTag.put("playersScores", playersScoresRaw);

        //Repass this call to parent class of this class
        super.saveAdditional(pTag);
    }

    @Override
    public void onLoad() {
        //Repass this call for this parent class
        super.onLoad();

        //Fill the "Lazy Item Handler" of this Block Entity, using the "Item Handler" in this Block Entity
        lazyItemHandler = LazyOptional.of(() -> { return itemHandler; });
    }

    @Override
    public CompoundTag getUpdateTag() {
        //Get the data that was saved by "saveAdditional()" and pack it in a CompoundTag to be sended to all Clients...
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket(){
        //Run as callback, when the Server request data to send the current updated state of this BlockEntity, to all Clients through "sendBlockUpdated()"...

        //Take a "snapshot" of the current state of this Block Entity (using data captured in "getUpdateTag()"), and return it, to be sended to all Clients...
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        //Run as callback, when the Client receive a Packet with data of the updated state of this BlockEntity, sended by the Server...

        //Get the CompoundTag, containing the updated data for this BlockEntity...
        CompoundTag tag = packet.getTag();

        //If the data is null, stop here
        if (tag == null)
            return;

        //Force a new load, to load the data of this BlockEntity, but, using the updated data sended by the Server
        this.load(tag);
    }

    @Override
    public @NotNull<T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side){
        //Inform that this block has the capability of "Item Handler", if requested, and repass the "LazyItemHandler" of this Block Entity...
        if (capability == ForgeCapabilities.ITEM_HANDLER){
            return lazyItemHandler.cast();
        }

        //Repass the call to parent class of this
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        //Repass this call to the parent class of this
        super.invalidateCaps();

        //On invalidate the capabilities, invalidate the "Lazy Item Handler" of this Block Entity too
        lazyItemHandler.invalidate();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        //On request the Menu of this Block Entity, by the "NetworkHook" of this binded Block, create and return the Menu for this Block Entity, for display
        return new GameTotemHeadPoweredMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    //Public auxiliar methods

    public void DropThisBlockInventory(){
        //Prepare a simple inventory to temp hold data about this block inventory
        SimpleContainer tmpInventory = new SimpleContainer(itemHandler.getSlots());
        //Fill the temp inventory with the items of this block inventory
        for (int i = 0; i < itemHandler.getSlots(); i++)
            tmpInventory.setItem(i, itemHandler.getStackInSlot(i));

        //Drop all items of the temp inventory at current position of this block
        Containers.dropContents(this.level, this.worldPosition, tmpInventory);
    }

    public void SetGameAsStarted(Level pLevel, BlockPos pPos, BlockState pState){
        //Inform via Conteiner Data, that game is running!
        isGameRunning = 1;

        //Inform via "sendBlockUpdated()/getUpdatePacket()", to use the light pumpkim
        useLightPumpkim = true;

        //If level is null, stop here
        if (pLevel == null)
            return;
        //If is running on the server...
        if (pLevel.isClientSide() == false){
            //Warn to the server that the block was changed, and need to be saved
            setChanged(pLevel, pPos, pState);
            //Warn to the server, to send a update packet for all Clients that area near to this BlockEntity, to receive the new synced state
            pLevel.sendBlockUpdated(pPos, pState, pState, 3);   //<- Use the flag "3" to ensure that all Players near, receive the update
        }
    }

    public void SetGameAsFinished(Level pLevel, BlockPos pPos, BlockState pState){
        //Inform via Conteiner Data, that game is stopped!
        isGameRunning = 0;

        //Inform via "sendBlockUpdated()/getUpdatePacket()", to NOT use the light pumpkim
        useLightPumpkim = false;

        //If level is null, stop here
        if (pLevel == null)
            return;
        //If is running on the server...
        if (pLevel.isClientSide() == false){
            //Warn to the server that the block was changed, and need to be saved
            setChanged(pLevel, pPos, pState);
            //Warn to the server, to send a update packet for all Clients that area near to this BlockEntity, to receive the new synced state
            pLevel.sendBlockUpdated(pPos, pState, pState, 3);   //<- Use the flag "3" to ensure that all Players near, receive the update
        }
    }

    public void RegisterPlayersNewScores(String[] nicknames, int[] scores, Level pLevel, BlockPos pPos, BlockState pState){
        //Update via "sendBlockUpdated()/getUpdatePacket()", to add or update the Players Scores in registry
        for (int i = 0; i < nicknames.length; i++){
            //Prepare the information if the score was updated
            boolean isScoreUpdated = false;
            //Interact with the Scores already existing...
            for (int x = 0; x < playersScores.size(); x++)
                if (playersScores.get(x).GetNickname().equals(nicknames[i]) == true){
                    //Update the score
                    playersScores.get(x).IncrementScore(scores[i]);
                    //Inform that was updated
                    isScoreUpdated = true;
                }
            //If was not updated, so add a new PlayerScore to the list
            if (isScoreUpdated == false)
                playersScores.add(new PlayerScore(nicknames[i], scores[i]));
        }

        //If is running on the server...
        if (pLevel.isClientSide() == false){
            //Warn to the server that the block was changed, and need to be saved
            setChanged(pLevel, pPos, pState);
            //Warn to the server, to send a update packet for all Clients that area near to this BlockEntity, to receive the new synced state
            pLevel.sendBlockUpdated(pPos, pState, pState, 3);   //<- Use the flag "3" to ensure that all Players near, receive the update
        }
    }

    public void Tick(Level pLevel, BlockPos pPos, BlockState pState){
        //If the game is not running, stop the logic here
        if (isGameRunning == 0) {
            //Reset the progress
            progress = 0;
            //Warn to the server that the block was changed, and need to be saved
            setChanged(pLevel, pPos, pState);
            //Cancel here
            return;
        }

        //Reset the progress if is at max
        if (progress == maxProgress){
            //Reset the progress
            progress = 0;
            //Warn to the server that the block was changed, and need to be saved
            setChanged(pLevel, pPos, pState);
            //Cancel here
            return;
        }

        //Increase the progress
        progress += 1;

        //Fix the progress value
        if (progress > maxProgress)
            progress = maxProgress;

        //Warn to the server that the block was changed, and need to be saved
        setChanged(pLevel, pPos, pState);
    }
}