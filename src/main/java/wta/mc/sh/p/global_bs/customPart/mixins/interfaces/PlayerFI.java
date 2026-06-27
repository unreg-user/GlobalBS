package wta.mc.sh.p.global_bs.customPart.mixins.interfaces;

import net.minecraft.core.Direction;

public interface PlayerFI {
	void global_bs$setPlacementFacing(Direction direction);
	void global_bs$setPlacementHorizontalFacing(Direction direction);

	Direction global_bs$getPlacementFacing();
	Direction global_bs$getPlacementHorizontalFacing();
}
