package wta.mc.sh.p.global_bs.customPart.mixins.plugin.reflections;

import net.minecraft.core.Direction;
import wta.mc.sh.p.global_bs.customPart.MFHelperC;

import java.util.Map;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

public class DirectionRF {
	public static Direction R_DOWN;
	public static Direction R_UP;
	public static Direction R_NORTH;
	public static Direction R_SOUTH;
	public static Direction R_WEST;
	public static Direction R_EAST;

	public static final Direction[] NR_VALUES;
	public static Map<Direction, Direction> NR_VALUES_TO_R_VALUES;
	public static Map<Direction, Direction> R_VALUES_TO_NR_VALUES;

	static {
		NR_VALUES = new Direction[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
	}

	public static void init(
		  Direction r_down,
		  Direction r_up,
		  Direction r_north,
		  Direction r_south,
		  Direction r_west,
		  Direction r_east
	) {
		R_DOWN = r_down;
		R_UP = r_up;
		R_NORTH = r_north;
		R_SOUTH = r_south;
		R_WEST = r_west;
		R_EAST = r_east;

		NR_VALUES_TO_R_VALUES = Map.of(
			  DOWN, DirectionRF.R_DOWN,
			  UP, DirectionRF.R_UP,
			  NORTH, DirectionRF.R_NORTH,
			  SOUTH, DirectionRF.R_SOUTH,
			  WEST, DirectionRF.R_WEST,
			  EAST, DirectionRF.R_EAST
		);
		R_VALUES_TO_NR_VALUES = MFHelperC.reverseFinal(NR_VALUES_TO_R_VALUES);
	}
}
