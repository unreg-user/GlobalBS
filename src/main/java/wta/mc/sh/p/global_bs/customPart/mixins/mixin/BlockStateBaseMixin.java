package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.math.OctahedralGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.customPart.DirectionCMath;
import wta.mc.sh.p.global_bs.customPart.mixins.clazzes.BlockPosR;
import wta.mc.sh.p.global_bs.customPart.mixins.interfaces.ShouldBe1RotatedFI;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin extends StateHolder<Block, BlockState> implements TypedInstance<Block> {
	@Shadow
	public abstract Block getBlock();

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
	public void collisionFix0(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	@Inject(
		  method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		  at = @At("RETURN"), cancellable = true)
	public void collisionFix1(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	@Inject(
		  method = "getEntityInsideCollisionShape",
		  at = @At("RETURN"), cancellable = true)
	public void collisionFix2(BlockGetter level, BlockPos pos, Entity entity, CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	@Inject(
		  method = "getBlockSupportShape",
		  at = @At("RETURN"), cancellable = true)
	public void collisionFix3(BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	@Inject(
		  method = "getVisualShape",
		  at = @At("RETURN"), cancellable = true)
	public void collisionFix4(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	@Inject(
		  method = "getInteractionShape",
		  at = @At("RETURN"), cancellable = true)
	public void collisionFix5(BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(fixCollision(cir.getReturnValue()));
	}

	// blockPosFixes
	@ModifyVariable(
		  method = {
				"isValidSpawn",
				"getMapColor",
				"emissiveRendering",
				"getShadeBrightness",
				"isRedstoneConductor",
				"getSignal",
				"getAnalogOutputSignal",
				"getDestroySpeed",
				"getDestroyProgress",
				"getDirectSignal",
				"getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
				"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
				"getEntityInsideCollisionShape",
				"getBlockSupportShape",
				"getVisualShape",
				"getInteractionShape",
				"getOffset",
				"triggerEvent",
				"handleNeighborChanged",
				"updateNeighbourShapes(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V",
				"updateIndirectNeighbourShapes(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V",
				"onPlace",
				"affectNeighborsAfterRemoval",
				"onExplosionHit",
				"tick",
				"randomTick",
				"entityInside",
				"spawnAfterBreak",
				"attack",
				"isSuffocating",
				"isViewBlocking",
				"updateShape",
				"canSurvive",
				"getPostProcessPos",
				"getMenuProvider",
				"getSeed",
				"isFaceSturdy(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/SupportType;)Z",
				"isCollisionShapeFullBlock",
				"getCloneItemStack"
		  },
		  at = @At("HEAD"), argsOnly = true, name = "pos")
	public BlockPos blockPosFix(BlockPos pos) {
		return new BlockPosR(pos, this);
	}

	@ModifyVariable(
		  method = "updateShape",
		  at = @At("HEAD"), argsOnly = true, name = "neighbourPos")
	public BlockPos blockPosFix1(BlockPos neighbourPos) {
		return blockPosFix(neighbourPos);
	}

	// other local fix
	@ModifyVariable(
		  method = {"useItemOn", "useWithoutItem"},
		  at = @At("HEAD"), argsOnly = true, name = "hitResult")
	public BlockHitResult hitResultFix(BlockHitResult hitResult) {
		return new BlockHitResult(
			  hitResult.getLocation(),
			  hitResult.getDirection(),
			  blockPosFix(hitResult.getBlockPos()),
			  hitResult.isInside());
	}

	@Mixin(BlockPlaceContext.class)
	public static class BlockPlaceContextMixin {
		@ModifyVariable(
			  method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/BlockHitResult;)V",
			  at = @At("HEAD"),
			  argsOnly = true, name = "hitResult")
		private static BlockHitResult hitResultFix0(
			  BlockHitResult hitResult,
			  @Local(argsOnly = true, name = "level") Level level) {
			var pos = hitResult.getBlockPos();
			return new BlockHitResult(
				  hitResult.getLocation(),
				  hitResult.getDirection(),
				  new BlockPosR(pos, level.getBlockState(pos)),
				  hitResult.isInside());
		}
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
	protected BlockStateBaseMixin(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
		super(owner, propertyKeys, propertyValues);
	}
}
