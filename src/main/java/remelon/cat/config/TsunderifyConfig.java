package remelon.cat.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TsunderifyConfig {
    public static final Path CONFIG_PATH = YACLPlatform.getConfigDir().resolve("tsunderify.json");

    public static final ConfigClassHandler<TsunderifyConfig> CONFIG = ConfigClassHandler.createBuilder(TsunderifyConfig.class)
            .id(Identifier.fromNamespaceAndPath("tsunderify", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    public static final String USER_TOKEN = "{user}";

    @SerialEntry
    public boolean modEnabled = true;

    @SerialEntry
    public boolean swearReplacement = true;

    @SerialEntry
    public double stutterChance = 50;

    @SerialEntry
    public double suffixChance = 100;

    @SerialEntry
    public boolean tsunifyOnEnter = false;

    @SerialEntry
    public boolean specialKeySends = true;

    @SerialEntry
    public boolean transformOtherMods = false;

    @SerialEntry
    public List<String> commands = new ArrayList<>(defaultCommands());

    private static List<String> defaultCommands() {
        return List.of(
                "^/r\\s+",
                "^/reply\\s+",
                "^/me\\s+",
                "^/pc\\s+",
                "^/pchat\\s+",
                "^/party chat\\s+",
                "^/gc\\s+",
                "^/gchat\\s+",
                "^/guild chat\\s+",
                "^/oc\\s+",
                "^/ochat\\s+",
                "^/officer chat\\s+",
                "^/cc\\s+",
                "^/cchat\\s+",
                "^/coopchat\\s+",
                "^/achat\\s+",
                "^/ac\\s+",
                "^/tellraw\\s+",
                "^/say(?:\\s+|$)",
                "^/msg\\s+\\S+\\s+",
                "^/w\\s+\\S+\\s+",
                "^/message\\s+\\S+\\s+",
                "^/whisper\\s+\\S+\\s+",
                "^/tell\\s+\\S+\\s+"
        );
    }

    public static Screen createScreen(@Nullable Screen parent) {
        if (!FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return null;
        }

        TsunderifyConfig config = CONFIG.instance();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.tsunderify:config.category.tsunderify"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("yacl3.config.tsunderify:config.category.tsunderify"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("yacl3.config.tsunderify:config.category.tsunderify.group.transformer"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("yacl3.config.tsunderify:config.modEnabled"))
                                        .binding(true, () -> config.modEnabled, value -> config.modEnabled = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("yacl3.config.tsunderify:config.swearReplacement"))
                                        .description(OptionDescription.of(Component.translatable("yacl3.config.tsunderify:config.swearReplacement.desc")))
                                        .binding(true, () -> config.swearReplacement, value -> config.swearReplacement = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("yacl3.config.tsunderify:config.suffixChance"))
                                        .description(OptionDescription.of(Component.translatable("yacl3.config.tsunderify:config.suffixChance.desc")))
                                        .binding(100d, () -> config.suffixChance, value -> config.suffixChance = value)
                                        .controller(option -> dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder.create(option)
                                                .range(0d, 100d)
                                                .step(1d))
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("yacl3.config.tsunderify:config.stutterChance"))
                                        .description(OptionDescription.of(Component.translatable("yacl3.config.tsunderify:config.stutterChance.desc")))
                                        .binding(50d, () -> config.stutterChance, value -> config.stutterChance = value)
                                        .controller(option -> dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder.create(option)
                                                .range(0d, 100d)
                                                .step(1d))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("yacl3.config.tsunderify:config.transformOtherMods"))
                                        .description(OptionDescription.of(Component.translatable("yacl3.config.tsunderify:config.transformOtherMods.desc")))
                                        .binding(false, () -> config.transformOtherMods, value -> config.transformOtherMods = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("yacl3.config.tsunderify:config.category.tsunderify.group.keys"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("yacl3.config.tsunderify:config.tsunifyOnEnter"))
                                        .description(OptionDescription.of(Component.translatable("yacl3.config.tsunderify:config.tsunifyOnEnter.desc")))
                                        .binding(false, () -> config.tsunifyOnEnter, value -> config.tsunifyOnEnter = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("yacl3.config.tsunderify:config.specialKeySends"))
                                        .description(OptionDescription.of(Component.translatable("yacl3.config.tsunderify:config.specialKeySends.desc")))
                                        .binding(true, () -> config.specialKeySends, value -> config.specialKeySends = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("yacl3.config.tsunderify:config.category.tsunderify.group.advanced"))
                        .option(ListOption.<String>createBuilder()
                                .name(Component.translatable("yacl3.config.tsunderify:config.commands"))
                                .description(OptionDescription.of(Component.translatable("yacl3.config.tsunderify:config.commands.desc")))
                                .binding(new ArrayList<>(defaultCommands()), () -> config.commands, value -> config.commands = new ArrayList<>(value))
                                .controller(StringControllerBuilder::create)
                                .initial("^/r\\s+")
                                .build())
                        .build())
                .save(CONFIG.serializer()::save)
                .build()
                .generateScreen(parent);
    }
}