package wta.mc.sh.p.global_bs.customPart;

import java.util.Map;
import java.util.stream.Collectors;

public interface MFHelperC {
	static <K, V> Map<V, K> reverseFinal(Map<K, V> map) {
		return Map.copyOf(map.entrySet().stream().collect(Collectors.toMap(
			  Map.Entry::getValue,
			  Map.Entry::getKey
		)));
	}
}
