package wta.mc.sh.p.global_bs.mixins.intefaces;

import net.minecraft.world.level.block.state.StateHolder;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry;

public interface HasUnregistryFI<O, S extends StateHolder<O, S>> {
	void global_bs$setUnregistry(PropertyUnregistry<O, S> unregistry);
	PropertyUnregistry<O, S> global_bs$getUnregistry();
}
