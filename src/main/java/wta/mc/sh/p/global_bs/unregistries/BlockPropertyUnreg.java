package wta.mc.sh.p.global_bs.unregistries;

import net.fabricmc.fabric.impl.client.model.loading.CompositeBlockStateModelImpl;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.WeightedVariants;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import wta.mc.sh.p.global_bs.PropertyUnregistry;
import wta.mc.sh.p.global_bs.internal.MFHelper;

import java.util.ArrayList;
import java.util.List;

public class BlockPropertyUnreg extends PropertyUnregistry {
	@ApiStatus.Internal
	public final List<BSModelHandler> MODEL_HANDLERS = new ArrayList<>();

	public void registerModelHandler(BSModelHandler handler) {
		MODEL_HANDLERS.add(handler);
	}

	@FunctionalInterface
	public interface BSModelHandler {
		BlockStateModel.Unbaked handle(BlockStateModel.Unbaked source, BlockState sourceState, BlockState newState);

		@FunctionalInterface
		interface VariantHandler {
			Variant handle(Variant variant, BlockState sourceState, BlockState newState);
		}

		static BSModelHandler fromVariant(VariantHandler handler) {
			return (source, sourceState, newState) -> InterfacePrivate.fromVariantFunc(handler, source, sourceState, newState);
		}
	}

	private interface InterfacePrivate {
		@SuppressWarnings("UnstableApiUsage")
		static BlockStateModel.Unbaked fromVariantFunc(BSModelHandler.VariantHandler handler, BlockStateModel.Unbaked source, BlockState sourceState, BlockState newState) {
			switch (source) {
				case SingleVariant.Unbaked(Variant variant) -> {
					return new SingleVariant.Unbaked(handler.handle(variant, sourceState, newState));
				}
				case WeightedVariants.Unbaked(WeightedList<BlockStateModel.Unbaked> entries) -> {
					List<Weighted<BlockStateModel.Unbaked>> newList = new ArrayList<>();
					for (var i : entries.unwrap()) {
						newList.add(MFHelper.withValue(i, fromVariantFunc(handler, i.value(), sourceState, newState)));
					}
					return new WeightedVariants.Unbaked(WeightedList.of(newList));
				}
				case CompositeBlockStateModelImpl.Unbaked unbaked ->
					  throw new UnregistryException("CompositeBlockStateModelImpl in fromVariant BSModelHandler");
				default -> throw new UnregistryException(source.getClass() + " in fromVariant BSModelHandler");
			}
		}
	}
}
