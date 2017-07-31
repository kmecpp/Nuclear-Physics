package org.halvors.quantum.common.block.debug.schematic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import org.halvors.quantum.common.QuantumBlocks;
import org.halvors.quantum.common.block.reactor.fusion.BlockElectromagnet;
import org.halvors.quantum.common.utility.transform.vector.Vector3;

import java.util.HashMap;

public class SchematicAccelerator implements ISchematic {
    @Override
    public String getName() {
        return "schematic.accelerator.name";
    }

    @Override
    public HashMap<Vector3, IBlockState> getStructure(EnumFacing direction, int size) {
        HashMap<Vector3, IBlockState> map = new HashMap<>();
        int radius = size;

        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == -radius || x == radius - 1 || z == -radius || z == radius - 1) {
                        map.put(new Vector3(x, y, z), QuantumBlocks.blockElectromagnet.getDefaultState());
                    }
                }
            }
        }

        radius = size - 2;

        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == -radius || x == radius - 1 || z == -radius || z == radius - 1) {
                        map.put(new Vector3(x, y, z), QuantumBlocks.blockElectromagnet.getDefaultState());
                    }
                }
            }
        }

        radius = size - 1;

        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == -radius || x == radius - 1 || z == -radius || z == radius - 1) {
                        if (y == -1 || y == 1) {
                            map.put(new Vector3(x, y, z), QuantumBlocks.blockElectromagnet.getStateFromMeta(BlockElectromagnet.EnumElectromagnet.GLASS.ordinal()));
                        } else {
                            map.put(new Vector3(x, y, z), Blocks.AIR.getDefaultState());
                        }
                    }
                }
            }
        }

        /*
        //Bottom
        map.putAll(getBox(new Vector3(0, 0, 0), QuantumBlocks.blockElectromagnet.block, 1, size));
        map.putAll(getBox(new Vector3(0, 0, 0), QuantumBlocks.blockElectromagnet.block, 0, size - 1));
        map.putAll(getBox(new Vector3(0, 0, 0), QuantumBlocks.blockElectromagnet.block, 0, size + 1));

        //Mid
        map.putAll(getBox(new Vector3(0, 1, 0), Blocks.air, 0, size));
        map.putAll(getBox(new Vector3(0, 1, 0), QuantumBlocks.blockElectromagnet.block, 1, size - 1));
        map.putAll(getBox(new Vector3(0, 1, 0), QuantumBlocks.blockElectromagnet.block, 1, size + 1));

        //Top
        map.putAll(getBox(new Vector3(0, 2, 0), QuantumBlocks.blockElectromagnet.block, 1, size));
        map.putAll(getBox(new Vector3(0, 2, 0), QuantumBlocks.blockElectromagnet.block, 0, size - 1));
        map.putAll(getBox(new Vector3(0, 2, 0), QuantumBlocks.blockElectromagnet.block, 0, size + 1));
        */

        return map;
    }
}