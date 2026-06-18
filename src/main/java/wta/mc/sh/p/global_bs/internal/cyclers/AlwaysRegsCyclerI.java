package wta.mc.sh.p.global_bs.internal.cyclers;

import net.minecraft.world.level.block.state.StateHolder;

public interface AlwaysRegsCyclerI<O, S extends StateHolder<O, S>> {
	StateHolder<O, S> cycle();
}
