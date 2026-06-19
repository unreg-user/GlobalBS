package wta.mc.sh.p.global_bs.customPart;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import wta.mc.sh.p.global_bs.unregistries.BlockPropertyUnreg;
import wta.mc.sh.p.global_bs.unregistries.PropUnregistries;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry.UnregPropHandler;

public class CustomPart {
	public static final EnumProperty<Direction> FACING_GLOBAL;
	public static final EnumProperty<Direction> HORIZONTAL_FACING_GLOBAL;

	public static void init(){
	}

	static {
		PropUnregistries.init();
		var blockUnreg = PropUnregistries.BLOCK_UNREG;

		FACING_GLOBAL = (EnumProperty<Direction>) blockUnreg.alwaysReg(new PropertyUnregistry.AllRegPropInfo<>(
			  EnumProperty.create(
					"facing_global",
					Direction.class,
				    Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
			  ), Direction.UP));
		HORIZONTAL_FACING_GLOBAL = (EnumProperty<Direction>) blockUnreg.alwaysReg(new PropertyUnregistry.AllRegPropInfo<>(
			  EnumProperty.create(
					"horizontal_facing_global",
					Direction.class, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST
				    ), Direction.SOUTH));
		blockUnreg.unreg(UnregPropHandler.ofMiddlepart(BlockStateProperties.FACING, Direction.NORTH));
		blockUnreg.unreg(UnregPropHandler.ofMiddlepart(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
		blockUnreg.unreg(UnregPropHandler.ofMiddlepart(BlockStateProperties.AXIS, Direction.Axis.X));
		blockUnreg.registerModelHandler(BlockPropertyUnreg.BSModelHandler.fromVariant(
			  (variant, _, newState) -> DirectionCMath.getRotate2DirInfoForState(newState).apply(variant)
		));
	}
}
