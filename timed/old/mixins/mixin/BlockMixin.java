package wta.mc.sh.p.global_bs.old.mixins.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wta.mc.sh.p.global_bs.old.mixins.classes.BSDefF;

@Mixin(Block.class)
public class BlockMixin {
	@Redirect(
		  method = "<init>",
		  at = @At(value = "NEW", target = "(Ljava/lang/Object;)Lnet/minecraft/world/level/block/state/StateDefinition$Builder;")
	)
	public StateDefinition.Builder<Block, BlockState> builderFixToCustom(Object owner){
		return new BSDefF.BuilderF((Block) owner);
	}
}
