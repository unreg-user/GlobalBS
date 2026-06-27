package wta.mc.sh.p.global_bs.customPart;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;

import static com.mojang.math.Quadrant.*;

public class DirectionCMath {
	static final RotateByQuadrants[] GET_RBQ_FOR_CACHE;
	static final OctahedralGroup[] GET_OCTAHEDRAL_FOR_CACHE;
	static final Map<OctahedralGroup, RotateByQuadrants> GET_RBQ_BY_OCTAHEDRAL_CACHE;
	static final Rotate2DirInfo[] R2D_INFO_CACHE;

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

	private static int getIndex(Direction direction, Direction horizontal) {
		return direction.get3DDataValue() * 4 + horizontal.get2DDataValue();
	}

	public static Rotate2DirInfo getRotate2DirInfo(Direction direction, Direction horizontal){
		return R2D_INFO_CACHE[getIndex(direction, horizontal)];
	}

	public static Rotate2DirInfo getRotate2DirInfoForState(StateHolder<Block, BlockState> state){
		return getRotate2DirInfo(
			  state.getValue(CustomPart.FACING_GLOBAL),
			  state.getValue(CustomPart.HORIZONTAL_FACING_GLOBAL));
	}

	public static Vec3i toVec3i(Vector3i vector) {
		return new Vec3i(vector.x, vector.y, vector.z);
	}

	public static BlockPos toBlockPos(Vector3i vector) {
		return new BlockPos(vector.x, vector.y, vector.z);
	}

	static {
		{
			var map = new HashMap<OctahedralGroup, RotateByQuadrants>();
			for (var x : Quadrant.values()) {
				for (var y : Quadrant.values()) {
					for (var z : Quadrant.values()) {
						map.put(Quadrant.fromXYZAngles(x, y, z), new RotateByQuadrants(x, y, z));
					}
				}
			}
			GET_RBQ_BY_OCTAHEDRAL_CACHE = Map.copyOf(map);
		}
		{
			GET_RBQ_FOR_CACHE = new RotateByQuadrants[24];
			GET_OCTAHEDRAL_FOR_CACHE = new OctahedralGroup[24];
			R2D_INFO_CACHE = new Rotate2DirInfo[24];
			for (var i3 : Direction.values()) {
				for (var i2 : Direction.Plane.HORIZONTAL) {
					var rbqResult = getRBQForGenerator(i3, i2);
					var i3i = i3.get3DDataValue();
					var i2i = i2.get2DDataValue();

					var index = i3i*4 + i2i;
					GET_RBQ_FOR_CACHE[index] = rbqResult;
					GET_OCTAHEDRAL_FOR_CACHE[index] = rbqResult.getOctahedral();
					R2D_INFO_CACHE[index] = new Rotate2DirInfo(i3i, i2i, index);
				}
			}
		}
	}

	public static class Rotate2DirInfo {
		public final int dir;
		public final int hor;
		private final int index;

		Rotate2DirInfo(int dir, int hor, int index) {
			this.dir = dir;
			this.hor = hor;
			this.index = index;
		}

		public Direction getDDir() {
			return Direction.from3DDataValue(dir);
		}

		public Direction getDHor() {
			return Direction.from2DDataValue(hor);
		}

		public Variant apply(Variant variant) {
			var state = variant.modelState();
			var origOctahedral = Quadrant.fromXYZAngles(state.x(), state.y(), state.z());
			var customOctahedral = DirectionCMath.GET_OCTAHEDRAL_FOR_CACHE[index];
			var resultRBQ = DirectionCMath.GET_RBQ_BY_OCTAHEDRAL_CACHE.get(customOctahedral.compose(origOctahedral));

			return variant.withState(new Variant.SimpleModelState(
				  resultRBQ.x,
				  resultRBQ.y,
				  resultRBQ.z,
				  state.uvLock()
			));
		}

		public Vector3i rotate(Vector3i source) {
			return getOctahedral().rotate(source);
		}

		public RotateByQuadrants getRBQ(){
			return GET_RBQ_FOR_CACHE[index];
		}

		public OctahedralGroup getOctahedral(){
			return GET_OCTAHEDRAL_FOR_CACHE[index];
		}
	}

	public record RotateByQuadrants(Quadrant x, Quadrant y, Quadrant z) {
		OctahedralGroup getOctahedral(){
			return Quadrant.fromXYZAngles(x, y, z);
		}
	}
}
