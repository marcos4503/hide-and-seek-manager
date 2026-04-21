package xyz.windsoft.hidenseek.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.block.custom.GameTotemHeadDeprecatedBlock;
import xyz.windsoft.hidenseek.block.custom.GameTotemHeadPoweredBlock;
import xyz.windsoft.hidenseek.block.custom.GameTotemPillarBlock;
import xyz.windsoft.hidenseek.block.custom.GameTotemSideBlock;
import xyz.windsoft.hidenseek.item.ModItems;

import java.util.function.Supplier;

/*
 * This class is responsible by the registering of blocks of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModBlocks {

    //Public static final variables
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);

    //Public static variables
    public static RegistryObject<Block> GAME_TOTEM_HEAD_DEPRECATED_BLOCK = null;
    public static RegistryObject<Block> GAME_TOTEM_HEAD_POWERED_BLOCK = null;
    public static RegistryObject<Block> GAME_TOTEM_BODY_BLOCK = null;
    public static RegistryObject<Block> GAME_TOTEM_SIDE_BLOCK = null;
    public static RegistryObject<Block> GAME_TOTEM_PILLAR_BLOCK = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for block of this mod in the event bus of Forge
        BLOCKS.register(eventBus);

        //Register the "Game Totem Head Deprecated" block...
        GAME_TOTEM_HEAD_DEPRECATED_BLOCK = RegisterBlock("game_totem_head_deprecated_block", () -> {
            //Set up the block properties and return it for register
            BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .sound(SoundType.AMETHYST);
            return new GameTotemHeadDeprecatedBlock(props);
        });
        //Register the "Game Totem Head Powered" block...
        GAME_TOTEM_HEAD_POWERED_BLOCK = RegisterBlock("game_totem_head_powered_block", () -> {
            //Set up the block properties and return it for register
            BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .sound(SoundType.AMETHYST);
            return new GameTotemHeadPoweredBlock(props);
        });
        //Register the "Game Totem Body" block...
        GAME_TOTEM_BODY_BLOCK = RegisterBlock("game_totem_body_block", () -> {
            //Set up the block properties and return it for register
            BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .sound(SoundType.WOOD);
            return new Block(props);
        });
        //Register the "Game Totem Side" block...
        GAME_TOTEM_SIDE_BLOCK = RegisterBlock("game_totem_side_block", () -> {
            //Set up the block properties and return it for register
            BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .sound(SoundType.WOOD);
            return new GameTotemSideBlock(props);
        });
        //Register the "Game Totem Pillar" block...
        GAME_TOTEM_PILLAR_BLOCK = RegisterBlock("game_totem_pillar_block", () -> {
            //Set up the block properties and return it for register
            BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .noOcclusion()
                    .sound(SoundType.WOOD);
            return new GameTotemPillarBlock(props);
        });
    }

    //Private static auxiliar methods

    private static <T extends Block>RegistryObject<T> RegisterBlock(String name, Supplier<T> block){
        //Register the block and prepare to return it
        RegistryObject<T> toReturn = BLOCKS.register(name, block);

        //Register a item for the block too
        RegisterBlockItem(name, toReturn);

        //Return the block registered, with a block item registered too
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Item> RegisterBlockItem(String name, RegistryObject<T> block){
        //Return a registered item for the block, based on a already registered block
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
