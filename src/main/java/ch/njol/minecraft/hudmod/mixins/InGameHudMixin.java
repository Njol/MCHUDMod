package ch.njol.minecraft.hudmod.mixins;

import ch.njol.minecraft.hudmod.HudMod;
import java.awt.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Shadow
	private @Nullable Text overlayMessage;
	@Shadow
	@Final
	private MinecraftClient client;

	@Unique
	private float tickDelta;

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V",
		at = @At(value = "HEAD"))
	void render_head(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		this.tickDelta = tickDelta;
	}

	// TODO XP bar? at least to be able to move it around?
//	@Inject(method = "renderExperienceBar(Lnet/minecraft/client/util/math/MatrixStack;I)V",
//		at = @At(value = "HEAD"), cancellable = true)
//	void renderExperienceBar(MatrixStack matrices, int x, CallbackInfo ci) {
//		if (HudMod.options.hud_enabled) {
//			hud.experience.renderAbsolute(matrices, tickDelta, scaledWidth, scaledHeight);
//			ci.cancel();
//		}
//	}

	// TODO mount jump bar? (in vanilla, it replaces the xp bar)

	@Inject(method = "renderStatusBars(Lnet/minecraft/client/util/math/MatrixStack;)V",
		at = @At(value = "HEAD"), cancellable = true)
	void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
		if (HudMod.options.hud_enabled
			    && HudMod.options.hud_statusBarsEnabled) {
			HudMod.healthBar.renderAbsolute(matrices, tickDelta);
			HudMod.hungerBar.renderAbsolute(matrices, tickDelta);
			HudMod.breathBar.renderAbsolute(matrices, tickDelta);
			// armor is useless in Monumenta, so no HUD element for that // TODO make one anyway? even in vanilla it doesn't really matter though
			ci.cancel();
		}
	}

	@Inject(method = "renderMountHealth(Lnet/minecraft/client/util/math/MatrixStack;)V",
		at = @At(value = "HEAD"), cancellable = true)
	void renderMountHealth(MatrixStack matrices, CallbackInfo ci) {
		if (HudMod.options.hud_enabled
			    && HudMod.options.hud_mountHealthEnabled) {
			HudMod.mountHealthBar.renderAbsolute(matrices, tickDelta);
			ci.cancel();
		}
	}

	@Inject(method = "renderHeldItemTooltip(Lnet/minecraft/client/util/math/MatrixStack;)V",
		at = @At(value = "HEAD"))
	void renderHeldItemTooltip_head(MatrixStack matrices, CallbackInfo ci) {
		if (HudMod.options.hud_enabled
			    && HudMod.options.hud_moveHeldItemTooltip) {
			Rectangle position = HudMod.heldItemTooltip.getDimension();
			// NB: Minecraft will write the text at the following coordinates, so need to subtract that from the translation
			int x = (client.getWindow().getScaledWidth() - position.width) / 2;
			int y = client.getWindow().getScaledHeight() - 59;
			if (!this.client.interactionManager.hasStatusBars()) {
				y += 14;
			}
			matrices.push();
			matrices.translate(position.x - x, position.y - y, 0);
		}
	}

	@Inject(method = "renderHeldItemTooltip(Lnet/minecraft/client/util/math/MatrixStack;)V",
		at = @At(value = "RETURN"))
	void renderHeldItemTooltip_return(MatrixStack matrices, CallbackInfo ci) {
		if (HudMod.options.hud_enabled
			    && HudMod.options.hud_moveHeldItemTooltip) {
			matrices.pop();
		}
	}

	@Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V"),
		slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;overlayRemaining:I", ordinal = 0),
			to = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;titleTotalTicks:I", ordinal = 0)))
	void render_overlayMessage_translate(MatrixStack instance, double x, double y, double z) {
		if (!HudMod.options.hud_enabled
			    || !HudMod.options.hud_moveOverlayMessage) {
			instance.translate(x, y, z);
			return;
		}
		Rectangle position = HudMod.overlayMessage.getDimension();
		// NB: Minecraft will write the text at (-textWidth/2, -4), so need to subtract that from the translation
		instance.translate(position.x + position.width / 2f, position.y + 4, z);
	}

}
