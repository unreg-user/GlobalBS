package wta.mc.sh.p.global_bs.internal.cyclers;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import wta.mc.sh.p.global_bs.mixins.clazzes.StateHolderWithNState;

import java.util.List;

public class AlwaysRegsRawCycler<O, S extends StateHolder<O, S>> implements AlwaysRegsCyclerI<O, S> {
	private final Property<?>[] properties;
	private final List<Comparable<?>>[] possibleValues;
	private final Comparable<?>[] values;
	private final short[] stateList;

	public AlwaysRegsRawCycler(Property<?>[] properties, Comparable<?>[] values, List<Comparable<?>>[] possibleValues) {
		this.properties = properties;
		this.values = values;
		this.possibleValues = possibleValues;
		this.stateList = new short[possibleValues.length];
	}

	@Override
	public StateHolder<O, S> cycle() {
		add(0);
		return new StateHolderWithNState<>(null, properties, values);
	}

	private void add(int index) {
		if (index < possibleValues.length) {
			short newState = (short) (stateList[index] + 1);
			if (newState < possibleValues[index].size()) {
				stateList[index] = newState;
				values[index] = possibleValues[index].get(newState);
			} else {
				stateList[index] = 0;
				values[index] = possibleValues[index].getFirst();
				add(++index);
			}
		}
	}
}

