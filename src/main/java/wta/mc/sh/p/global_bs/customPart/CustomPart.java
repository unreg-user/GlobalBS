package wta.mc.sh.p.global_bs.customPart;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import wta.mc.sh.p.global_bs.unregistries.BlockPropertyUnreg;
import wta.mc.sh.p.global_bs.unregistries.PropUnregistries;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry;
import wta.mc.sh.p.global_bs.unregistries.PropertyUnregistry.UnregPropHandler;

public class CustomPart {
	public static final EnumProperty<Direction> FACING_GLOBAL;
	public static final EnumProperty<Direction> HORIZONTAL_FACING_GLOBAL;
	public static final Direction FACING_GLOBAL_DEFAULT;
	public static final Direction HORIZONTAL_FACING_GLOBAL_DEFAULT;

	public static void init(){
		FacingControl.init();
	}

	static {
		PropUnregistries.init();
		var blockUnreg = PropUnregistries.BLOCK_UNREG;

		FACING_GLOBAL = (EnumProperty<Direction>) blockUnreg.alwaysReg(new PropertyUnregistry.AllRegPropInfo<>(
			  EnumProperty.create(
					"facing_global",
					Direction.class,
				    Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
			  ), FACING_GLOBAL_DEFAULT = Direction.UP));
		HORIZONTAL_FACING_GLOBAL = (EnumProperty<Direction>) blockUnreg.alwaysReg(new PropertyUnregistry.AllRegPropInfo<>(
			  EnumProperty.create(
					"horizontal_facing_global",
					Direction.class, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST
				    ), HORIZONTAL_FACING_GLOBAL_DEFAULT = Direction.SOUTH));
		blockUnreg.unreg(UnregPropHandler.ofMiddlepartReplace(
			  BlockStateProperties.FACING, Direction.NORTH, new PropertyUnregistry.UnregisteredValueHandlerSetter<Direction>(){
				  @Override
				  public <O, S> S setValue(StateHolder<O, S> self, Direction value) {
					  return self.setValue(FACING_GLOBAL, DirectionCMathP.unmarkRotated(value));
				  }
			  }
		));
		blockUnreg.unreg(UnregPropHandler.ofMiddlepartReplace(
			  BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH, new PropertyUnregistry.UnregisteredValueHandlerSetter<Direction>(){
				  @SuppressWarnings("unchecked")
				  @Override
				  public <O, S> S setValue(StateHolder<O, S> self, Direction value) {
					  value = DirectionCMathP.unmarkRotated(value);
					  if (value.getAxis().isHorizontal())
						  return self.setValue(HORIZONTAL_FACING_GLOBAL, value);
					  return (S) self;
				  }
			  }
		));
		blockUnreg.unreg(UnregPropHandler.ofMiddlepartReplace(
			  BlockStateProperties.AXIS, Direction.Axis.X, new PropertyUnregistry.UnregisteredValueHandlerSetter<Direction.Axis>(){
				  @SuppressWarnings("unchecked")
				  @Override
				  public <O, S> S setValue(StateHolder<O, S> self, Direction.Axis value) {
					  return switch (value) {
						  case X -> ((StateHolder<O, S>) self.setValue(FACING_GLOBAL, FACING_GLOBAL_DEFAULT))
							    .setValue(HORIZONTAL_FACING_GLOBAL, Direction.EAST);
						  case Y -> ((StateHolder<O, S>) self.setValue(FACING_GLOBAL, Direction.NORTH))
							    .setValue(HORIZONTAL_FACING_GLOBAL, HORIZONTAL_FACING_GLOBAL_DEFAULT);
						  case Z -> ((StateHolder<O, S>) self.setValue(FACING_GLOBAL, FACING_GLOBAL_DEFAULT))
							    .setValue(HORIZONTAL_FACING_GLOBAL, Direction.SOUTH);
						  default -> (S) self;
					  };
				  }
			  }
		));
		blockUnreg.registerModelHandler(BlockPropertyUnreg.BSModelHandler.fromVariant(
			  (variant, _, newState) -> DirectionCMath.getRotate2DirInfoForState(newState).apply(variant)
		));
	}
}
