package org.halvors.quantum.atomic.common.multiblock;

import net.minecraft.world.World;
import org.halvors.quantum.atomic.common.utility.location.Position;

public interface IMultiBlockStructure<W extends IMultiBlockStructure> extends IMultiBlock {
    World getWorldObject();

    void onMultiBlockChanged();

    Position getPosition();

    MultiBlockHandler<W> getMultiBlock();
}