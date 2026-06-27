package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wta.mc.sh.p.global_bs.customPart.mixins.clazzes.BlockPosR;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
	@WrapOperation(
		  method = "useOn",
		  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/UseOnContext;getClickedPos()Lnet/minecraft/core/BlockPos;")
	)
	private BlockPos blockPosFix(UseOnContext instance, Operation<BlockPos> original){
		var orig = original.call(instance);
		return new BlockPosR(orig, instance.getLevel().getBlockState(orig));
	}
}
