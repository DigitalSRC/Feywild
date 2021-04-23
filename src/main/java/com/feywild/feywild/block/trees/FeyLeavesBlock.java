package com.feywild.feywild.block.trees;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import java.util.Random;

public class FeyLeavesBlock extends Block implements net.minecraftforge.common.IForgeShearable
{
    private static final int maxDistance = 15;
    public static final IntegerProperty DISTANCE = IntegerProperty.create("more_distance", 0, maxDistance);
    public static final BooleanProperty PERSISTENT = BooleanProperty.create("persistent_leaves");

    public FeyLeavesBlock() {
        super(Block.Properties.create(Material.LEAVES)
                .hardnessAndResistance(0.2F)
                .tickRandomly()
                .sound(SoundType.PLANT)
                .harvestTool(ToolType.HOE)
                .notSolid()
                .setAllowsSpawn((s, r, p, t) -> false)
                .setSuffocates((s, r, p) -> false)
                .setBlocksVision((s, r, p) -> false));

        this.setDefaultState(this.stateContainer.getBaseState()
                .with(DISTANCE, 0)
                .with(PERSISTENT, false));
    }

    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return VoxelShapes.empty();
    }



    public boolean ticksRandomly(BlockState state) {
        return state.get(DISTANCE) == maxDistance && !state.get(PERSISTENT);
    }


    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {

        updateDistance(state,worldIn,pos);

        if (!state.get(PERSISTENT) && state.get(DISTANCE) == maxDistance) {
            spawnDrops(state, worldIn, pos);
            worldIn.removeBlock(pos, false);
        }
    }

    //This causes the decay //what does flags do?
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        worldIn.setBlockState(pos, updateDistance(state, worldIn, pos), 3);
    }

    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1;
    }


    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        int i = getDistance(facingState) + 1;
        if (i != 1 || stateIn.get(DISTANCE) != i) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }

        return stateIn;
    }

    private static BlockState updateDistance(BlockState state, IWorld worldIn, BlockPos pos) {

        int i = maxDistance;

        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(Direction direction : Direction.values()) {
            blockpos$mutable.setAndMove(pos, direction);
            i = Math.min(i, getDistance(worldIn.getBlockState(blockpos$mutable)) + 1);
            if (i == 1) {
                break;
            }
        }

        return state.with(DISTANCE, Integer.valueOf(i));
    }


    private static int getDistance(BlockState neighbor) {
        if (BlockTags.LOGS.contains(neighbor.getBlock())) {
            return 0;
        } else {
            //is instance of FeyLeaves Block
            return neighbor.getBlock() instanceof FeyLeavesBlock ? neighbor.get(DISTANCE) : maxDistance;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (worldIn.isRainingAt(pos.up())) {
            if (rand.nextInt(15) == 1) {
                BlockPos blockpos = pos.down();
                BlockState blockstate = worldIn.getBlockState(blockpos);
                if (!blockstate.isSolid() || !blockstate.isSolidSide(worldIn, blockpos, Direction.UP)) {
                    double d0 = (double)pos.getX() + rand.nextDouble();
                    double d1 = (double)pos.getY() - 0.05D;
                    double d2 = (double)pos.getZ() + rand.nextDouble();
                    worldIn.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        if(worldIn.isAirBlock(pos.down()) && rand.nextInt(30) == 1) {
            double windStrength = 5 + Math.cos((double) worldIn.getGameTime() / 2000) * 2;
            double windX = Math.cos((double) worldIn.getGameTime() / 1200) * windStrength;
            double windZ = Math.sin((double) worldIn.getGameTime() / 1000) * windStrength;

            worldIn.addParticle(new BlockParticleData(ParticleTypes.BLOCK, stateIn), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, windX, -1.0, windZ);

        }
    }


    @Override
    protected void fillStateContainer(StateContainer.Builder builder) {

        builder.add(DISTANCE, PERSISTENT);

    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return updateDistance(this.getDefaultState().with(PERSISTENT, Boolean.valueOf(true)), context.getWorld(), context.getPos());
    }



}
