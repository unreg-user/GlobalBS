package wta.mc.sh.p.global_bs.mixins.clazzes;

import net.minecraft.world.level.block.state.StateHolder;
import wta.mc.sh.p.global_bs.unregistries.rawStateHolder.RawStateHolder;

import java.util.function.Predicate;

public record PredicateWithStates<O, S extends StateHolder<O, S>>(RawStateHolder rawHolder, Predicate<S> rawPredicate) implements Predicate<S> {
	@Override
	public boolean test(S holder) {
		return rawPredicate.test(holder) && rawHolder.isStatesFor(holder);
	}
}
