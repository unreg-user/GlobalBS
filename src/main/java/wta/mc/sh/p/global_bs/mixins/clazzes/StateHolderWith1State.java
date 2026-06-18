package wta.mc.sh.p.global_bs.mixins.clazzes;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unchecked")
public class StateHolderWith1State<O, S> extends StateHolder<O, S> {
	private final Property<?> property;

	public StateHolderWith1State(O owner, Property<?> property, Comparable<?> value) {
		super(owner, new Property<?>[]{property}, new Comparable<?>[]{value});
		this.property = property;
	}

	@Override
	public <T2 extends Comparable<T2>> @NonNull T2 getValue(@NonNull Property<T2> property) {
		return (T2) super.getValue(this.property);
	}
}
