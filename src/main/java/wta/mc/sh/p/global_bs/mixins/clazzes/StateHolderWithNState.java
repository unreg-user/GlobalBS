package wta.mc.sh.p.global_bs.mixins.clazzes;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public class StateHolderWithNState<O, S> extends StateHolder<O, S> {
	public StateHolderWithNState(O owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
		super(owner, propertyKeys, propertyValues);
	}

	public StateHolderWithNState(O owner, Map<Property<?>, Comparable<?>> states) {
		var count = states.size();
		var propertyKeys = new Property<?>[count];
		var propertyValues = new Comparable<?>[count];
		var i = 0;
		for (var j : states.entrySet()) {
			propertyKeys[i] = j.getKey();
			propertyValues[i] = j.getValue();
			i++;
		}
		this(owner, propertyKeys, propertyValues);
	}
}