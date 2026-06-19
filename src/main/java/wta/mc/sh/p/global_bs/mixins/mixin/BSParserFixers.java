package wta.mc.sh.p.global_bs.mixins.mixin;

import com.google.common.base.Splitter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.dispatch.VariantSelector;
import net.minecraft.client.renderer.block.dispatch.multipart.CombinedCondition;
import net.minecraft.client.renderer.block.dispatch.multipart.KeyValueCondition;
import net.minecraft.client.renderer.block.dispatch.multipart.Selector;
import net.minecraft.util.Util;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.internal.MFHelper;
import wta.mc.sh.p.global_bs.mixins.clazzes.PredicateWithStates;
import wta.mc.sh.p.global_bs.mixins.clazzes.StateHolderWith1State;
import wta.mc.sh.p.global_bs.mixins.intefaces.HasUnregetPropsFI;
import wta.mc.sh.p.global_bs.mixins.intefaces.HasUnregistryFI;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@SuppressWarnings({"unused"})
public class BSParserFixers {
	@Mixin(KeyValueCondition.class)
	public static abstract class Multipart1ConditionMixin {

		@Shadow
		@Final
		private Map<String, KeyValueCondition.Terms> tests;

		@Shadow
		private static <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> definition, String key, KeyValueCondition.Terms valueTest) {
			return null;
		}

		/**
		 * @author UnregUser
		 * @reason Adding allRegs Models
		 */
		@SuppressWarnings("unchecked")
		@Overwrite
		public <O, S extends StateHolder<O, S>> Predicate<S> instantiate(final StateDefinition<O, S> definition) {
			var predicates = new ArrayList<Predicate<S>>(this.tests.size());
			AtomicBoolean isFalse = new AtomicBoolean(false);
			tests.forEach((key, valueTest) -> {
				var result = instantiate(definition, key, valueTest);
				if (result == PredicateWithStates.FALSE) {
					isFalse.set(true);
				} else if (result != null) {
					predicates.add(result);
				}
			});
			if (isFalse.get()) return (Predicate<S>) PredicateWithStates.FALSE;
			var unregistry = ((HasUnregistryFI<?, ?>) definition).global_bs$getUnregistry();
			return new PredicateWithStates<>(unregistry.postCustomsInitData.needRawDefaultsValues(), Util.allOf(predicates), definition.getOwner());
		}

		@Inject(
			  method = "<clinit>",
			  at = @At("TAIL")
		)
		private static void clinit(CallbackInfo ci) {
			PropertyUnregistry.initPostCustomsInitData();
		}

		@SuppressWarnings("unchecked")
		@Inject(
			  method = "instantiate(Lnet/minecraft/world/level/block/state/StateDefinition;Ljava/lang/String;Lnet/minecraft/client/renderer/block/dispatch/multipart/KeyValueCondition$Terms;)Ljava/util/function/Predicate;",
			  at = @At("HEAD"),
			  cancellable = true)
		private static <O, S extends StateHolder<O, S>> void unregsPropsFix(
			  StateDefinition<O, S> definition,
			  String key,
			  KeyValueCondition.Terms valueTest,
			  CallbackInfoReturnable<Predicate<S>> cir) {
			var unregetProp = ((HasUnregetPropsFI) definition).global_bs$getUnregedProps().get(key);
			if (unregetProp != null) {
				var unregistry = ((HasUnregistryFI<O, S>) definition).global_bs$getUnregistry();
				Comparable<?> shouldValue = unregistry.defaultsUnregs.get(unregetProp);
				if (!((Predicate<S>) valueTest.instantiate(definition.getOwner(), unregetProp)).test((S) (Object) new StateHolderWith1State<>(definition.getOwner(), unregetProp, shouldValue))) {
					cir.setReturnValue((Predicate<S>) PredicateWithStates.FALSE);
				} else {
					cir.setReturnValue(null);
				}
			}
		}

		@Mixin(KeyValueCondition.Terms.class)
		public static class TermsMixin {
			@Inject(
				  method = "instantiate(Ljava/lang/Object;Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/util/function/Predicate;",
				  at = @At(
						value = "RETURN",
					    ordinal = 0
				  ),
				  cancellable = true)
			private <O, S extends StateHolder<O, S>, T extends Comparable<T>> void falseFix(Object owner, Property<T> property, CallbackInfoReturnable<Predicate<?>> cir){
				cir.setReturnValue(PredicateWithStates.FALSE);
			}

			@Inject(
				  method = "instantiate(Ljava/lang/Object;Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/util/function/Predicate;",
				  at = @At(
						value = "RETURN",
						ordinal = 1
				  ),
				  cancellable = true)
			private <O, S extends StateHolder<O, S>, T extends Comparable<T>> void trueFix(Object owner, Property<T> property, CallbackInfoReturnable<Predicate<?>> cir){
				cir.setReturnValue(PredicateWithStates.TRUE);
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
		@SuppressWarnings("unchecked")
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
								return (Predicate<StateHolder<O, S>>) PredicateWithStates.FALSE;
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

	@Mixin(CombinedCondition.class)
	public static abstract class MultipartNConditionMixin {
		@Redirect(
			  method = "instantiate",
			  at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/dispatch/multipart/CombinedCondition$Operation;apply(Ljava/util/List;)Ljava/util/function/Predicate;")
		)
		public <O, S extends StateHolder<O, S>> Predicate<S> operationFix(CombinedCondition.Operation instance, List<Predicate<S>> predicates){
			return MFHelper.applyOperationToTerms(instance, predicates);
		}
	}

	@Mixin(Selector.class)
	public static class SelectorMixin {
		@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked"})
		@WrapOperation(
			  method = "instantiate",
			  at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;")
		)
		public <O, S extends StateHolder<O, S>> Object instantiateTrueFix(
			  Optional<?> instance,
			  Object other,
			  Operation<Predicate<S>> original,
			  @Local(argsOnly = true, name = "definition") StateDefinition<O, S> definition) {
			return original.call(instance, new PredicateWithStates<>(
				  ((HasUnregistryFI<?, ?>) definition).global_bs$getUnregistry().postCustomsInitData.needRawDefaultsValues(),
				  (Predicate<S>) PredicateWithStates.TRUE,
				  definition.getOwner()));
		}
	}
}
