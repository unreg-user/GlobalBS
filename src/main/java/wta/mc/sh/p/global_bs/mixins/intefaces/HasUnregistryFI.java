package wta.mc.sh.p.global_bs.mixins.intefaces;

import wta.mc.sh.p.global_bs.PropertyUnregistry;

public interface HasUnregistryFI {
	void global_bs$setUnregistry(PropertyUnregistry unregistry);
	PropertyUnregistry global_bs$getUnregistry();
}
