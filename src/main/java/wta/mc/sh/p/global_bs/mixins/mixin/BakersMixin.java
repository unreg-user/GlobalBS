package wta.mc.sh.p.global_bs.mixins.mixin;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.VariantSelector;
import net.minecraft.client.renderer.block.dispatch.multipart.MultiPartModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wta.mc.sh.p.global_bs.mixins.clazzes.PredicateWithStates;
import wta.mc.sh.p.global_bs.mixins.clazzes.StateHolderWithNState;
import wta.mc.sh.p.global_bs.unregistries.PropUnregistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class BakersMixin {
	@Mixin(BlockStateModelDispatcher.SimpleModelSelectors.class)
	public static class SimpleInstantiateMixin {
		@Shadow
		@Final
		private Map<String, BlockStateModel.Unbaked> models;

		/**
		 * @author UnregUser
		 * @reason add custom always states
		 */
		@Overwrite
		public void instantiate(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source, BiConsumer<BlockState, BlockStateModel.UnbakedRoot> output) {
			var unregistry = PropUnregistries.BLOCK_UNREG;
			var modelHandlers = unregistry.modelHandlers;

			models.forEach(
				  (selectorString, model) -> {
					  try {
						  Predicate<StateHolder<Block, BlockState>> selector = VariantSelector.predicate(stateDefinition, selectorString);

						  for (BlockState state : stateDefinition.getPossibleStates()) {
							  if (selector.test(state)) {
								  var cycler = unregistry.getAlwaysRegsCycler(state);
								  var count = unregistry.alwaysRegsVariantsCount;
								  for (int i = 0; i < count; i++) {
									  var cycledState = cycler.cycle();
									  BlockStateModel.Unbaked hadledUnbaked = model;
									  for (var handler : modelHandlers) {
										  hadledUnbaked = handler.handle(hadledUnbaked, state, cycledState);
									  }
									  output.accept((BlockState) cycledState, hadledUnbaked.asRoot());
								  }
							  }
						  }
					  } catch (Exception var9) {
						  BlockStateModelDispatcher.LOGGER
								.warn("Exception loading blockstate definition: '{}' for variant: '{}': {}", source.get(), selectorString, var9.getMessage());
					  }
				  }
			);
		}
	}

	@Mixin(MultiPartModel.Unbaked.class)
	public static class MultipartUnbakedMixin {
		@ModifyVariable(
			  method = "<init>",
			  at = @At("HEAD"),
			  argsOnly = true, name = "selectors")
		private static List<MultiPartModel.Selector<BlockStateModel.Unbaked>> addAlwaysStates(List<MultiPartModel.Selector<BlockStateModel.Unbaked>> selectors){
			var unregistry = PropUnregistries.BLOCK_UNREG;
			var modelHandlers = unregistry.modelHandlers;

			List<MultiPartModel.Selector<BlockStateModel.Unbaked>> list = new ArrayList<>(selectors.size() * unregistry.alwaysRegsVariantsCount);
			for (var selector : selectors) {
				var condition = selector.condition();
				if (condition instanceof PredicateWithStates<?, ?>) {
					var predicate = (PredicateWithStates<Block, BlockState>) condition;
					var rawPredicate = predicate.rawPredicate();
					var rawState = predicate.rawHolder();
					var owner = predicate.owner();

					var fakeState = new StateHolderWithNState<Block, BlockState>(owner, rawState.getProperties(), rawState.getValues());
					var model = selector.model();

					var cycler = unregistry.getAlwaysRegsRawCycler(rawState, owner);
					var count = unregistry.alwaysRegsVariantsCount;
					for (int i = 0; i < count; i++) {
						var cycledState = cycler.cycle();
						BlockStateModel.Unbaked hadledUnbaked = model;
						for (var handler : modelHandlers) {
							hadledUnbaked = handler.handle(hadledUnbaked, fakeState, cycledState);
						}
						var rawCustomPredicate = cycler.getRaw().rawIsStatesFor();
						list.add(new MultiPartModel.Selector<>(
							  state -> rawPredicate.test(state) && rawCustomPredicate.test(state),
							  hadledUnbaked));
					}
				} else if (!PredicateWithStates.isConst(condition)) {
					BlockStateModelDispatcher.LOGGER.error("NOT PREDICATE!!!");
					throw new IllegalStateException("condition is not PredicateWithStates");
				}
			}
			return list;
		}
	}
}
