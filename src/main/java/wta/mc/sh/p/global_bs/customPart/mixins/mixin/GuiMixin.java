package wta.mc.sh.p.global_bs.customPart.mixins.mixin;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class GuiMixin {
	/*
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
		  method = "extractItemHotbar",
		  at = @At("TAIL"))
	private void fixSelectItem(
		  GuiGraphicsExtractor graphics,
		  DeltaTracker deltaTracker,
		  CallbackInfo ci,
		  @Local(name = "player") Player player) {
		if (player != null) {
			ItemStack handItem = player.getItemInHand(player.getUsedItemHand());
			int screenCenter = graphics.guiWidth() / 2;
			int i = -1;
			int x = screenCenter - 90 + i * 20 + 2;
			int y = graphics.guiHeight() - 16 - 3;
		}
	}*/
}
