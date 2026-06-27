package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.customPart.DirectionCMath;
import wta.mc.sh.p.global_bs.customPart.mixins.interfaces.ShouldBe1RotatedFI;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixinShapePart extends StateHolder<Block, BlockState> implements TypedInstance<Block>  {
	@Unique
	public VoxelShape fixCollision(VoxelShape shape) {
		if (((ShouldBe1RotatedFI) shape).global_bs$isRotated())
			return shape;
		var rotation = DirectionCMath.getRotate2DirInfoForState(this).getOctahedral();
		if (rotation == OctahedralGroup.IDENTITY)
			return shape;
		var result = Shapes.rotate(shape, rotation);
		((ShouldBe1RotatedFI) result).global_bs$markRotated();
		return result;
	}

	// collisionFixes
	@Inject(
		  method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		  at = @At("RETURN"), cancellable = true)
	public void collisionFix0(CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	@Inject(
		  method = {
				"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			    "getEntityInsideCollisionShape",
			    "getBlockSupportShape",
			    "getVisualShape",
			    "getInteractionShape"
		  },
		  at = @At("RETURN"), cancellable = true)
	public void collisionFix1(CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	@Mixin(VoxelShape.class)
	public static abstract class VoxelShapeMixin implements ShouldBe1RotatedFI {
		@Unique
		boolean isRotated = false;

		@Override
		public boolean global_bs$isRotated() {
			return isRotated;
		}

		@Override
		public void global_bs$markRotated() {
			isRotated = true;
		}
	}

	// костыли
	protected BlockStateBaseMixinShapePart(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
		super(owner, propertyKeys, propertyValues);
	}
}
