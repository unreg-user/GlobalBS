package wta.mc.sh.p.global_bs.unregistries.rawStateHolder;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wta.mc.sh.p.global_bs.mixins.clazzes.PredicateWithStates;

import java.util.List;
import java.util.function.Predicate;

public class RawStateHolder implements RawStateHolderI {
	final @NotNull Property<?> @NotNull [] properties;
	final @Nullable Comparable<?> @Nullable [] values;
	@Nullable List<Comparable<?>> @Nullable [] partialPossibleValues;

	public RawStateHolder(@NotNull Property<?> @NotNull [] properties, @Nullable Comparable<?> @Nullable [] values, @Nullable List<Comparable<?>> @Nullable [] partialPossibleValues) {
		this.properties = properties;
		this.values = values;
		this.partialPossibleValues = partialPossibleValues;
	}

	public RawStateHolder(@NotNull Property<?>[] properties, @Nullable Comparable<?>[] values) {
		this(properties, values, null);
	}

	public boolean isStatesFor(StateHolder<?, ?> holder) {
		if (values == null) return true;
		var size = properties.length;
		for (int i = 0; i < size; i++) {
			if (holder.getValue(properties[i]) != values[i]) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public Predicate<StateHolder<?, ?>> rawIsStatesFor() {
		Predicate<StateHolder<?, ?>> predicate = (Predicate<StateHolder<?,?>>) PredicateWithStates.TRUE;
		if (values == null) return predicate;

		var size = properties.length;
		for (int i = 0; i < size; i++) {
			var pred = predicate;
			var prop = properties[i];
			var val = values[i];
			predicate = holder -> holder.getValue(prop) == val && pred.test(holder);
		}
		return predicate;
	}

	public RawStateHolder copy() {
		return new RawStateHolder(
			  properties.clone(),
			  values != null ? values.clone() : null,
			  partialPossibleValues != null ? partialPossibleValues: null
		);
	}

	@SuppressWarnings("unchecked")
	public List<Comparable<?>>[] computePossibleValues() {
		var size = properties.length;
		if (partialPossibleValues == null) {
			partialPossibleValues = (List<Comparable<?>>[]) new List<?>[size];
			for (int i = 0; i < size; i++) {
				partialPossibleValues[i] = (List<Comparable<?>>) properties[i].getPossibleValues();
			}
		} else if (partialPossibleValues.length < size) {
			var newPartial = (List<Comparable<?>>[]) new List<?>[size];
			System.arraycopy(partialPossibleValues, 0, newPartial, 0, partialPossibleValues.length);
			for (int i = partialPossibleValues.length; i < newPartial.length; i++) {
				newPartial[i] = (List<Comparable<?>>) properties[i].getPossibleValues();
			}
			partialPossibleValues = newPartial;
		}
		return partialPossibleValues;
	}

	public Property<?>[] getProperties() {
		return properties;
	}

	public Comparable<?>[] getValues() {
		return values;
	}
}
