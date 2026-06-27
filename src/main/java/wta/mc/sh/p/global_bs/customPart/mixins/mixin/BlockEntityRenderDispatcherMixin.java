package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wta.mc.sh.p.global_bs.customPart.DirectionCMath;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
	@WrapOperation(
		  method = "submit",
		  at = @At(
			    value = "INVOKE",
			    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
	public <S extends BlockEntityRenderState> void fixPose(BlockEntityRenderer<?, S> instance, S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Operation<Void> original){
		DirectionCMath.Rotate2DirInfo rot = DirectionCMath.getRotate2DirInfoForState(state.blockState);

		if (rot != null) {
			poseStack.pushPose();
			var matrix = poseStack.last().pose();
			matrix.translate(0.5F, 0.5F, 0.5F);
			matrix.mul(new Matrix4f(rot.getOctahedral().transformation()));
			matrix.translate(-0.5F, -0.5F, -0.5F);
			original.call(instance, state, poseStack, submitNodeCollector, cameraRenderState);
			poseStack.popPose();
		} else {
			original.call(instance, state, poseStack, submitNodeCollector, cameraRenderState);
		}
	}
}
