package xyz.windsoft.hidenseek.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

/*
 * This class creates the custom behavior for the block "Game Totem Pillar"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class GameTotemPillarBlock extends HorizontalDirectionalBlock {

    //Private static final variables
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    //Public methods

    public GameTotemPillarBlock(Properties pProperties) {
        //Repass the properties to parent class of this class
        super(pProperties);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        //Return the default state of placement, for this block. Make it face the Player
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        //Repass to parent class, the shape of model of this block
        return SHAPE;
    }

    //Private methods

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        //Add the property of FACING for the builder
        builder.add(FACING);
    }
}
