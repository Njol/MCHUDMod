package ch.njol.minecraft.hudmod.elements;

import ch.njol.minecraft.hudmod.HudMod;
import ch.njol.minecraft.hudmod.mixins.InGameHudAccessor;
import ch.njol.minecraft.uiframework.ElementPosition;
import ch.njol.minecraft.uiframework.hud.HudElement;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public class HeldItemTooltip extends HudElement {

	public static final Text EDIT_SAMPLE_MESSAGE = Text.of("Held Item's Name");

	public HeldItemTooltip() {
		super();
	}

	@Override
	protected boolean isEnabled() {
		return HudMod.options.hud_enabled && HudMod.options.hud_moveHeldItemTooltip
			       && this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR; // these two are vanilla check
	}

	@Override
	protected boolean isVisible() {
		return false; // only "visible" while editing, but the text is always rendered by (modified) vanilla code
	}

	@Override
	protected int getWidth() {
		ItemStack currentStack = ((InGameHudAccessor) client.inGameHud).getCurrentStack();
		if (currentStack == null || currentStack.isEmpty()) {
			return 0;
		}
		MutableText mutableText = MutableText.of(new LiteralTextContent("")).append(currentStack.getName()).formatted(currentStack.getRarity().formatting);
		if (currentStack.hasCustomName()) {
			mutableText.formatted(Formatting.ITALIC);
		}
		return client.textRenderer.getWidth(mutableText);
	}

	@Override
	protected int getHeight() {
		return client.textRenderer.fontHeight;
	}

	@Override
	protected ElementPosition getPosition() {
		return HudMod.options.hud_heldItemTooltipPosition;
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
