package wta.mc.sh.p.global_bs.mixins.mixin;

import com.google.common.base.Splitter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.dispatch.VariantSelector;
import net.minecraft.client.renderer.block.dispatch.multipart.KeyValueCondition;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.mixins.clazzes.PredicateWithStates;
import wta.mc.sh.p.global_bs.unregistries.PropUnregistries;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry;
import wta.mc.sh.p.global_bs.mixins.clazzes.StateHolderWith1State;
import wta.mc.sh.p.global_bs.mixins.intefaces.HasUnregetPropsFI;
import wta.mc.sh.p.global_bs.mixins.intefaces.HasUnregistryFI;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings({"unused"})
public class BSParserFixers {
	@Mixin(KeyValueCondition.class)
	public static class Multipart1ConditionFixer {
		@WrapOperation(
			  method = "lambda$instantiate$0",
			  at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
		private static <O, S extends StateHolder<O, S>> boolean addNullIsIgnoreFix(List<Predicate<S>> instance, Object e, Operation<Boolean> original) {
			return e != null && original.call(instance, e);
		}

		@WrapOperation(
			  method = "instantiate(Lnet/minecraft/world/level/block/state/StateDefinition;)Ljava/util/function/Predicate;",
			  at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;allOf(Ljava/util/List;)Ljava/util/function/Predicate;"))
		private <O, S extends StateHolder<O, S>> Predicate<S> instantiate(
			  List<? extends Predicate<? super S>> conditions,
			  Operation<Predicate<S>> original,
			  @Local(name = "definition", argsOnly = true) StateDefinition<O, S> definition

		) {
			var unregistry = ((HasUnregistryFI<?, ?>) definition).global_bs$getUnregistry();
			return new PredicateWithStates<>(unregistry.postCustomsInitData.needRawDefaultsValues().flat(), original.call(conditions));
		}

		@Inject(
			  method = "<clinit>",
			  at = @At("TAIL")
		)
		private static void clinit(CallbackInfo ci){
			PropertyUnregistry.initPostCustomsInitData();
		}

		@SuppressWarnings("unchecked")
		@Inject(
			  method = "instantiate(Lnet/minecraft/world/level/block/state/StateDefinition;Ljava/lang/String;Lnet/minecraft/client/renderer/block/dispatch/multipart/KeyValueCondition$Terms;)Ljava/util/function/Predicate;",
			  at = @At(value = "RETURN"),
			  cancellable = true)
		private static <O, S extends StateHolder<O, S>> void facingPropertyFix(
			  StateDefinition<O, S> definition,
			  String key,
			  KeyValueCondition.Terms valueTest,
			  CallbackInfoReturnable<Predicate<S>> cir) {
			if (((HasUnregetPropsFI) definition).global_bs$getUnregedProps().containsKey(key)) {
				var unregetProp = ((HasUnregetPropsFI) definition).global_bs$getUnregedProps().get(key);
				if (unregetProp != null) {
					var unregistry = ((HasUnregistryFI<?, ?>) definition).global_bs$getUnregistry();
					Comparable<?> shouldValue = unregistry.defaultsUnregs.get(unregetProp);
					if (!cir.getReturnValue().test((S) (Object) new StateHolderWith1State<>(definition.getOwner(), unregetProp, shouldValue)))
						throw new IllegalStateException(PropertyUnregistry.NilProperty.NIL_PROPERTY_USENAME);
				}
				cir.cancel();
			}
		}
	}

	@Mixin(VariantSelector.class)
	public static abstract class Variant1ConditionFixer {
		@Shadow
		@Final
		private static Splitter COMMA_SPLITTER;

		@Shadow
		@Final
		private static Splitter EQUAL_SPLITTER;

		@Shadow
		@Nullable
		private static <T extends Comparable<T>> T getValueHelper(Property<T> property, String next) {
			return null;
		}

		/**
		 * @author UnregUser
		 * @reason adding GlobalBS
		 */
		@Overwrite
		public static <O, S extends StateHolder<O, S>> Predicate<StateHolder<O, S>> predicate(final StateDefinition<O, S> stateDefinition, final String properties) {
			var unregedProps = ((HasUnregetPropsFI) stateDefinition).global_bs$getUnregedProps();
			var unregistry = ((HasUnregistryFI<?, ?>) stateDefinition).global_bs$getUnregistry();

			var map = new HashMap<>(unregistry.defaultsAlwaysRegs);

			for (String keyValue : COMMA_SPLITTER.split(properties)) {
				Iterator<String> iterator = EQUAL_SPLITTER.split(keyValue).iterator();
				if (iterator.hasNext()) {
					String propertyName = iterator.next();

					var unregetProp = unregedProps.get(propertyName);
					Comparable<?> shouldValue;
					if (unregetProp != null) {
						shouldValue = unregistry.defaultsUnregs.get(unregetProp);
					} else {
						shouldValue = null;
					}

					Property<?> property = stateDefinition.getProperty(propertyName);
					if (iterator.hasNext()) {
						String propertyValue = iterator.next();
						if (shouldValue != null) {
							if (getValueHelper(unregetProp, propertyValue) != shouldValue)
								throw new IllegalStateException(PropertyUnregistry.NilProperty.NIL_PROPERTY_USENAME);
						} else if (property != null) {
							Comparable<?> value = getValueHelper(property, propertyValue);
							if (value == null) {
								throw new RuntimeException("Unknown value: '" + propertyValue + "' for blockstate property: '" + propertyName + "' " + property.getPossibleValues());
							}

							map.put(property, value);
						}
					} else if (!propertyName.isEmpty()) {
						throw new RuntimeException("Unknown blockstate property: '" + propertyName + "'");
					}
				}
			}

			return input -> {
				for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
					if (!Objects.equals(input.getValue(entry.getKey()), entry.getValue())) {
						return false;
					}
				}

				return true;
			};
		}
	}
}
