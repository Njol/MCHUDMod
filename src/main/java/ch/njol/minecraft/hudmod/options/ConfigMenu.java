package ch.njol.minecraft.hudmod.options;

import ch.njol.minecraft.config.ClothConfigSetup;
import ch.njol.minecraft.config.ModMenuConfigSetup;
import ch.njol.minecraft.hudmod.HudMod;
import ch.njol.minecraft.uiframework.ElementPosition;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.impl.ConfigEntryBuilderImpl;
import net.minecraft.text.Text;

public class ConfigMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuConfigSetup.getModConfigScreenFactory(HudMod.MOD_IDENTIFIER + ".config", () -> HudMod.options, new Options());
	}

	public static void registerTypes() {
		ClothConfigSetup.registerType(ElementPosition.class, (value, defaultValue, field, translatePath, saveConsumer) -> {
			List<AbstractConfigListEntry> entries = new ArrayList<>(); // API requires raw type
			for (Field posField : ElementPosition.class.getDeclaredFields()) {
				entries.add(ClothConfigSetup.buildConfigEntry(value, defaultValue, posField, HudMod.MOD_IDENTIFIER + ".config.position"));
			}
			return ConfigEntryBuilderImpl.create()
				       .startSubCategory(Text.translatable(translatePath), entries)
				       .build();
		});
	}

}
