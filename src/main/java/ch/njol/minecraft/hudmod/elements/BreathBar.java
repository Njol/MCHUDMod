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
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;

public class BreathBar extends HudElement {

	private static final int HEIGHT = 32;
	private static final int MARGIN = 16;

	private static Identifier BACKGROUND, BREATH, WATER_BREATHING, OVERLAY;
	private static NavigableMap<Integer, Identifier> LEVEL_OVERLAYS;

	public BreathBar() {
		super();
	}

	public static void registerSprites(ModSpriteAtlasHolder atlas) {
		BACKGROUND = atlas.registerSprite("breath/background");
		BREATH = atlas.registerSprite("breath/breath");
		WATER_BREATHING = atlas.registerSprite("breath/water_breathing");
		OVERLAY = atlas.registerSprite("breath/overlay");
		LEVEL_OVERLAYS = HudMod.findLevelledSprites(atlas, "breath", "overlay_");
	}

	@Override
	protected boolean isEnabled() {
		return HudMod.options.hud_enabled && HudMod.options.hud_statusBarsEnabled;
	}

	@Override
	protected boolean isVisible() {
		PlayerEntity player = getCameraPlayer();
		return player != null && (player.getAir() < player.getMaxAir()
			                          || player.isSubmergedIn(FluidTags.WATER) && (!HudMod.options.hud_hideBreathWithWaterBreathing || !player.hasStatusEffect(StatusEffects.WATER_BREATHING)));
	}

	@Override
	protected int getWidth() {
		Sprite barSprite = HudMod.HUD_ATLAS.getSprite(BREATH);
		return 2 * MARGIN + (int) (1.0 * barSprite.getContents().getWidth() * HEIGHT / barSprite.getContents().getHeight());
	}

	@Override
	protected int getHeight() {
		return HEIGHT;
	}

	@Override
	protected ElementPosition getPosition() {
		return HudMod.options.hud_breathBarPosition;
	}

	@Override
	protected int getZOffset() {
		return 1;
	}

	private PlayerEntity getCameraPlayer() {
		return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity) this.client.getCameraEntity();
	}

	private float easedAir = -1;

	@Override
	protected void render(MatrixStack matrices, float tickDelta) {

		PlayerEntity player = getCameraPlayer();
		if (player == null) {
			return;
		}

		int width = getWidth();
		int barWidth = width - 2 * MARGIN;

		if (HudMod.options.hud_breathMirror) {
			matrices.push();
			matrices.translate(width, 0, 0);
			matrices.scale(-1, 1, 1);
			RenderSystem.disableCull();
		}

		drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(BACKGROUND), 0, 0, width, HEIGHT);

		int air = Utils.clamp(0, player.getAir(), player.getMaxAir());
		float lastFrameDuration = client.getLastFrameDuration() / 20;
		easedAir = Utils.clamp(0, easedAir < 0 ? air : Utils.ease(air, easedAir, 6 * lastFrameDuration, 6 * lastFrameDuration), player.getMaxAir());

		boolean hasWaterBreathing = player.hasStatusEffect(StatusEffects.WATER_BREATHING);
		drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(hasWaterBreathing ? WATER_BREATHING : BREATH),
			MARGIN, 0, barWidth, HEIGHT,
			0, 0, 1f * air / player.getMaxAir(), 1);

		drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(OVERLAY), 0, 0, width, HEIGHT);

		Map.Entry<Integer, Identifier> levelOverlay = LEVEL_OVERLAYS.floorEntry(air);
		if (levelOverlay != null) {
			drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(levelOverlay.getValue()), 0, 0, width, HEIGHT);
		}

		if (HudMod.options.hud_breathMirror) {
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
