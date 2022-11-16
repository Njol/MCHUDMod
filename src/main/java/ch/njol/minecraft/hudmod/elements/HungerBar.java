package ch.njol.minecraft.hudmod.elements;

import ch.njol.minecraft.hudmod.HudMod;
import ch.njol.minecraft.uiframework.ElementPosition;
import ch.njol.minecraft.uiframework.ModSpriteAtlasHolder;
import ch.njol.minecraft.uiframework.Utils;
import ch.njol.minecraft.uiframework.hud.HudElement;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.NavigableMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class HungerBar extends HudElement {

	private static final int HEIGHT = 32;
	private static final int MARGIN = 16;

	private static Identifier BACKGROUND, BAR_HUNGER, BAR_DECAY, BAR_SATURATION,
		OVERLAY, OVERLAY_DECAY, OVERLAY_SATURATION;
	private static NavigableMap<Integer, Identifier> LEVEL_OVERLAYS, LEVEL_OVERLAYS_DECAY;

	public HungerBar() {
		super();
	}

	public static void registerSprites(ModSpriteAtlasHolder atlas) {
		BACKGROUND = atlas.registerSprite("hunger/background");
		BAR_HUNGER = atlas.registerSprite("hunger/hunger");
		BAR_DECAY = atlas.registerSprite("hunger/decay");
		BAR_SATURATION = atlas.registerSprite("hunger/saturation");
		OVERLAY = atlas.registerSprite("hunger/overlay");
		OVERLAY_DECAY = atlas.registerSprite("hunger/overlay_decay");
		OVERLAY_SATURATION = atlas.registerSprite("hunger/overlay_saturation");
		LEVEL_OVERLAYS = HudMod.findLevelledSprites(atlas, "hunger", "overlay_");
		LEVEL_OVERLAYS_DECAY = HudMod.findLevelledSprites(atlas, "hunger", "overlay_decay_");
	}

	@Override
	protected boolean isEnabled() {
		return HudMod.options.hud_enabled && HudMod.options.hud_statusBarsEnabled;
	}

	@Override
	protected boolean isVisible() {
		return true;
	}

	@Override
	protected int getWidth() {
		Sprite barSprite = HudMod.HUD_ATLAS.getSprite(BAR_HUNGER);
		return 2 * MARGIN + (int) (1.0 * barSprite.getWidth() * HEIGHT / barSprite.getHeight());
	}

	@Override
	protected int getHeight() {
		return HEIGHT;
	}

	@Override
	protected ElementPosition getPosition() {
		return HudMod.options.hud_hungerBarPosition;
	}

	@Override
	protected int getZOffset() {
		return 1;
	}

	private PlayerEntity getCameraPlayer() {
		return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity) this.client.getCameraEntity();
	}

	private float easedHunger = -1;
	private float easedSaturation = -1;


	@Override
	protected void render(MatrixStack matrices, float tickDelta) {

		PlayerEntity player = getCameraPlayer();
		if (player == null) {
			return;
		}

		int width = getWidth();
		int barWidth = width - 2 * MARGIN;

		if (HudMod.options.hud_hungerMirror) {
			matrices.push();
			matrices.translate(width, 0, 0);
			matrices.scale(-1, 1, 1);
			RenderSystem.disableCull();
		}

		drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(BACKGROUND), 0, 0, width, HEIGHT);

		int hunger = Utils.clamp(0, player.getHungerManager().getFoodLevel(), 20);
		float saturation = Utils.clamp(0, player.getHungerManager().getSaturationLevel(), 20);
		float lastFrameDuration = client.getLastFrameDuration() / 20;
		easedHunger = Utils.clamp(0, easedHunger < 0 ? hunger : Utils.ease(hunger, easedHunger, 6 * lastFrameDuration, 6 * lastFrameDuration), 20);
		easedSaturation = Utils.clamp(0, easedSaturation < 0 ? saturation : Utils.ease(saturation, easedSaturation, 6 * lastFrameDuration, 6 * lastFrameDuration), 20);

		boolean hasDecay = player.hasStatusEffect(StatusEffects.HUNGER);
		drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(hasDecay ? BAR_DECAY : BAR_HUNGER),
			MARGIN, 0, barWidth, HEIGHT,
			0, 0, easedHunger / 20, 1);
		drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(BAR_SATURATION),
			MARGIN, 0, barWidth, HEIGHT,
			0, 0, easedSaturation / 20, 1);

		drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(hasDecay ? OVERLAY_DECAY : OVERLAY), 0, 0, width, HEIGHT);

		Map.Entry<Integer, Identifier> levelOverlay = (hasDecay ? LEVEL_OVERLAYS_DECAY : LEVEL_OVERLAYS).floorEntry(hunger);
		if (levelOverlay != null) {
			drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(levelOverlay.getValue()), 0, 0, width, HEIGHT);
		}

		if (easedSaturation > 0) {
			drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(OVERLAY_SATURATION), 0, 0, width, HEIGHT);
		}

//		// TODO text options: no text at all, with saturation, current as % (with or without saturation)
//		String fullText = "" + hunger;
//		drawOutlinedText(matrices, fullText, width / 2 - client.textRenderer.getWidth(fullText) / 2, HEIGHT / 2 - client.textRenderer.fontHeight / 2 + HudMod.options.hud_hungerTextOffset, 0xFFFFFFFF);

		if (HudMod.options.hud_hungerMirror) {
			matrices.pop();
			RenderSystem.enableCull();
		}
	}

	@Override
	protected boolean isClickable(double mouseX, double mouseY) {
		return !isPixelTransparent(HudMod.HUD_ATLAS.getSprite(BACKGROUND), mouseX / getWidth(), mouseY / getHeight())
			       || !isPixelTransparent(HudMod.HUD_ATLAS.getSprite(OVERLAY), mouseX / getWidth(), mouseY / getHeight());
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (dragging) {
			HudMod.saveConfig();
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

}
