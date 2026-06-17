package wta.mc.sh.p.global_bs.mixins.intefaces;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public interface HasUnregetPropsFI {
	Map<String, Property<?>> global_bs$getUnregedProps();
	void global_bs$setUnregedProps(Map<String, Property<?>> unregedProps);
}
