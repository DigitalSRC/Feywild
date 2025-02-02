package com.feywild.feywild.block.trees.seasonal;

import com.feywild.feywild.block.trees.BaseTree;
import com.feywild.feywild.block.trees.FeyLeavesBlock;
import com.feywild.feywild.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.util.data.TagAccess;

public class SummerTree extends BaseTree {

    public SummerTree(ModX mod) {
        super(mod, () -> new FeyLeavesBlock(mod, 20, ModParticles.summerLeafParticle));
    }

    private static BlockState getDecorationBlock(RandomSource random) {
        return switch (random.nextInt(30)) {
            case 0 -> Blocks.OXEYE_DAISY.defaultBlockState();
            case 1 -> Blocks.DANDELION.defaultBlockState();
            case 2 -> Blocks.POPPY.defaultBlockState();
            case 4 -> Blocks.ALLIUM.defaultBlockState();
            case 5 -> Blocks.CORNFLOWER.defaultBlockState();
            default -> Blocks.GRASS.defaultBlockState();
        };
    }

    @Override
    public void decorateSaplingGrowth(ServerLevel level, BlockPos pos, RandomSource random) {
        if (TagAccess.ROOT.has(BlockTags.DIRT, level.getBlockState(pos.below()).getBlock())) {
            level.setBlockAndUpdate(pos, getDecorationBlock(random));
        }
    }
}
