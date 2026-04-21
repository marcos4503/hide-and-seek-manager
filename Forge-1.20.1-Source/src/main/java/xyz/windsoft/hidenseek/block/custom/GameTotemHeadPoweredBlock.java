package xyz.windsoft.hidenseek.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import xyz.windsoft.hidenseek.block.ModBlockEntities;
import xyz.windsoft.hidenseek.block.entity.GameTotemHeadPoweredBlockEntity;

/*
 * This class creates the custom behavior for the block "Game Totem Head Powered"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class GameTotemHeadPoweredBlock extends BaseEntityBlock {

    //Public static final variables
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    //Public methods

    public GameTotemHeadPoweredBlock(Properties pProperties){
        //Repass the properties to parent class of this class
        super(pProperties);

        //Register the default state for this block
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
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

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        //Repass to parent class, the render shape type of this block
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        //Create and return a new Block Entity that will be placed and binded to this Block
        return new GameTotemHeadPoweredBlockEntity(pPos, pState);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        //On break the already created Block Entity, binded to this Block, drop their content
        if (pState.getBlock() != pNewState.getBlock())
            if (pLevel.getBlockEntity(pPos) instanceof GameTotemHeadPoweredBlockEntity)
                ((GameTotemHeadPoweredBlockEntity) pLevel.getBlockEntity(pPos)).DropThisBlockInventory();

        //Repass to parent class, the call of this
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        //If is the client side, cancel here
        if (pLevel.isClientSide == true)
            return null;

        //Create and return a Ticker Helper for this Block, that will Tick the binded Block Entity of this Block
        return createTickerHelper(pBlockEntityType, ModBlockEntities.GAME_TOTEM_HEAD_POWERED_BLOCK_ENTITY.get(), ((pLevel1, pPos, pState1, pBlockEntity) -> {
            //Inform the tick method of the binded Block Entity, to be called
            pBlockEntity.Tick(pLevel1, pPos, pState1);
        }));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        //If is the client side, cancel here
        if (pLevel.isClientSide() == true)
            return InteractionResult.sidedSuccess(pLevel.isClientSide());



        //Get the Block Entity binded to this Block
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        //Try to open the screen associated with the Block Entity binded to this Block, calling the "createMenu()" of the Block Entity binded to this Block
        if (blockEntity instanceof GameTotemHeadPoweredBlockEntity){
            NetworkHooks.openScreen((ServerPlayer) pPlayer, ((GameTotemHeadPoweredBlockEntity) blockEntity), pPos);
        }
        else {
            throw new IllegalStateException("Container provider is missing!");
        }

        //Inform that the interaction was successfull
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    //Private methods

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        //Add the property of FACING and LIT for the builder
        builder.add(FACING, LIT);
    }
}
