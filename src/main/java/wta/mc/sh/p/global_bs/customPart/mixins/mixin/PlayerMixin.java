package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import wta.mc.sh.p.global_bs.customPart.CustomPart;
import wta.mc.sh.p.global_bs.customPart.mixins.interfaces.PlayerFI;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerFI {
	@Unique
	private Direction placementFacing = CustomPart.FACING_GLOBAL_DEFAULT;
	@Unique
	private Direction placementHorizontalFacing = CustomPart.HORIZONTAL_FACING_GLOBAL_DEFAULT;

	@Override
	public void global_bs$setPlacementFacing(Direction direction) {
		placementFacing = direction;
	}

	@Override
	public void global_bs$setPlacementHorizontalFacing(Direction direction) {
		placementHorizontalFacing = direction;
	}

	@Override
	public Direction global_bs$getPlacementFacing() {
		return placementFacing;
	}

	@Override
	public Direction global_bs$getPlacementHorizontalFacing() {
		return placementHorizontalFacing;
	}
}
