package wta.mc.sh.p.global_bs.unregistries.rawStateHolder;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wta.mc.sh.p.global_bs.internal.MFHelper;

import java.util.List;

public final class SeparatedRawStateHolder implements RawStateHolderI {
	private final @NotNull RawStateHolder base;
	private final @Nullable RawStateHolder custom;
	private @Nullable RawStateHolder flat;

	public SeparatedRawStateHolder(@NotNull RawStateHolder base, @Nullable RawStateHolder custom) {
		this.base = base;
		this.custom = custom;
	}

	@Override
	public boolean isStatesFor(StateHolder<?, ?> holder) {
		return base.isStatesFor(holder) && (custom == null || custom.isStatesFor(holder));
	}

	public RawStateHolder flat() {
		return flat != null ? flat : (flat = computeFlat());
	}

	@SuppressWarnings("unchecked")
	private RawStateHolder computeFlat() {
		if (custom == null) return base;
		var baseSize = base.properties.length;
		var customSize = custom.properties.length;
		var combineSize = baseSize + customSize;

		var baseProps = base.properties;
		var customProps = custom.properties;
		var flatProps = new Property<?>[combineSize];
		MFHelper.combineArraysTo(baseProps, baseSize, customProps, customSize, flatProps);

		var baseValues = base.values;
		var customValues = custom.values;
		var flatValues = new Comparable<?>[combineSize];
		MFHelper.combineArraysTo(baseValues, baseSize, customValues, customSize, flatValues);

		var basePartialV = base.computePossibleValues();
		var customPartialV = custom.partialPossibleValues;
		var flatPartialV = (List<Comparable<?>>[]) new List<?>[combineSize];
		MFHelper.combineArraysTo(basePartialV, baseSize, customPartialV, customSize, flatPartialV);

		return new RawStateHolder(flatProps, flatValues, flatPartialV);
	}

	public SeparatedRawStateHolder copy() {
		return new SeparatedRawStateHolder(base.copy(), custom != null ? custom.copy() : null);
	}

	public @NotNull RawStateHolder getBase() {
		return base;
	}

	public @Nullable RawStateHolder getCustom() {
		return custom;
	}

}
