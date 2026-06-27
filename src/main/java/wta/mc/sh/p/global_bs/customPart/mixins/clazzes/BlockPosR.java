package wta.mc.sh.p.global_bs.customPart.mixins.clazzes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;
import wta.mc.sh.p.global_bs.customPart.DirectionCMath;
import wta.mc.sh.p.global_bs.customPart.mixins.interfaces.DirectionFI;

public class BlockPosR extends BlockPos {
	private final DirectionCMath.Rotate2DirInfo info;

	public BlockPosR(int x, int y, int z, DirectionCMath.Rotate2DirInfo info) {
		super(x, y, z);
		this.info = info;
	}

	public BlockPosR(Vec3i blockPos, DirectionCMath.Rotate2DirInfo info) {
		this(blockPos.getX(), blockPos.getY(), blockPos.getZ(), info);
	}

	public BlockPosR(Vec3i blockPos, StateHolder<Block, BlockState> state) {
		this(blockPos.getX(), blockPos.getY(), blockPos.getZ(), DirectionCMath.getRotate2DirInfoForState(state));
	}

	// Overrides

	@Override
	public @NonNull BlockPos offset(int x, int y, int z) {
		var rotated = getRotated(x, y, z);
		return x == 0 && y == 0 && z == 0 ? this :
			  new BlockPosR(
					this.getX() + rotated.x,
					this.getY() + rotated.y,
					this.getZ() + rotated.z,
					info);
	}

	@Override
	public @NonNull BlockPos multiply(final int scale) {
		if (scale == 1) {
			return this;
		} else {
			return scale == 0 ? new BlockPosR(0, 0, 0, info) :
				  new BlockPosR(
						this.getX() * scale,
						this.getY() * scale,
						this.getZ() * scale,
						info);
		}
	}

	@Override
	public @NonNull BlockPos rotate(final Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_90 -> new BlockPosR(-this.getZ(), this.getY(), this.getX(), info);
			case CLOCKWISE_180 -> new BlockPosR(-this.getX(), this.getY(), -this.getZ(), info);
			case COUNTERCLOCKWISE_90 -> new BlockPosR(this.getZ(), this.getY(), -this.getX(), info);
			case NONE -> this;
		};
	}

	@Override
	public @NonNull BlockPos cross(final Vec3i upVector) {
		return new BlockPosR(
			  this.getY() * upVector.getZ() - this.getZ() * upVector.getY(),
			  this.getZ() * upVector.getX() - this.getX() * upVector.getZ(),
			  this.getX() * upVector.getY() - this.getY() * upVector.getX(),
			  info
		);
	}


	@Override
	public @NonNull BlockPos relative(final @NonNull Direction direction) {
		var rotated = ((DirectionFI) (Object) direction).global_bs$isRotated() ?  new Vector3i(direction.getStepX(), direction.getStepY(), direction.getStepZ()) : getRotated(direction.getStepX(), direction.getStepY(), direction.getStepZ());
		return new BlockPosR(
			  this.getX() + rotated.x,
			  this.getY() + rotated.y,
			  this.getZ() + rotated.z,
			  info);
	}

	@Override
	public @NonNull BlockPos relative(final @NonNull Direction direction, final int steps) {
		var rotated = ((DirectionFI) (Object) direction).global_bs$isRotated() ?  new Vector3i(direction.getStepX(), direction.getStepY(), direction.getStepZ()) : getRotated(direction.getStepX(), direction.getStepY(), direction.getStepZ());
		return steps == 0
			  ? this
			  : new BlockPosR(
			  this.getX() + rotated.x * steps,
			  this.getY() + rotated.y * steps,
			  this.getZ() + rotated.z * steps,
			  info);
	}

	@Override
	public @NonNull BlockPos relative(final Direction.@NonNull Axis axis, final int steps) {
		if (steps == 0) {
			return this;
		} else {
			int xStep = axis == Direction.Axis.X ? steps : 0;
			int yStep = axis == Direction.Axis.Y ? steps : 0;
			int zStep = axis == Direction.Axis.Z ? steps : 0;
			var rotated = new Vector3i(xStep, yStep, zStep);
			return new BlockPosR(
				  this.getX() + rotated.x,
				  this.getY() + rotated.y,
				  this.getZ() + rotated.z,
				  info);
		}
	}

	// API
	private final Vector3i getRotated(int x, int y, int z) {
		return info.rotate(new Vector3i(x, y, z));
	}

	private final Vec3i getRotatedVec3i(int x, int y, int z) {
		return DirectionCMath.toVec3i(getRotated(x, y, z));
	}

	public final DirectionCMath.Rotate2DirInfo getInfo() {
		return info;
	}
}
