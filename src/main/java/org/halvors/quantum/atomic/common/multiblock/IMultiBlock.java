package org.halvors.quantum.atomic.common.multiblock;

import org.halvors.quantum.atomic.common.utility.location.Position;

/** Interface to be applied to tile entity blocks that occupies more than one block space. Useful for
 * large machines.
 *
 * @author Calclavia */
public interface IMultiBlock {
    /** @return An array of Vector3 containing the multiblock relative coordinates to be constructed. */
    Position[] getMultiBlockVectors();
}