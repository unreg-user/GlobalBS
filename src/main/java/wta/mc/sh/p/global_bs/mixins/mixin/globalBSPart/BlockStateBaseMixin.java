package wta.mc.sh.p.global_bs.mixins.mixin.globalBSPart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.customPart.DirectionCMath;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin extends StateHolder<Block, BlockState> implements TypedInstance<Block> {
	@Inject(
		  method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		  at = @At("RETURN"),
		  cancellable = true)
	public void getCollisionShapeFix(BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir){
		cir.setReturnValue(Shapes.rotate(cir.getReturnValue(), DirectionCMath.getRotate2DirInfoForState(this).getOctahedral()));
	}

	// костыли
	protected BlockStateBaseMixin(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
		super(owner, propertyKeys, propertyValues);
	}
}
