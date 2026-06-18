package wta.mc.sh.p.global_bs;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import wta.mc.sh.p.global_bs.internal.AllRegCycler;

import java.util.*;
import java.util.function.Predicate;

public class PropertyUnregistry<O, S extends StateHolder<O, S>> {
	private static final HashMap<Class<?>, PropertyUnregistry<?, ?>> UNREG_MAP = new HashMap<>();
	private static final List<FastGetterInfo<?, ?>> FAST_IMPL_UNREG_GETTERS = new ArrayList<>();

	// Internal
	@ApiStatus.Internal
	public final Set<Property<?>> RAW_UNREGISTERED = new HashSet<>();
	@ApiStatus.Internal
	public final List<Property<?>> REGISTERED_FOR_ALL = new ArrayList<>();
	@ApiStatus.Internal
	public Predicate<StateHolder<?, ?>> ALL_PROP_REG_PREDICATE = _ -> true;
	@ApiStatus.Internal
	public Map<Property<?>, Comparable<?>> DEFAULTS_UNREG = new HashMap<>();
	@ApiStatus.Internal
	public Map<Property<?>, Comparable<?>> DEFAULTS_ALWAYS_REG = new HashMap<>();
	/**
	 * Old -> New
	 */
	@ApiStatus.Internal
	public final Map<Property<?>, Property<?>> REPLACES = new IdentityHashMap<>();
	@ApiStatus.Internal
	public final Set<Property<?>> ALWAYS_HAS_PROPERTY = new HashSet<>();
	@ApiStatus.Internal
	public final Map<Property<?>, UnregisteredValueHandler<?>> UNREG_VALUE_HANDLERS = new HashMap<>();

	public static void addUnregistriesFor(Class<?>... parents) {
		for (var clazz : parents) {
			UNREG_MAP.put(clazz, new PropertyUnregistry<>());
		}
	}

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

	// API
	public <T extends Comparable<T>> void addDefaultReg(Property<T> property, T defaultValue) {
		ALL_PROP_REG_PREDICATE = ALL_PROP_REG_PREDICATE.and(state -> state.getValue(property) == defaultValue);
		DEFAULTS_ALWAYS_REG.put(property, defaultValue);
	}

	public <T extends Comparable<T>> void unregister(UnregPropHandler<T> propHandler) {
		var prop = propHandler.self();
		RAW_UNREGISTERED.add(prop);

		var newReplaceValue = propHandler.newReplaceValue;
		if (newReplaceValue != null) REPLACES.put(prop, newReplaceValue);
		var unregValueHandler = propHandler.unregValueHandler;
		if (unregValueHandler != null) UNREG_VALUE_HANDLERS.put(prop, unregValueHandler);
		boolean alwaysHas = propHandler.alwaysHas;
		if (alwaysHas) ALWAYS_HAS_PROPERTY.add(prop);
		var defaultSerValue = propHandler.defaultSerValue;
		if (defaultSerValue != null) DEFAULTS_UNREG.put(prop, defaultSerValue);
	}

	public <T extends Comparable<T>> Property<T> registerForAll(AllRegPropInfo<T> info) {
		var prop = info.property;
		REGISTERED_FOR_ALL.add(prop);

		addDefaultReg(prop, info.defaultSerValue);

		return prop;
	}

	@ApiStatus.Internal
	public AllRegCycler<O, S> getAllRegCycler(S state) {
		return new AllRegCycler<>(REGISTERED_FOR_ALL, state);
	}

	public BlockState getAllRegDefault(BlockState state){
		return
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
	                                                        UnregisteredValueHandler<T> unregValueHandler) {
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
				  new UnregisteredValueHandler.OfRetValue<>(value));
		}
	}

	// Other Handlers
	@SuppressWarnings("unchecked")
	public interface UnregisteredValueHandler<T extends Comparable<T>> {
		<O, S> T getNullableValue(StateHolder<O, S> self);

		<O, S> S setValue(StateHolder<O, S> self, T value);

		record OfRetValue<T extends Comparable<T>>(T value) implements UnregisteredValueHandler<T> {
			@Override
			public <O, S> T getNullableValue(StateHolder<O, S> self) {
				return value;
			}

			@Override
			public <O, S> S setValue(StateHolder<O, S> self, T value) {
				return (S) self;
			}
		}
	}

	// Other
	public record PropUnregAddInfo<O, S extends StateHolder<O, S>>(PropertyUnregistry<O, S> unregistry, Class<O> clazz, Predicate<Object> impl_predicate) {
	}

	private record FastGetterInfo<O, S extends StateHolder<O, S>>(Predicate<Object> predicate, PropertyUnregistry<O, S> unregistry) {
	}

	@ApiStatus.Internal
	public static class UnregistryException extends RuntimeException {
		public UnregistryException(String message) {
			super(message);
		}
	}
}
