package wta.mc.sh.p.global_bs.customPart;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import wta.mc.sh.p.global_bs.GlobalBS;
import wta.mc.sh.p.global_bs.customPart.mixins.interfaces.PlayerFI;


public class FacingControl {
	public static final KeyMapping PLACEMENT_FACING_KEYM = new KeyMapping(
		  "key.global_bs.placement_facing",
		  InputConstants.Type.KEYSYM,
		  GLFW.GLFW_KEY_R,
		  KeyMapping.Category.GAMEPLAY
	);

	public static final KeyMapping PLACEMENT_HORIZONTAL_FACING_KEYM = new KeyMapping(
		  "key.global_bs.placement_horizontal_facing",
		  InputConstants.Type.KEYSYM,
		  GLFW.GLFW_KEY_G,
		  KeyMapping.Category.GAMEPLAY
	);

	public record RotationPacketPayload(byte facingId, byte horizontalId) implements CustomPacketPayload {
		public static final Type<RotationPacketPayload> TYPE = new Type<>(
			  Identifier.fromNamespaceAndPath(GlobalBS.MODID, "placement_facing_control_packet")
		);

		public static final StreamCodec<FriendlyByteBuf, RotationPacketPayload> CODEC = StreamCodec.composite(
			  ByteBufCodecs.BYTE, RotationPacketPayload::facingId,
			  ByteBufCodecs.BYTE, RotationPacketPayload::horizontalId,
			  RotationPacketPayload::new
		);

		@Override
		public @NonNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void init() {
	}

	private static void sendFacingControlPacket(Direction facing, Direction horizontal) {
		ClientPlayNetworking.send(new FacingControl.RotationPacketPayload(
			  (byte) facing.get3DDataValue(),
			  (byte) horizontal.get2DDataValue()
		));
	}

	static {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			var player = (PlayerFI) client.player;

			if (PLACEMENT_FACING_KEYM.consumeClick()) {
				int nextId = (player.global_bs$getPlacementFacing().get3DDataValue() + 1) % 6;
				Direction nextDir = Direction.from3DDataValue(nextId);
				player.global_bs$setPlacementFacing(nextDir);
				sendFacingControlPacket(nextDir, player.global_bs$getPlacementHorizontalFacing());
			}

			if (PLACEMENT_HORIZONTAL_FACING_KEYM.consumeClick()) {
				int nextId = (player.global_bs$getPlacementHorizontalFacing().get2DDataValue() + 1) % 4;
				Direction nextDir = Direction.from2DDataValue(nextId);
				player.global_bs$setPlacementHorizontalFacing(nextDir);
				sendFacingControlPacket(player.global_bs$getPlacementFacing(), nextDir);
			}
		});

		PayloadTypeRegistry.serverboundPlay().register(RotationPacketPayload.TYPE, RotationPacketPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(RotationPacketPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				var player = (PlayerFI) context.player();

				player.global_bs$setPlacementFacing(Direction.from3DDataValue(payload.facingId()));
				player.global_bs$setPlacementHorizontalFacing(Direction.from2DDataValue(payload.horizontalId()));
			});
		});
	}
}
