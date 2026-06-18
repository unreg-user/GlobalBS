package wta.mc.sh.p.global_bs.internal.cyclers;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;

public class AllRegCycler<O, S extends StateHolder<O, S>> implements AllRegCyclerI<O, S> {
	private final List<Property<?>> properties;
	private final short[] sizes;
	private final short[] stateList;
	private S state;

	public AllRegCycler(List<Property<?>> properties, S state) {
		this.state = state;
		this.properties = properties;

		var size = properties.size();
		sizes = new short[size];
		for (int i = 0; i < size; i++) {
			sizes[i] = (short) properties.get(i).getAllValues().count();
		}
		stateList = new short[size];
	}

	@Override
	public StateHolder<O, S> cycle() {
		add(0);
		return state;
	}

	private void add(int index) {
		if (index < sizes.length) {
			short newState = (short) (stateList[index] + 1);
			state = state.cycle(properties.get(index));
			if (newState < sizes[index]) {
				stateList[index] = newState;
			} else {
				stateList[index] = 0;
				add(++index);
			}
		}
	}
}
