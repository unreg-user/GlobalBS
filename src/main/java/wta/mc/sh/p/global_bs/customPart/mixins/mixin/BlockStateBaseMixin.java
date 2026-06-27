package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wta.mc.sh.p.global_bs.customPart.mixins.clazzes.BlockPosR;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin extends StateHolder<Block, BlockState> implements TypedInstance<Block> {
	@Shadow
	public abstract Block getBlock();

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
		  method = "",
		  at = @At("HEAD"), argsOnly = true, name = "direction")
	public BlockPos directionFix(BlockPos pos) {
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

	// костыли
	protected BlockStateBaseMixin(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
		super(owner, propertyKeys, propertyValues);
	}
}
