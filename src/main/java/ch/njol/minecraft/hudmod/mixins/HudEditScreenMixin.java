package ch.njol.minecraft.hudmod.mixins;

import ch.njol.minecraft.hudmod.elements.HeldItemTooltip;
import ch.njol.minecraft.hudmod.elements.OverlayMessage;
import ch.njol.minecraft.uiframework.hud.HudEditScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HudEditScreen.class)
public class HudEditScreenMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	void tick_head(CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.inGameHud.setOverlayMessage(Text.of(OverlayMessage.EDIT_SAMPLE_MESSAGE), false);
		((InGameHudAccessor) client.inGameHud).setHeldItemTooltipFade(40);
		ItemStack fakeHeldItem = new ItemStack(Items.OAK_PLANKS, 1);
		fakeHeldItem.setCustomName(HeldItemTooltip.EDIT_SAMPLE_MESSAGE);
		((InGameHudAccessor) client.inGameHud).setCurrentStack(fakeHeldItem);
	}

	@Inject(method = "close", at = @At("HEAD"))
	void close_head(CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.inGameHud.setOverlayMessage(Text.of(""), false);
		((InGameHudAccessor) client.inGameHud).setHeldItemTooltipFade(0);
		((InGameHudAccessor) client.inGameHud).setCurrentStack(ItemStack.EMPTY);
	}

}
