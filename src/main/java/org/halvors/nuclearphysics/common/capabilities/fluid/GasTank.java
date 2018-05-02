package org.halvors.nuclearphysics.common.capabilities.fluid;

import net.minecraftforge.fluids.FluidStack;

public class GasTank extends LiquidTank {
    public GasTank(final int capacity) {
        super(capacity);
    }

    @Override
    public int fill(final FluidStack resource, final boolean doFill) {
        if (resource.getFluid().isGaseous()) {
            return super.fill(resource, doFill);
        }

        return 0;
    }

    // TODO: Check if this is really needed in 1.7.10.
    /*
    @Nullable
    @Override
    public FluidStack drain(final FluidStack resource, final boolean doDrain) {
        if (resource.getFluid().isGaseous()) {
            return super.drain(resource, doDrain);
        }

        return null;
    }
    */
}
