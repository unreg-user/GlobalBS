package wta.mc.sh.p.global_bs.unregistries;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import wta.mc.sh.p.global_bs.internal.cyclers.AlwaysRegsCycler;
import wta.mc.sh.p.global_bs.internal.cyclers.AlwaysRegsRawCycler;
import wta.mc.sh.p.global_bs.unregistries.rawStateHolder.RawStateHolder;

import java.util.*;
import java.util.function.Predicate;

public class PropertyUnregistry<O, S extends StateHolder<O, S>> {
	private static final HashMap<Class<?>, PropertyUnregistry<?, ?>> UNREG_MAP = new HashMap<>();
	private static final List<FastGetterInfo<?, ?>> FAST_IMPL_UNREG_GETTERS = new ArrayList<>();

	// Internal
	@ApiStatus.Internal
	public final Set<Property<?>> rawUnregs = new HashSet<>();
	@ApiStatus.Internal
	public final List<Property<?>> alwaysRegs = new ArrayList<>();
	//@ApiStatus.Internal
	//public Predicate<StateHolder<?, ?>> ALLWAYS_REGS_DEFAULT_PREDICATE = _ -> true;
	@ApiStatus.Internal
	public Map<Property<?>, Comparable<?>> defaultsUnregs = new HashMap<>();
	@ApiStatus.Internal
	public Map<Property<?>, Comparable<?>> defaultsAlwaysRegs = new HashMap<>();
	/**
	 * Old -> New
	 */
	@ApiStatus.Internal
	public final Map<Property<?>, Property<?>> replaces = new IdentityHashMap<>();
	@ApiStatus.Internal
	public final Set<Property<?>> alwaysHasProperty = new HashSet<>();
	@ApiStatus.Internal
	public final Map<Property<?>, UnregisteredGettingValueHandler<?>> unregsGettingValueHandlers = new HashMap<>();
	@ApiStatus.Internal
	public int alwaysRegsVariantsCount = 1;

	@ApiStatus.Internal
	public PostInitCustomsData postCustomsInitData;

	public static void addUnregistriesFor(PropUnregAddInfo<?, ?>... parentsInfo) {
		for (var info : parentsInfo) {
			var unregistry = info.unregistry;
			UNREG_MAP.put(info.clazz, unregistry);
			FAST_IMPL_UNREG_GETTERS.add(new FastGetterInfo<>(info.impl_predicate, unregistry));
		}
	}

	public static PropertyUnregistry<?, ?> getForSubclass(Object state) {
		for (var i : FAST_IMPL_UNREG_GETTERS) {
			if (i.predicate.test(state)) {
				return i.unregistry;
			}
		}
		return getForSubclass(state.getClass());
	}

	public static PropertyUnregistry<?, ?> getForSubclass(Class<?> stateClazz) {
		for (var i : UNREG_MAP.entrySet()) {
			if (i.getKey().isAssignableFrom(stateClazz)) {
				return i.getValue();
			}
		}
		throw new UnregistryException("No parent class for " + stateClazz + " in UNREG_MAP");
	}

	public static PropertyUnregistry<?, ?> getOrCreateFor(Class<?> stateClazz) {
		return UNREG_MAP.computeIfAbsent(stateClazz, _ -> new PropertyUnregistry<>());
	}

	public static PropertyUnregistry<?, ?> getFor(Class<?> stateClazz) {
		return Objects.requireNonNull(UNREG_MAP.get(stateClazz));
	}

	@ApiStatus.Internal
	public static void initPostCustomsInitData() {
		for (var i : UNREG_MAP.entrySet()) {
			i.getValue().initPostCustomsInitDataLocal();
		}
	}

	// API
	public <T extends Comparable<T>> void addDefaultRegs(Property<T> property, T defaultValue) {
		//ALLWAYS_REGS_DEFAULT_PREDICATE = ALLWAYS_REGS_DEFAULT_PREDICATE.and(state -> state.getValue(property) == defaultValue);
		defaultsAlwaysRegs.put(property, defaultValue);
	}

	public <T extends Comparable<T>> void unreg(UnregPropHandler<T> propHandler) {
		var prop = propHandler.self();
		rawUnregs.add(prop);

		var newReplaceValue = propHandler.newReplaceValue;
		if (newReplaceValue != null) replaces.put(prop, newReplaceValue);
		var unregValueHandler = propHandler.unregValueHandler;
		if (unregValueHandler != null) unregsGettingValueHandlers.put(prop, unregValueHandler);
		boolean alwaysHas = propHandler.alwaysHas;
		if (alwaysHas) alwaysHasProperty.add(prop);
		var defaultSerValue = propHandler.defaultSerValue;
		if (defaultSerValue != null) defaultsUnregs.put(prop, defaultSerValue);
	}

	public <T extends Comparable<T>> Property<T> alwaysReg(AllRegPropInfo<T> info) {
		var prop = info.property;
		alwaysRegs.add(prop);

		addDefaultRegs(prop, info.defaultSerValue);
		alwaysRegsVariantsCount *= (int) prop.getAllValues().count();

		return prop;
	}

	public AlwaysRegsCycler<O, S> getAlwaysRegsCycler(S state) {
		return new AlwaysRegsCycler<>(alwaysRegs, state);
	}

	public AlwaysRegsRawCycler<O, S> getAlwaysRegsRawCycler(RawStateHolder rawHolder, O owner) {
		return new AlwaysRegsRawCycler<>(rawHolder.getProperties(), rawHolder.getValues(), rawHolder.computePossibleValues(), owner);
	}

	@ApiStatus.Internal
	public void initPostCustomsInitDataLocal() {
		RawStateHolder needRawDefaultsValues;
		{
			var properties = alwaysRegs.toArray(count -> new Property<?>[count]);
			@SuppressWarnings("unchecked")
			var possibleValues = (List<Comparable<?>>[]) alwaysRegs.stream().map(Property::getPossibleValues).toArray(count -> new List<?>[count]);
			var defaults = new Comparable<?>[properties.length];
			for (int i = 0; i < properties.length; i++) {
				defaults[i] = defaultsAlwaysRegs.get(properties[i]);
			}
			needRawDefaultsValues = new RawStateHolder(properties, defaults, possibleValues);
		}
		postCustomsInitData = new PostInitCustomsData(needRawDefaultsValues);
	}

	// Nil Property
	@ApiStatus.Internal
	public static class NilProperty extends EnumProperty<NilProperty.Nil> {
		public static final String NIL_PROPERTY_USENAME = "=NIL[*]=";
		public static final NilProperty INSTANCE = new NilProperty();

		private NilProperty() {
			super("nil", Nil.class, List.of(Nil.INSTANCE));
		}

		private enum Nil implements StringRepresentable {
			INSTANCE;

			@Override
			public @NonNull String getSerializedName() {
				return "nil";
			}
		}
	}

	// Registry For All
	public record AllRegPropInfo<T extends Comparable<T>>(Property<T> property, T defaultSerValue) {
	}

	// Unregister
	public record UnregPropHandler<T extends Comparable<T>>(Property<T> self, Property<T> newReplaceValue,
	                                                        T defaultSerValue, boolean alwaysHas,
	                                                        UnregisteredGettingValueHandler<T> unregValueHandler) {
		@SuppressWarnings("unused")
		public static <T extends Comparable<T>> UnregPropHandler<T> ofReplace(Property<T> self, Property<T> old, T value) {
			return new UnregPropHandler<>(
				  self,
				  old,
				  value,
				  false,
				  null);
		}

		public static <T extends Comparable<T>> UnregPropHandler<T> ofMiddlepart(Property<T> self, T value) {
			return new UnregPropHandler<>(
				  self,
				  null,
				  value,
				  true,
				  new UnregisteredGettingValueHandler.OfRetValue<>(value));
		}

		public static <T extends Comparable<T>> UnregPropHandler<T> ofMiddlepartReplace(Property<T> self, T value, UnregisteredValueHandlerSetter<T> setter) {
			return new UnregPropHandler<>(
				  self,
				  null,
				  value,
				  true,
				  new UnregisteredGettingValueHandler.OfRetValueWithSetter<>(value, setter));
		}
	}

	// Other Handlers
	public interface UnregisteredValueHandlerGetter<T extends Comparable<T>> {
		<O, S> T getNullableValue(StateHolder<O, S> self);
	}

	public interface UnregisteredValueHandlerSetter<T extends Comparable<T>> {
		<O, S> S setValue(StateHolder<O, S> self, T value);
	}

	@SuppressWarnings("unchecked")
	public interface UnregisteredGettingValueHandler<T extends Comparable<T>> extends UnregisteredValueHandlerGetter<T>, UnregisteredValueHandlerSetter<T> {

		record OfRetValue<T extends Comparable<T>>(T value) implements UnregisteredGettingValueHandler<T> {
			@Override
			public <O, S> T getNullableValue(StateHolder<O, S> self) {
				return value;
			}

			@Override
			public <O, S> S setValue(StateHolder<O, S> self, T value) {
				return (S) self;
			}
		}

		record OfRetValueWithSetter<T extends Comparable<T>>(T value, UnregisteredValueHandlerSetter<T> setter) implements UnregisteredGettingValueHandler<T> {
			@Override
			public <O, S> T getNullableValue(StateHolder<O, S> self) {
				return value;
			}

			@Override
			public <O, S> S setValue(StateHolder<O, S> self, T value) {
				return setter.setValue(self, value);
			}
		}
	}

	// Other
	public record PropUnregAddInfo<O, S extends StateHolder<O, S>>(PropertyUnregistry<O, S> unregistry, Class<O> clazz, Predicate<Object> impl_predicate) {
	}

	private record FastGetterInfo<O, S extends StateHolder<O, S>>(Predicate<Object> predicate, PropertyUnregistry<O, S> unregistry) {
	}

	public record PostInitCustomsData(RawStateHolder needRawDefaultsValues) {
	}

	@ApiStatus.Internal
	public static class UnregistryException extends RuntimeException {
		public UnregistryException(String message) {
			super(message);
		}
	}
}
