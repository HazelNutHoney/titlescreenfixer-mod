package dev.hazel.titlescreenfixer;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;

@Mod(TitlescreenFixer.MOD_ID)
public class TitlescreenFixer {
    public static final String MOD_ID = "titlescreenfixer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean ESSENTIAL_LOADED = ModList.get().isLoaded("essential");
    public static final Identifier NOTIFICATION_DOT = Identifier.fromNamespaceAndPath( MOD_ID, "widget/notification_dot" );

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); } }