package wta.mc.sh.p.global_bs.customPart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import wta.mc.sh.p.global_bs.customPart.mixins.clazzes.BlockPosR;
import wta.mc.sh.p.global_bs.customPart.mixins.plugin.reflections.DirectionRF;

import static wta.mc.sh.p.global_bs.customPart.DirectionCMath.getRotate2DirInfoForState;

@SuppressWarnings("unused")
public class DirectionCMathP {
	public static Direction getRotated(Direction direction, BlockState state) {
		if (direction == null) return null;
		if (state == null) return direction;
		var info = getRotate2DirInfoForState(state);
		return DirectionRF.NR_VALUES_TO_R_VALUES.get(info.getOctahedral().rotate(direction));
	}

	public static Direction.Axis getRotated(Direction.Axis axis, BlockState state) {
		if (axis == null) return null;
		if (state == null) return axis;
		var info = getRotate2DirInfoForState(state);
		return info.getOctahedral().rotate(axis.getDirections()[0]).getAxis();
	}

	public static BlockPos getRotated(BlockPos pos, BlockState state) {
		if (pos == null) return null;
		if (state == null) return pos;
		return new BlockPosR(pos, state);
	}

	public static int fixOrdinal(Direction direction) {
		return direction.get3DDataValue();
	}

	public static Direction unmarkRotated(Direction direction) {
		return DirectionRF.R_VALUES_TO_NR_VALUES.getOrDefault(direction, direction);
	}

	public static Direction markRotated(Direction direction) {
		return DirectionRF.NR_VALUES_TO_R_VALUES.getOrDefault(direction, direction);
	}
}
