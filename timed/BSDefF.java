package wta.mc.sh.p.global_bs.mixins.clazzes;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.NonNull;
import wta.mc.sh.p.global_bs.GlobalBSPart;
import wta.mc.sh.p.global_bs.PropertyUnregistry;

import java.util.Map;
import java.util.function.Function;

public class BSDefF extends StateDefinition<Block, BlockState> {
	public BSDefF(Function<Block, BlockState> defaultState, Block owner, Factory<Block, BlockState> factory, Map<String, Property<?>> properties) {
		super(defaultState, owner, factory, properties);
	}

	public static class BuilderF extends StateDefinition.Builder<Block, BlockState> {
		public BuilderF(Block owner) {
			super(owner);
			for (var i : PropertyUnregistry.REGISTERED_FOR_ALL) {
				var prop = i.property();
				properties.put(prop.getName(), prop);
			}
		}

		@Override
		public StateDefinition.@NonNull Builder<Block, BlockState> add(Property<?>... properties) {
			for (Property<?> property : properties) {
				if (PropertyUnregistry.RAW_UNREGISTERED.contains(property)) {
					continue;
				}
				this.validateProperty(property);
				this.properties.put(property.getName(), property);
			}

			return this;
		}

		@Override
		public @NonNull StateDefinition<Block, BlockState> create(final @NonNull Function<Block, BlockState> defaultState, final StateDefinition.@NonNull Factory<Block, BlockState> factory) {
			return new BSDefF(defaultState, this.owner, factory, this.properties);
		}
	}

	static {
		GlobalBSPart.init();
	}
}
