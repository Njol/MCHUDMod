package ch.njol.minecraft.hudmod.options;

import ch.njol.minecraft.config.annotations.Category;
import ch.njol.minecraft.config.annotations.DescriptionLine;
import ch.njol.minecraft.hudmod.HudMod;
import ch.njol.minecraft.uiframework.ElementPosition;

public class Options implements ch.njol.minecraft.config.Options {

	public enum AbsorptionStart {
		HEALTH_START, HEALTH_END, NEW_BAR;
	}

	@Category("hud")
	public DescriptionLine hud_info;
	@Category("hud")
	public boolean hud_enabled = true;

	@Category("hud")
	public boolean hud_statusBarsEnabled = true;
	@Category("hud")
	public float hud_regenerationSpeed = 10;
	@Category("hud")
	public boolean hud_healthText = true;
	@Category("hud")
	public int hud_healthTextOffset = -5;
	@Category("hud")
	public int hud_absorptionMaxBars = 5;
	@Category("hud")
	public int hud_absorptionBarsOffset = -3;
	@Category("hud")
	public AbsorptionStart hud_absorptionStart = AbsorptionStart.HEALTH_END;
	@Category("hud")
	public boolean hud_healthMirror = false;
	@Category("hud")
	public boolean hud_hungerMirror = false;
	@Category("hud")
	public boolean hud_breathMirror = true;
	@Category("hud")
	public boolean hud_hideBreathWithWaterBreathing = false;
	@Category("hud")
	public boolean hud_mountHealthEnabled = true;
	@Category("hud")
	public boolean hud_mountHealthText = true;
	@Category("hud")
	public int hud_mountHealthTextOffset = -5;
	@Category("hud")
	public boolean hud_mountHealthMirror = false;
	@Category("hud")
	public boolean hud_moveOverlayMessage = true;
	@Category("hud")
	public boolean hud_moveHeldItemTooltip = true;
	@Category("hud")
	public DescriptionLine hud_positionsInfo;
	@Category("hud")
	public ElementPosition hud_healthBarPosition = new ElementPosition(0.5f, 0, 1.0f, -39, 0.5f, 1.0f);
	@Category("hud")
	public ElementPosition hud_hungerBarPosition = new ElementPosition(0.5f, 59, 1.0f, -23, 0.5f, 1.0f);
	@Category("hud")
	public ElementPosition hud_breathBarPosition = new ElementPosition(0.5f, -60, 1.0f, -23, 0.5f, 1.0f);
	@Category("hud")
	public ElementPosition hud_mountHealthBarPosition = new ElementPosition(0.5f, 60, 1.0f, -55, 0.5f, 1.0f);
	@Category("hud")
	public ElementPosition hud_overlayMessagePosition = new ElementPosition(0.5f, 0, 1.0f, -81, 0.5f, 1.0f);
	@Category("hud")
	public ElementPosition hud_heldItemTooltipPosition = new ElementPosition(0.5f, 0, 1.0f, -36, 0.5f, 1.0f);

	public void onUpdate() {
		HudMod.saveConfig();
	}

}
