package wta.mc.sh.p.global_bs.internal;

import net.minecraft.util.random.Weighted;

public interface MFHelper {
	static <T> Weighted<T> withValue(Weighted<T> weighted, T value){
		return new Weighted<>(value, weighted.weight());
	}
}
