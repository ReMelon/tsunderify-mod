package remelon.cat.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.DoubleSlider;
import dev.isxander.yacl3.config.v2.api.autogen.TickBox;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class TsunderifyConfig {
    public static final Path CONFIG_PATH = YACLPlatform.getConfigDir().resolve("tsunderify.json");

    public static final ConfigClassHandler<TsunderifyConfig> CONFIG = ConfigClassHandler.createBuilder(TsunderifyConfig.class).id(Identifier.of("tsunderify", "config")).serializer(config -> GsonConfigSerializerBuilder.create(config).setPath(CONFIG_PATH).build()).build();

    @SerialEntry
    @AutoGen(category = "tsunderify", group = "transformer")
    @TickBox
    public boolean modEnabled = true;

    @SerialEntry
    @AutoGen(category = "tsunderify", group = "transformer")
    @TickBox
    public boolean swearReplacement = true;

    @SerialEntry
    @AutoGen(category = "tsunderify", group = "transformer")
    @DoubleSlider(min = 0, max = 100, step = 1)
    public double stutterChance = 50;

    @SerialEntry
    @AutoGen(category = "tsunderify", group = "transformer")
    @DoubleSlider(min = 0, max = 100, step = 1)
    public double suffixChance = 100;

    @SerialEntry
    @AutoGen(category = "tsunderify", group = "keys")
    @TickBox
    public boolean tsunifyOnEnter = false;

    @SerialEntry
    @AutoGen(category = "tsunderify", group = "keys")
    @TickBox
    public boolean specialKeySends = true;

    public static Screen createScreen(@Nullable Screen parent) {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return CONFIG.generateGui().generateScreen(parent);
        } else {
            return null;
        }
    }
}
