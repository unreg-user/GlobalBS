package wta.mc.sh.p.global_bs.internal;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;

public class AllRegCycler {
	private final List<Property<?>> properties;
	private final short[] sizes;
	private final short[] stateList;
	private BlockState state;

	public AllRegCycler(List<Property<?>> properties, BlockState state) {
		this.state = state;
		this.properties = properties;

		var size = properties.size();
		sizes = new short[size];
		for (int i = 0; i < size; i++) {
			sizes[i] = (short) properties.get(i).getAllValues().count();
		}
		stateList = new short[size];
	}

	public BlockState cycle() {
		add(0);
		return state;
	}

	public int getInterCount() {
		int result = 1;
		for (short size : sizes) {
			result *= size;
		}
		return result;
	}

	public void add(int index) {
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
