package ch.njol.minecraft.hudmod.elements;

import ch.njol.minecraft.hudmod.HudMod;
import ch.njol.minecraft.hudmod.options.Options;
import ch.njol.minecraft.uiframework.ElementPosition;
import ch.njol.minecraft.uiframework.ModSpriteAtlasHolder;
import ch.njol.minecraft.uiframework.Utils;
import ch.njol.minecraft.uiframework.hud.HudElement;
import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class HealthBar extends HudElement {

	private static final int HEIGHT = 32;
	private static final int MARGIN = 16;

	private static Identifier BACKGROUND, OVERLAY;
	private static Identifier HEALTH_LEFT, HEALTH, HEALTH_RIGHT, HIT_HEALTH, HIT_HEALTH_RIGHT;
	private static Identifier ABSORPTION_LEFT, ABSORPTION, ABSORPTION_RIGHT, // in health bar, after health
		ABSORPTION_OVERLAY_LEFT, ABSORPTION_OVERLAY, ABSORPTION_OVERLAY_RIGHT, // in health bar, over health
		ABSORPTION_BAR_LEFT, ABSORPTION_BAR, ABSORPTION_BAR_RIGHT; // separate bar // TODO maybe different texture for first bar to be able to make a nice connection to health bar
	private static Identifier POISON_LEFT, POISON, POISON_RIGHT, HIT_POISON, HIT_POISON_RIGHT;
	private static Identifier REGENERATION;
	private static Identifier WITHER;

	private static final DecimalFormat SINGLE_DIGIT = new DecimalFormat("0.0");
	private static final DecimalFormat OPTIONAL_SINGLE_DIGIT = new DecimalFormat("0.#");

	public HealthBar() {
		super();
	}

	public static void registerSprites(ModSpriteAtlasHolder atlas) {
		BACKGROUND = atlas.registerSprite("health/background");
		OVERLAY = atlas.registerSprite("health/overlay");
		HEALTH_LEFT = atlas.registerSprite("health/health_left");
		HEALTH = atlas.registerSprite("health/health");
		HEALTH_RIGHT = atlas.registerSprite("health/health_right");
		HIT_HEALTH = atlas.registerSprite("health/health_damage");
		HIT_HEALTH_RIGHT = atlas.registerSprite("health/health_damage_right");
		POISON_LEFT = atlas.registerSprite("health/poison_left");
		POISON = atlas.registerSprite("health/poison");
		POISON_RIGHT = atlas.registerSprite("health/poison_right");
		HIT_POISON = atlas.registerSprite("health/poison_damage");
		HIT_POISON_RIGHT = atlas.registerSprite("health/poison_damage_right");
		ABSORPTION_LEFT = atlas.registerSprite("health/absorption_left");
		ABSORPTION = atlas.registerSprite("health/absorption");
		ABSORPTION_RIGHT = atlas.registerSprite("health/absorption_right");
		ABSORPTION_OVERLAY_LEFT = atlas.registerSprite("health/absorption_overlay_left");
		ABSORPTION_OVERLAY = atlas.registerSprite("health/absorption_overlay");
		ABSORPTION_OVERLAY_RIGHT = atlas.registerSprite("health/absorption_overlay_right");
		ABSORPTION_BAR_LEFT = atlas.registerSprite("health/absorption_bar_left");
		ABSORPTION_BAR = atlas.registerSprite("health/absorption_bar");
		ABSORPTION_BAR_RIGHT = atlas.registerSprite("health/absorption_bar_right");
		REGENERATION = atlas.registerSprite("health/regeneration");
		WITHER = atlas.registerSprite("health/wither");
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
		Sprite healthSprite = HudMod.HUD_ATLAS.getSprite(HEALTH);
		return 2 * MARGIN + (int) (1.0 * healthSprite.getContents().getWidth() * HEIGHT / healthSprite.getContents().getHeight());
	}

	@Override
	protected int getHeight() {
		return HEIGHT;
	}

	@Override
	protected ElementPosition getPosition() {
		return HudMod.options.hud_healthBarPosition;
	}

	@Override
	protected int getZOffset() {
		return 1;
	}

	private PlayerEntity getCameraPlayer() {
		return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity) this.client.getCameraEntity();
	}

	private float easedHealth = -1;
	private float easedAbsorption = -1;
	private float regenProgress = 0;

	@Override
	protected void render(MatrixStack matrices, float tickDelta) {

		PlayerEntity player = getCameraPlayer();
		if (player == null) {
			return;
		}

		int width = getWidth();
		int healthWidth = width - 2 * MARGIN;

		Options options = HudMod.options;

		if (options.hud_healthMirror) {
			matrices.push();
			matrices.translate(width, 0, 0);
			matrices.scale(-1, 1, 1);
			RenderSystem.disableCull();
		}

		float maxHealth = Math.max(1.0f, player.getMaxHealth());
		float health = Utils.clamp(0, player.getHealth(), maxHealth);
		float absorption = Math.max(0, player.getAbsorptionAmount());

		float lastFrameDuration = client.getLastFrameDuration() / 20;
		easedHealth = Utils.clamp(0, easedHealth <= 0 ? health : Utils.ease(health, easedHealth, 6 * lastFrameDuration, maxHealth / 3 * lastFrameDuration), maxHealth);
		easedAbsorption = Math.max(0, easedAbsorption < 0 ? absorption : Utils.ease(absorption, easedAbsorption, 6 * lastFrameDuration, maxHealth / 3 * lastFrameDuration));

		float healthFactor = easedHealth / maxHealth;
		float absorptionStartFactor = switch (options.hud_absorptionStart) {
			case HEALTH_START -> 0.0F;
			case HEALTH_END -> healthFactor;
			case NEW_BAR -> 1.0F;
		};
		float absorptionEndFactor = Math.min(absorptionStartFactor + easedAbsorption / maxHealth, options.hud_absorptionMaxBars + 1);
		// draw extra absorption bars
		if (absorptionEndFactor > 1) {
			int extraBars = (int) Math.ceil(absorptionEndFactor) - 1;
			for (int i = 0; i < extraBars; i++) {
				float barEnd = i == 0 ? absorptionEndFactor % 1 : 1;
				if (barEnd == 0) {
					barEnd = 1;
				}
				int y = (extraBars - i - 1) * options.hud_absorptionBarsOffset;
				// TODO absorption_damage
				drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION_BAR), MARGIN, y, healthWidth, HEIGHT, 0, 0, barEnd, 1);
				drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION_BAR_LEFT), 0, y, 2 * MARGIN, HEIGHT);
				drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION_BAR_RIGHT), healthWidth * barEnd, y, 2 * MARGIN, HEIGHT);
			}
			absorptionEndFactor = 1;
		}

		drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(BACKGROUND), 0, 0, width, HEIGHT);

		if (health > 0 || easedHealth > 0) {
			boolean poisoned = player.hasStatusEffect(StatusEffects.POISON);
			drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(poisoned ? POISON : HEALTH), MARGIN, 0, healthWidth, HEIGHT, 0, 0, Math.min(easedHealth, health) / maxHealth, 1);
			if (easedHealth > health) {
				drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(poisoned ? HIT_POISON : HIT_HEALTH), MARGIN, 0, healthWidth, HEIGHT, health / maxHealth, 0, healthFactor, 1);
				drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(poisoned ? HIT_POISON_RIGHT : HIT_HEALTH_RIGHT), healthWidth * healthFactor, 0, 2 * MARGIN, HEIGHT);
			}
			drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(poisoned ? POISON_LEFT : HEALTH_LEFT), 0, 0, 2 * MARGIN, HEIGHT);
			drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(poisoned ? POISON_RIGHT : HEALTH_RIGHT), healthWidth * (Math.min(easedHealth, health) / maxHealth), 0, 2 * MARGIN, HEIGHT);
		}

		StatusEffectInstance regeneration = player.getStatusEffect(StatusEffects.REGENERATION);
		if (regeneration != null) {
			regenProgress += options.hud_regenerationSpeed * lastFrameDuration * (regeneration.getAmplifier() + 1);
			if (regenProgress > healthWidth) {
				regenProgress -= healthWidth;
			} else if (regenProgress < 0) {
				regenProgress += healthWidth;
			}
			Sprite regenSprite = HudMod.HUD_ATLAS.getSprite(REGENERATION);
			drawPartialSprite(matrices, regenSprite, MARGIN + regenProgress, 0, healthWidth, HEIGHT, 0, 0, Math.min(easedHealth, health) / maxHealth - regenProgress / healthWidth, 1);
			drawPartialSprite(matrices, regenSprite, MARGIN + regenProgress - healthWidth, 0, healthWidth, HEIGHT, 1 - regenProgress / healthWidth, 0, Math.min(1, Math.min(easedHealth, health) / maxHealth + 1 - regenProgress / healthWidth), 1);
		}

		if (absorptionStartFactor < absorptionEndFactor) {
			if (absorptionStartFactor < healthFactor) { // draw absorption overlay (plus potential absorption in health bar)
				drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION_OVERLAY), MARGIN, 0, healthWidth, HEIGHT, 0, 0, Math.min(absorptionEndFactor, healthFactor), 1);
				if (healthFactor < absorptionEndFactor) {
					drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION), MARGIN, 0, healthWidth, HEIGHT, healthFactor, 0, absorptionEndFactor, 1);
				}
				drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION_OVERLAY_LEFT), 0, 0, 2 * MARGIN, HEIGHT);
				drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(absorptionEndFactor <= healthFactor ? ABSORPTION_OVERLAY_RIGHT : ABSORPTION_RIGHT), healthWidth * absorptionEndFactor, 0, 2 * MARGIN, HEIGHT);
			} else { // draw absorption in health bar
				drawPartialSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION), MARGIN, 0, healthWidth, HEIGHT, absorptionStartFactor, 0, absorptionEndFactor, 1);
				drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION_LEFT), healthWidth * absorptionStartFactor, 0, 2 * MARGIN, HEIGHT);
				drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(ABSORPTION_RIGHT), healthWidth * absorptionEndFactor, 0, 2 * MARGIN, HEIGHT);
			}
		}

		drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(OVERLAY), 0, 0, width, HEIGHT);

		if (player.hasStatusEffect(StatusEffects.WITHER)) {
			drawSprite(matrices, HudMod.HUD_ATLAS.getSprite(WITHER), 0, 0, width, HEIGHT);
		}

		if (options.hud_healthMirror) {
			matrices.pop();
			RenderSystem.enableCull();
		}

		if (options.hud_healthText) {
			// TODO text options: no text at all, no max health, absorption added to current health, current as % (with or without absorption), maybe even move max health to the side
			String fullText = OPTIONAL_SINGLE_DIGIT.format(health) + (absorption <= 0 ? "" : " + " + OPTIONAL_SINGLE_DIGIT.format(absorption));
			drawOutlinedText(matrices, fullText, width / 2 - client.textRenderer.getWidth(fullText) / 2, HEIGHT / 2 - client.textRenderer.fontHeight / 2 + options.hud_healthTextOffset, 0xFFFFFFFF);
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
