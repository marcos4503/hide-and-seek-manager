package xyz.windsoft.hidenseek.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/*
 * This class helps handling data to be saved/lodade from a ServerLevel, used in
 * Game Manager Logic.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [X] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class GameManagerLevelData extends SavedData {

    //Private static final constants
    private static final String DATA_NAME = "hidenseek_game_status";

    //Private final data to store in the Level
    private boolean isGameRunning = false;
    private int gameTotemHeadPosX = 0;
    private int gameTotemHeadPosY = 0;
    private int gameTotemHeadPosZ = 0;

    //Public methods

    @Override
    public CompoundTag save(CompoundTag levelNbt) {
        //Save this data when the Minecraft is saving the Level NBT to Disk
        levelNbt.putBoolean("isGameRunning", isGameRunning);
        levelNbt.putInt("gameTotemHeadPosX", gameTotemHeadPosX);
        levelNbt.putInt("gameTotemHeadPosY", gameTotemHeadPosY);
        levelNbt.putInt("gameTotemHeadPosZ", gameTotemHeadPosZ);
        //Return the data to be saved
        return levelNbt;
    }

    public void SetGameRunning(boolean running) {
        //Inform that have a game running now
        this.isGameRunning = running;
        this.setDirty();   //<- Warn to Minecraft, that this Data was changed, and need to be saved to Disk
    }

    public void SetGameTotemHeadPos(BlockPos gameTotemHeadPos){
        //Inform the Game Totem Head position
        this.gameTotemHeadPosX = gameTotemHeadPos.getX();
        this.gameTotemHeadPosY = gameTotemHeadPos.getY();
        this.gameTotemHeadPosZ = gameTotemHeadPos.getZ();
        this.setDirty();   //<- Warn to Minecraft, that this Data was changed, and need to be saved to Disk
    }

    public boolean isGameRunning(){
        //Return if the game is running now
        return isGameRunning;
    }

    public BlockPos GetGameTotemHeadPos(){
        //Return the Game Totem Head position
        return new BlockPos(gameTotemHeadPosX, gameTotemHeadPosY, gameTotemHeadPosZ);
    }

    //Public static auxiliar methods

    public static GameManagerLevelData get(ServerLevel level) {
        //Call the load constructor of this class, to load this Level Data, generate a Instance of this Class, and bind it to the Level, to save automatically, when changed, and finally, return the instance of this Class, to be handled.
        return level.getDataStorage().computeIfAbsent(GameManagerLevelData::load, GameManagerLevelData::new, DATA_NAME);
    }

    public static GameManagerLevelData load(CompoundTag nbt) {
        //Load the data when the Minecraft is reading the Level NBT from Disk, generate a Instance of this Class, and return it. Run after the first static constructor, above this!
        GameManagerLevelData data = new GameManagerLevelData();
        data.isGameRunning = nbt.getBoolean("isGameRunning");
        data.gameTotemHeadPosX = nbt.getInt("gameTotemHeadPosX");
        data.gameTotemHeadPosY = nbt.getInt("gameTotemHeadPosY");
        data.gameTotemHeadPosZ = nbt.getInt("gameTotemHeadPosZ");
        //Return the instance generated
        return data;
    }
}