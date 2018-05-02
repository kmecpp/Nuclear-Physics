package org.halvors.nuclearphysics.common.world;

import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.halvors.nuclearphysics.common.ConfigurationManager.General;
import org.halvors.nuclearphysics.common.block.states.BlockStateRadioactive;
import org.halvors.nuclearphysics.common.block.states.BlockStateRadioactive.EnumRadioactive;
import org.halvors.nuclearphysics.common.init.ModBlocks;

import java.util.Random;

public class WorldGeneratorOre implements IWorldGenerator {
    @Override
    public void generate(final Random random, final int chunkX, final int chunkZ, final World world, final IChunkGenerator chunkGenerator, final IChunkProvider chunkProvider) {
        if (!(chunkGenerator instanceof ChunkGeneratorHell) && !(chunkGenerator instanceof ChunkGeneratorEnd)) {
            if (General.enableOreRegeneration) {
                for (int i = 0; i < General.uraniumPerChunk; i++) {
                    final BlockPos pos = new BlockPos(chunkX * 16 + random.nextInt(16), random.nextInt(25), (chunkZ * 16) + random.nextInt(16));
                    new WorldGenMinable(ModBlocks.blockRadioactive.getDefaultState().withProperty(BlockStateRadioactive.TYPE, EnumRadioactive.URANIUM_ORE), 3, BlockMatcher.forBlock(Blocks.STONE)).generate(world, random, pos);
                }
            }
        }
    }
}
