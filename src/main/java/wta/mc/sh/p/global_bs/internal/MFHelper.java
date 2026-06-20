package wta.mc.sh.p.global_bs.internal;

import net.minecraft.client.renderer.block.dispatch.multipart.CombinedCondition;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.level.block.state.StateHolder;
import wta.mc.sh.p.global_bs.mixins.clazzes.PredicateWithStates;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry;

import java.util.List;
import java.util.function.Predicate;

public interface MFHelper {
	static <T> Weighted<T> withValue(Weighted<T> weighted, T value){
		return new Weighted<>(value, weighted.weight());
	}

	static <T> void combineArraysTo(T[] left, T[] right, T[] result) {
		System.arraycopy(left, 0, result, 0, left.length);
		System.arraycopy(right, 0, result, left.length, right.length);
	}

	static <T> void combineArraysTo(T[] left, int leftSize, T[] right, int rightSize, T[] result) {
		if (left != null) {
			System.arraycopy(left, 0, result, 0, leftSize);
		}
		if (right != null) {
			System.arraycopy(right, 0, result, leftSize, rightSize);
		}
	}

	@SuppressWarnings("unchecked")
	static <O, S extends StateHolder<O, S>> Predicate<S> applyOperationToTerms(CombinedCondition.Operation instance, List<Predicate<S>> terms) {
		if (instance == CombinedCondition.Operation.AND) {
			Predicate<S> predicate = (Predicate<S>) PredicateWithStates.TRUE;
			PredicateWithStates<O, S> firstWithStates = null;
			for (Predicate<S> term : terms) {
				if (term instanceof PredicateWithStates<?, ?>) {
					var termWithStates = (PredicateWithStates<O, S>) term;
					if (firstWithStates == null) firstWithStates = termWithStates;
					var j = predicate;
					var k = termWithStates.rawPredicate();
					predicate = v -> k.test(v) && j.test(v);
				} else if (term == PredicateWithStates.FALSE) {
					return (Predicate<S>) PredicateWithStates.FALSE;
				} else if (term != PredicateWithStates.TRUE) {
					throw new PropertyUnregistry.UnregistryException("NOT PREDICATE!");
				}
			}
			if (firstWithStates == null) return (Predicate<S>) PredicateWithStates.TRUE;
			return new PredicateWithStates<>(firstWithStates.rawHolder(), predicate, firstWithStates.owner());
		} else if (instance == CombinedCondition.Operation.OR) {
			Predicate<S> predicate = (Predicate<S>) PredicateWithStates.FALSE;
			PredicateWithStates<O, S> firstWithStates = null;
			for (Predicate<S> term : terms) {
				if (term instanceof PredicateWithStates<?, ?>) {
					var termWithStates = (PredicateWithStates<O, S>) term;
					if (firstWithStates == null) firstWithStates = termWithStates;
					var j = predicate;
					var k = termWithStates.rawPredicate();
					predicate = v -> k.test(v) || j.test(v);
				} else if (term == PredicateWithStates.TRUE) {
					return (Predicate<S>) PredicateWithStates.TRUE;
				} else if (term != PredicateWithStates.FALSE) {
					throw new PropertyUnregistry.UnregistryException("NOT PREDICATE!");
				}
			}
			if (firstWithStates == null) return (Predicate<S>) PredicateWithStates.FALSE;
			return new PredicateWithStates<>(firstWithStates.rawHolder(), predicate, firstWithStates.owner());
		} else {
			throw new PropertyUnregistry.UnregistryException("Not and or or...");
		}
	}
}
