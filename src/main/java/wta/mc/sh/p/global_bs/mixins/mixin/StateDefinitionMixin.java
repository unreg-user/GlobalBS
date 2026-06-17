package wta.mc.sh.p.global_bs.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.globalBSPart.GlobalBSPart;
import wta.mc.sh.p.global_bs.PropertyUnregistry;
import wta.mc.sh.p.global_bs.mixins.intefaces.HasUnregetPropsFI;
import wta.mc.sh.p.global_bs.mixins.intefaces.HasUnregistryFI;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"unused"})
@Mixin(StateDefinition.class)
public abstract class StateDefinitionMixin<O, S extends StateHolder<O, S>> implements HasUnregistryFI, HasUnregetPropsFI {
	@Shadow
	@Final
	private O owner;

	@Unique
	private PropertyUnregistry unregistry;

	@Unique
	private Map<String, Property<?>> unregedProps = new HashMap<>();

	@Inject(
		  method = "<init>",
		  at = @At("TAIL"))
	public void initFix(Function<O, S> defaultState, O owner, StateDefinition.Factory<O, S> factory, Map<String, Property<?>> properties, CallbackInfo ci) {
		unregistry = PropertyUnregistry.getForSubclass(owner);
	}

	@Override
	public void global_bs$setUnregistry(PropertyUnregistry unregistry) {
		this.unregistry = unregistry;
	}

	@Override
	public PropertyUnregistry global_bs$getUnregistry() {
		return unregistry;
	}

	@Override
	public Map<String, Property<?>> global_bs$getUnregedProps() {
		return unregedProps;
	}

	@Override
	public void global_bs$setUnregedProps(Map<String, Property<?>> unregedProps) {
		this.unregedProps = unregedProps;
	}

	@Inject(
		  method = "<clinit>",
		  at = @At("TAIL"))
	private static void initUnregs(CallbackInfo ci) {
		GlobalBSPart.init();
	}

	@Mixin(StateHolder.class)
	public abstract static class StateHolderMixin<O, S> implements HasUnregistryFI {
		@Unique
		private PropertyUnregistry unregistry;

		@Inject(
			  method = "<init>",
			  at = @At("TAIL")
		)
		public void initFix(Object owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues, CallbackInfo ci) {
			unregistry = PropertyUnregistry.getForSubclass(owner);
		}

		@ModifyVariable(
			  method = "valueIndex",
			  at = @At("HEAD"),
			  argsOnly = true, name = "property")
		public Property<?> valueIndexFix(Property<?> property) {
			var prop2 = unregistry.REPLACES.get(property);
			if (prop2 != null) return prop2;
			return property;
		}

		@Inject(
			  method = "hasProperty",
			  at = @At("HEAD"),
			  cancellable = true)
		public void hasPropertyFix(Property<?> property, CallbackInfoReturnable<Boolean> cir) {
			if (unregistry.ALWAYS_HAS_PROPERTY.contains(property)) cir.setReturnValue(true);
		}

		@SuppressWarnings("unchecked")
		@Inject(
			  method = "setValue",
			  at = @At("HEAD"),
			  cancellable = true)
		public <T extends Comparable<T>, V extends T> void setValueFix(Property<T> property, V value, CallbackInfoReturnable<S> cir) {
			var handler = (PropertyUnregistry.UnregisteredValueHandler<T>) unregistry.UNREG_VALUE_HANDLERS.get(property);
			if (handler != null) cir.setReturnValue(handler.setValue((StateHolder<O, S>) (Object) this, value));
		}

		@SuppressWarnings("unchecked")
		@Inject(
			  method = "getNullableValue",
			  at = @At("HEAD"),
			  cancellable = true)
		public <T extends Comparable<T>, V extends T> void getNullableValueFix(Property<T> property, CallbackInfoReturnable<T> cir) {
			var handler = (PropertyUnregistry.UnregisteredValueHandler<T>) unregistry.UNREG_VALUE_HANDLERS.get(property);
			if (handler != null) cir.setReturnValue(handler.getNullableValue((StateHolder<O, S>) (Object) this));
		}

		@Override
		public void global_bs$setUnregistry(PropertyUnregistry unregistry) {
			this.unregistry = unregistry;
		}

		@Override
		public PropertyUnregistry global_bs$getUnregistry() {
			return unregistry;
		}

		@Inject(
			  method = "<clinit>",
			  at = @At("TAIL"))
		private static void initUnregs(CallbackInfo ci) {
			GlobalBSPart.init();
		}
	}

	@Mixin(StateDefinition.Builder.class)
	public abstract static class StateDefBuilderMixin<O, S extends StateHolder<O, S>> implements HasUnregistryFI, HasUnregetPropsFI {
		@Shadow
		@Final
		public Map<String, Property<?>> properties;

		@Unique
		private final Map<String, Property<?>> unregedProps = new HashMap<>();

		@Unique
		private PropertyUnregistry unregistry;

		@Inject(
			  method = "<init>",
			  at = @At("TAIL"))
		public void initFix(Object owner, CallbackInfo ci) {
			unregistry = PropertyUnregistry.getForSubclass(owner);
			for (var prop : unregistry.REGISTERED_FOR_ALL) {
				properties.put(prop.getName(), prop);
			}
		}

		@WrapOperation(
			  method = "add",
			  at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
		public Object addFix(Map<String, Property<?>> instance, Object k, Object v, Operation<?> original) {
			var property = (Property<?>) v;
			if (unregistry.RAW_UNREGISTERED.contains(property)) {
				unregedProps.put((String) k, property);
				return null;
			} else if (property == PropertyUnregistry.NilProperty.INSTANCE) {
				throw new NullPointerException("Cannot add NilProperty to properties");
			}
			return original.call(instance, k, v);
		}

		@Inject(
			  method = "create",
			  at = @At("RETURN")
		)
		public void createFix(Function<O, S> defaultState, StateDefinition.Factory<O, S> factory, CallbackInfoReturnable<StateDefinition<O, S>> cir) {
			((HasUnregetPropsFI) cir.getReturnValue()).global_bs$setUnregedProps(unregedProps);
		}

		@Override
		public void global_bs$setUnregistry(PropertyUnregistry unregistry) {
			this.unregistry = unregistry;
		}

		@Override
		public PropertyUnregistry global_bs$getUnregistry() {
			return unregistry;
		}

		@Override
		public Map<String, Property<?>> global_bs$getUnregedProps() {
			return unregedProps;
		}

		@Override
		public void global_bs$setUnregedProps(Map<String, Property<?>> unregedProps) {
			throw new RuntimeException("unregedProps is final");
		}

		static {
			GlobalBSPart.init();
		}
	}
}
