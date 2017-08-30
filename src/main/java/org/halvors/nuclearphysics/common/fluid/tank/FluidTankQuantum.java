package org.halvors.nuclearphysics.common.fluid.tank;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.halvors.nuclearphysics.common.network.PacketHandler;

import javax.annotation.Nullable;
import java.util.List;

public class FluidTankQuantum extends FluidTank {
    public FluidTankQuantum(int capacity) {
        super(capacity);
    }

    public FluidTankQuantum(@Nullable FluidStack fluidStack, int capacity) {
        super(fluidStack, capacity);
    }

    public FluidTankQuantum(Fluid fluid, int amount, int capacity) {
        super(fluid, amount, capacity);
    }

    public void handlePacketData(ByteBuf dataStream) {
        if (dataStream.readBoolean()) {
            setFluid(FluidStack.loadFluidStackFromNBT(PacketHandler.readNBT(dataStream)));
        }
    }

    public List<Object> getPacketData(List<Object> objects) {
        if (fluid != null) {
            objects.add(true);

            NBTTagCompound compoundInputTank = new NBTTagCompound();
            fluid.writeToNBT(compoundInputTank);
            objects.add(compoundInputTank);
        } else {
            objects.add(false);
        }

        return objects;
    }
}