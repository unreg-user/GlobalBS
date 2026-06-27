package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wta.mc.sh.p.global_bs.customPart.CustomPart;
import wta.mc.sh.p.global_bs.customPart.mixins.clazzes.BlockPosR;

public class LevelsMixins {
	@Mixin(Level.class)
	public static class LevelMixin {
		@ModifyVariable(
			  method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
			  at = @At("HEAD"),
			  argsOnly = true, name = "blockState")
		private BlockState stateByPosFix(
			  BlockState blockState,
			  @Local(argsOnly = true, name = "pos") BlockPos pos) {
			if (pos instanceof BlockPosR posR) {
				var info = posR.getInfo();
				return blockState
					  .setValue(CustomPart.FACING_GLOBAL, info.getDDir())
					  .setValue(CustomPart.HORIZONTAL_FACING_GLOBAL, info.getDHor());
			}
			return blockState;
		}
	}
}
