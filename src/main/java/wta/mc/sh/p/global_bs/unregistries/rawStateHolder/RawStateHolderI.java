package wta.mc.sh.p.global_bs.unregistries.rawStateHolder;

import net.minecraft.world.level.block.state.StateHolder;

public interface RawStateHolderI {
	boolean isStatesFor(StateHolder<?, ?> holder);
}
