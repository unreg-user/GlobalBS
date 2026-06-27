package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import static net.minecraft.core.Direction.*;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wta.mc.sh.p.global_bs.customPart.DirectionCMathP;
import wta.mc.sh.p.global_bs.customPart.mixins.interfaces.DirectionFI;
import wta.mc.sh.p.global_bs.customPart.mixins.plugin.reflections.DirectionRF;

import static wta.mc.sh.p.global_bs.customPart.mixins.plugin.reflections.DirectionRF.*;

@Mixin(Direction.class)
public abstract class DirectionMixin implements DirectionFI {
	@Shadow
	@Final
	@Mutable
	private int data3d;
	@Shadow
	@Final
	@Mutable
	private int data2d;
	@Shadow
	@Final
	@Mutable
	public int oppositeIndex;
	@Shadow
	@Final
	@Mutable
	private String name;
	@Shadow
	@Final
	@Mutable
	private Axis axis;
	@Shadow
	@Final
	@Mutable
	private AxisDirection axisDirection;
	@Shadow
	@Final
	@Mutable
	private Vec3i normal;
	@Shadow
	@Final
	@Mutable
	private Vec3 normalVec3;
	@Shadow
	@Final
	@Mutable
	private Vector3fc normalVec3f;

	@Unique
	private boolean is_rotated = false;

	@SuppressWarnings({"MissingUnique", "unused"}) private static Direction R_DOWN;
	@SuppressWarnings({"MissingUnique", "unused"}) private static Direction R_UP;
	@SuppressWarnings({"MissingUnique", "unused"}) private static Direction R_NORTH;
	@SuppressWarnings({"MissingUnique", "unused"}) private static Direction R_SOUTH;
	@SuppressWarnings({"MissingUnique", "unused"}) private static Direction R_WEST;
	@SuppressWarnings({"MissingUnique", "unused"}) private static Direction R_EAST;

	@Override
	public boolean global_bs$isRotated() {
		return is_rotated;
	}

	/**
	 * @author UnregUser
	 * @reason fix rotation
	 */
	@Overwrite
	public static Direction[] values() {
		return NR_VALUES.clone();
	}

	/**
	 * @author UnregUser
	 * @reason fix rotation
	 */
	@Overwrite
	private static Direction[] $values() {
		return NR_VALUES.clone();
	}

	@SuppressWarnings("NameDoesntMatchTargetClass")
	@Inject(
		  method = "<init>",
		  at = @At("TAIL"))
	private void initFix(
		  String n,
		  int i,
		  int data3d,
		  int oppositeIndex,
		  int data2d,
		  String name,
		  AxisDirection axisDirection,
		  Axis axis,
		  Vec3i normal,
		  CallbackInfo info) {
		if ("r_down".equals(name)) {
			shadowOf(DOWN);
		} else if ("r_up".equals(name)) {
			shadowOf(UP);
		} else if ("r_north".equals(name)) {
			shadowOf(NORTH);
		} else if ("r_south".equals(name)) {
			shadowOf(SOUTH);
		} else if ("r_west".equals(name)) {
			shadowOf(WEST);
		} else if ("r_east".equals(name)) {
			shadowOf(EAST);
		}
	}

	@ModifyVariable(
		  method = "<init>",
		  at = @At("HEAD"),
		  argsOnly = true, name = "normal")
	private static Vec3i initNullFix0(Vec3i normal){
		return normal != null ? normal : new Vec3i(0, 0, 0);
	}

	@Unique
	private void shadowOf(Direction direction) {
		this.data3d = direction.get3DDataValue();
		this.data2d = direction.get2DDataValue();
		this.oppositeIndex = direction.oppositeIndex;
		this.name = "r_" + direction.getName();
		this.axis = direction.getAxis();
		this.axisDirection = direction.getAxisDirection();
		this.normal = direction.getUnitVec3i();
		this.normalVec3 = direction.getUnitVec3();
		this.normalVec3f = direction.getUnitVec3f();
		this.is_rotated = true;
	}

	@Inject(
		  method = {
				"getOpposite",
			    "getClockWise()Lnet/minecraft/core/Direction;",
			    "getClockWise(Lnet/minecraft/core/Direction$Axis;)Lnet/minecraft/core/Direction;",
			    "getCounterClockWise()Lnet/minecraft/core/Direction;",
			    "getCounterClockWise(Lnet/minecraft/core/Direction$Axis;)Lnet/minecraft/core/Direction;",
			    "getClockWiseX",
			    "getClockWiseZ",
			    "getCounterClockWiseX",
			    "getCounterClockWiseZ"
		  },
		  at = @At("RETURN"),
		  cancellable = true)
	private void fixUnrotatedReturn(CallbackInfoReturnable<Direction> cir) {
		if (is_rotated) {
			cir.setReturnValue(NR_VALUES_TO_R_VALUES.get(cir.getReturnValue()));
		}
	}

	@Redirect(
		  method = {
				"getYRot",
				"getRotation",
				"getFacingAxis",
				"getClockWise()Lnet/minecraft/core/Direction;",
				"getClockWise(Lnet/minecraft/core/Direction$Axis;)Lnet/minecraft/core/Direction;",
				"getCounterClockWise()Lnet/minecraft/core/Direction;",
				"getCounterClockWise(Lnet/minecraft/core/Direction$Axis;)Lnet/minecraft/core/Direction;",
				"getClockWiseX",
				"getClockWiseZ",
				"getCounterClockWiseX",
				"getCounterClockWiseZ"
		  },
		  at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;ordinal()I")
	)
	private static int fixOrdinal(Direction instance) {
		return DirectionCMathP.fixOrdinal(instance);
	}

	@Inject(
		  method = "<clinit>",
		  at = @At("TAIL")
	)
	private static void initRF(CallbackInfo ci) {
		DirectionRF.init(R_DOWN, R_UP, R_NORTH, R_SOUTH, R_WEST, R_EAST);
	}
}
