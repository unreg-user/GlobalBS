package wta.mc.sh.p.global_bs.old.mixins.mixin;

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
import wta.mc.sh.p.global_bs.old.mixins.classes.BSDefF;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements FabricBlockState {
	@Override
	public int valueIndex(@NonNull Property<?> property) {
		if (property == BlockStateProperties.FACING) property = BSDefF.FACING_GLOBAL;
		if (property == BlockStateProperties.HORIZONTAL_FACING) property = BSDefF.HORIZONTAL_FACING_GLOBAL;
		for(int i = 0; i < this.propertyKeys.length; ++i) {
			if (this.propertyKeys[i] == property) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public boolean hasProperty(@NonNull Property<?> property) {
		if (property == BlockStateProperties.AXIS) return true;
		return super.hasProperty(property);
	}

	@Override
	public <T extends Comparable<T>, V extends T> @NonNull BlockState setValue(@NonNull Property<T> property, @NonNull V value) {
		if (property == BlockStateProperties.AXIS) return (BlockState) (Object) this;
		return super.setValue(property, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T extends Comparable<T>> T getNullableValue(final @NonNull Property<T> property) {
		if (property == BlockStateProperties.AXIS) return (T) (Object) Direction.Axis.Y;
		return super.getNullableValue(property);
	}

	// костыли
	protected BlockStateMixin(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
		super(owner, propertyKeys, propertyValues);
	}
}
