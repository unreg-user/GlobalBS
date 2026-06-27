package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import wta.mc.sh.p.global_bs.customPart.CustomPart;
import wta.mc.sh.p.global_bs.customPart.mixins.interfaces.PlayerFI;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Shadow
	@Final
	private Block block;

	@WrapOperation(
		  method = "place",
		  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState fixPlacementState(BlockItem instance, BlockPlaceContext context, Operation<BlockState> original){
		var orig = original.call(instance, context);
		if (orig == null) {
			orig = block.defaultBlockState();
		}
		var rawPlayer = context.getPlayer();
		if (rawPlayer == null)
			return orig;
		var player = (PlayerFI) rawPlayer;
		var f = player.global_bs$getPlacementFacing();
		var hf = player.global_bs$getPlacementHorizontalFacing();
		var shouldState = orig
			  .setValue(CustomPart.FACING_GLOBAL, f != null ? f : CustomPart.FACING_GLOBAL_DEFAULT)
			  .setValue(CustomPart.HORIZONTAL_FACING_GLOBAL, hf != null ? hf : CustomPart.HORIZONTAL_FACING_GLOBAL_DEFAULT);
		if (shouldState.canSurvive(context.getLevel(), context.getClickedPos()))
			return shouldState;
		return null;
	}
}
