package dev.hazel.titlescreenfixer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TitleScreenFixer implements ModInitializer {
	public static final String MOD_ID = "titlescreenfixer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean ESSENTIAL_LOADED = FabricLoader.getInstance().isModLoaded("essential");

    public static final Identifier NOTIFICATION_DOT = Identifier.fromNamespaceAndPath(TitleScreenFixer.MOD_ID, "widget/notification_dot");

	@Override
	public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            writeModMenuConfig();
        }
    }

    private void writeModMenuConfig() {
        Path configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("modmenu.json");

        if (!Files.exists(configPath)) {
            try {
                Files.writeString(configPath, """
                {
                  "mods_button_style": "classic"
                }
                """);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
