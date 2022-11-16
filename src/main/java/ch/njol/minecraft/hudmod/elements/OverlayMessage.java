package ch.njol.minecraft.hudmod.elements;

import ch.njol.minecraft.hudmod.HudMod;
import ch.njol.minecraft.hudmod.mixins.InGameHudAccessor;
import ch.njol.minecraft.uiframework.ElementPosition;
import ch.njol.minecraft.uiframework.hud.HudElement;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class OverlayMessage extends HudElement {

	public static String EDIT_SAMPLE_MESSAGE = "Messages appear here!";

	public OverlayMessage() {
		super();
	}

	@Override
	protected boolean isEnabled() {
		return HudMod.options.hud_enabled && HudMod.options.hud_moveOverlayMessage;
	}

	@Override
	protected boolean isVisible() {
		return false; // only "visible" while editing, but the text is always rendered by (modified) vanilla code
	}

	@Override
	protected int getWidth() {
		Text overlayMessage = ((InGameHudAccessor) client.inGameHud).getOverlayMessage();
		return overlayMessage == null ? 0 : client.textRenderer.getWidth(overlayMessage);
	}

	@Override
	protected int getHeight() {
		return client.textRenderer.fontHeight;
	}

	@Override
	protected ElementPosition getPosition() {
		return HudMod.options.hud_overlayMessagePosition;
	}

	@Override
	protected int getZOffset() {
		return 0;
	}

	@Override
	protected void render(MatrixStack matrices, float tickDelta) {
		// nothing to do - rendered by (modified) vanilla code
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (dragging) {
			HudMod.saveConfig();
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

}
