package wta.mc.sh.p.global_bs.mixins.mixin;

import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import wta.mc.sh.p.global_bs.PropertyUnregistry;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements FabricBlockState {
	@Override
	public int valueIndex(@NonNull Property<?> property) {
		var prop2 = PropertyUnregistry.VALUE_INDEX_REPLACE.get(property);
		if (prop2 != null) property = prop2;
		for(int i = 0; i < this.propertyKeys.length; ++i) {
			if (this.propertyKeys[i] == property) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public boolean hasProperty(@NonNull Property<?> property) {
		if (PropertyUnregistry.ALWAYS_HAS_PROPERTY.contains(property)) return true;
		return super.hasProperty(property);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>, V extends T> @NonNull BlockState setValue(@NonNull Property<T> property, @NonNull V value) {
		var handler = (PropertyUnregistry.UnregisteredValueHandler<T>) PropertyUnregistry.UNREG_VALUE_HANDLERS.get(property);
		if (handler != null) return handler.setValue((BlockState) (Object) this, value);
		if (property == BlockStateProperties.AXIS) return (BlockState) (Object) this;
		return super.setValue(property, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T extends Comparable<T>> T getNullableValue(final @NonNull Property<T> property) {
		var handler = (PropertyUnregistry.UnregisteredValueHandler<T>) PropertyUnregistry.UNREG_VALUE_HANDLERS.get(property);
		if (handler != null) return handler.getNullableValue((BlockState) (Object) this);
		return super.getNullableValue(property);
	}

	// костыли
	protected BlockStateMixin(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
		super(owner, propertyKeys, propertyValues);
	}
}
