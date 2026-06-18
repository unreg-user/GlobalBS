package wta.mc.sh.p.global_bs.internal;

import net.minecraft.util.random.Weighted;

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
}
