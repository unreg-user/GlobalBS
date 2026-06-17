package wta.mc.sh.p.global_bs.unregistries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import wta.mc.sh.p.global_bs.PropertyUnregistry;

public class PropUnregistries {
	public static final BlockPropertyUnreg BLOCK_UNREG;
	public static final PropertyUnregistry FLUID_UNREG;

	public static void init(){}

	static {
		PropertyUnregistry.addUnregistriesFor(
			  new PropertyUnregistry.PropUnregAddInfo<>(
					BLOCK_UNREG = new BlockPropertyUnreg(),
					Block.class,
					x -> x instanceof Block
			  ),
			  new PropertyUnregistry.PropUnregAddInfo<>(
					FLUID_UNREG = new PropertyUnregistry(),
					Fluid.class,
					x -> x instanceof Fluid
			  )
		);
	}
}
