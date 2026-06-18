package wta.mc.sh.p.global_bs.customPart;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.mojang.math.Quadrant.*;
import static com.mojang.math.Quadrant.R270;

public class DirectionCMath {
	static final RotateByQuadrants[][] GET_RBQ_FOR_CACHE;
	static final OctahedralGroup[][] GET_OCTAHEDRAL_FOR_CACHE;
	static final Map<OctahedralGroup, RotateByQuadrants> GET_RBQ_BY_OCTAHEDRAL_CACHE;

	private static RotateByQuadrants getRBQForGenerator(Direction direction, Direction horizontal) {
		Quadrant y = switch (horizontal) {
			case SOUTH -> R0;
			case WEST -> R90;
			case NORTH -> R180;
			case EAST -> R270;
			default -> throw new IllegalArgumentException("Direction must be horizontal, but got: " + horizontal);
		};

		Quadrant x = switch (direction) {
			case NORTH -> R90;
			case SOUTH -> R270;
			case DOWN -> R180;
			default -> R0;
		};

		Quadrant z = switch (direction) {
			case WEST -> R90;
			case EAST -> R270;
			default -> R0;
		};

		return new RotateByQuadrants(x, y, z);
	}

	public static OctahedralGroup getOctahedralFromQuadrants(Quadrant x, Quadrant y, Quadrant z){
		return x.rotationX.compose(y.rotationY).compose(z.rotationZ);
	}

	public static Rotate2DirInfo getRotate2DirInfo(Direction direction, Direction horizontal){
		return new Rotate2DirInfo(direction.get3DDataValue(), horizontal.get2DDataValue());
	}

	public static Rotate2DirInfo getRotate2DirInfoForState(StateHolder<Block, BlockState> state){
		return new Rotate2DirInfo(state.getValue(CustomPart.FACING_GLOBAL).get3DDataValue(), state.getValue(CustomPart.HORIZONTAL_FACING_GLOBAL).get2DDataValue());
	}

	static {
		{
			var map = new HashMap<OctahedralGroup, RotateByQuadrants>();
			for (var x : Quadrant.values()) {
				for (var y : Quadrant.values()) {
					for (var z : Quadrant.values()) {
						map.put(getOctahedralFromQuadrants(x, y, z), new RotateByQuadrants(x, y, z));
					}
				}
			}
			GET_RBQ_BY_OCTAHEDRAL_CACHE = Map.copyOf(map);
		}
		{
			GET_RBQ_FOR_CACHE = new RotateByQuadrants[6][4];                     // 64, lol
			GET_OCTAHEDRAL_FOR_CACHE = new OctahedralGroup[6][4];
			for (var i3 : Direction.values()) {
				for (var i2 : Direction.Plane.HORIZONTAL) {
					var rbqResult = getRBQForGenerator(i3, i2);
					var i3i = i3.get3DDataValue();
					var i2i = i2.get2DDataValue();
					GET_RBQ_FOR_CACHE[i3i][i2i] = rbqResult;
					GET_OCTAHEDRAL_FOR_CACHE[i3i][i2i] = rbqResult.getOctahedral();
				}
			}
		}
	}

	public record Rotate2DirInfo(int dir, int hor) {
		public Variant apply(Variant variant) {
			var state = variant.modelState();
			var origOctahedral = DirectionCMath.getOctahedralFromQuadrants(state.x(), state.y(), state.z());
			var customOctahedral = DirectionCMath.GET_OCTAHEDRAL_FOR_CACHE[dir][hor];
			var resultRBQ = DirectionCMath.GET_RBQ_BY_OCTAHEDRAL_CACHE.get(origOctahedral.compose(customOctahedral));

			return variant.withState(new Variant.SimpleModelState(
				  resultRBQ.x,
				  resultRBQ.y,
				  resultRBQ.z,
				  state.uvLock()
			));
		}

		public RotateByQuadrants getRBQ(){
			return GET_RBQ_FOR_CACHE[dir][hor];
		}

		public OctahedralGroup getOctahedral(){
			return GET_OCTAHEDRAL_FOR_CACHE[dir][hor];
		}
	}

	public record RotateByQuadrants(Quadrant x, Quadrant y, Quadrant z) {
		OctahedralGroup getOctahedral(){
			return DirectionCMath.getOctahedralFromQuadrants(x, y, z);
		}
	}
}
