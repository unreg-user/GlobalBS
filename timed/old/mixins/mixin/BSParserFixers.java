package wta.mc.sh.p.global_bs.old.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.dispatch.VariantSelector;
import net.minecraft.client.renderer.block.dispatch.multipart.KeyValueCondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.old.mixins.classes.BSDefF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class BSParserFixers {
	@Mixin(KeyValueCondition.class)
	public static class Multipart1ConditionFixer {
		@WrapOperation(
			  method = "lambda$instantiate$0",
			  at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
		private static <O, S extends StateHolder<O, S>> boolean addNullIsIgnoreFix(List<Predicate<S>> instance, Object e, Operation<Boolean> original) {
			return e != null && original.call(instance, e);
		}

		@Redirect(
			  method = "instantiate(Lnet/minecraft/world/level/block/state/StateDefinition;)Ljava/util/function/Predicate;",
			  at = @At(value = "NEW", target = "(I)Ljava/util/ArrayList;"))
		private static <O, S extends StateHolder<O, S>> ArrayList<Predicate<S>> customPropertiesFix(
			  int initialCapacity,
			  @Local(name = "definition", argsOnly = true) StateDefinition<O, S> definition) {
			if (definition.getOwner() instanceof Block) {
				var list = new ArrayList<Predicate<S>>(initialCapacity + 1);
				//noinspection unchecked
				list.add((Predicate<S>) BSDefF.CUSTOM_PROPERTIES_PREDICATE);
				return list;
			}
			return new ArrayList<>(initialCapacity);
		}

		@Inject(
			  method = "instantiate(Lnet/minecraft/world/level/block/state/StateDefinition;Ljava/lang/String;Lnet/minecraft/client/renderer/block/dispatch/multipart/KeyValueCondition$Terms;)Ljava/util/function/Predicate;",
			  at = @At(value = "RETURN"),
			  cancellable = true)
		private static <O, S extends StateHolder<O, S>> void facingPropertyFix(
			  StateDefinition<O, S> definition,
			  String key,
			  KeyValueCondition.Terms valueTest,
			  CallbackInfoReturnable<Predicate<S>> cir,
			  @Local(name = "property") Property<?> property) {
			if (definition.getOwner() instanceof Block) {
				if ((property == null && "facing".equals(key)) || "axis".equals(key)) {
					cir.cancel();
				}
			}
		}
	}

	@Mixin(VariantSelector.class)
	public static class Variant1ConditionFixer {
		/*@WrapOperation(
			  method = "predicate",
			  at = @At(value = "INVOKE", target = "Ljava/lang/String;isEmpty()Z"))
		private static <O, S extends StateHolder<O, S>> boolean facingPropertyFix(
			  String instance,
			  Operation<Boolean> original,
			  @Local(name = "stateDefinition", argsOnly = true) StateDefinition<O, S> stateDefinition,
			  @Local(name = "property") Property<?> property) {
			if (stateDefinition.getOwner() instanceof Block) {
				if ((property == null && "facing".equals(instance)) || "axis".equals(instance)) {
					return true;
				}
			}
			return original.call(instance);
		}*/

		@WrapOperation(
			  method = "predicate",
			  at = @At(value = "INVOKE", target = "Ljava/lang/String;isEmpty()Z"))
		private static <O, S extends StateHolder<O, S>> boolean facingPropertyFix(
			  String instance,
			  Operation<Boolean> original,
			  @Local(name = "stateDefinition", argsOnly = true) StateDefinition<O, S> stateDefinition,
			  @Local(name = "property") Property<?> property) {
			if (stateDefinition.getOwner() instanceof Block) {
				if ((property == null && "facing".equals(instance)) || "axis".equals(instance)) {
					return true;
				}
			}
			return original.call(instance);
		}

		@WrapOperation(
			  method = "predicate",
			  at = @At(value = "NEW", target = "()Ljava/util/HashMap;")
		)
		private static <O, S extends StateHolder<O, S>> HashMap<Property<?>, Comparable<?>> customPropertiesFix(
			  Operation<HashMap<Property<?>, Comparable<?>>> original,
			  @Local(name = "stateDefinition", argsOnly = true) StateDefinition<O, S> stateDefinition){
			var map = original.call();
			if (stateDefinition.getOwner() instanceof Block){
				map.putAll(BSDefF.CUSTOM_PROPERTIES_TO_DEFAULT);
			}
			return map;
		}
	}
}
