package lol.sylvie.overture.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingListController;
import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.config.type.BackendTypeAdapter;
import lol.sylvie.overture.util.Anchor;
import lol.sylvie.overture.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Configuration {
    public static ConfigClassHandler<Configuration> HANDLER = ConfigClassHandler.createBuilder(Configuration.class)
            .id(Constants.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("overture.json5"))
                    .setJson5(true)
                    .appendGsonBuilder(builder -> {
                        builder.registerTypeAdapter(MetadataRetriever.class, new BackendTypeAdapter());
                        return builder;
                    })
                    .build())
            .build();

    // General settings
    // Retrieval
    @SerialEntry(comment = "The method in which Overture will get the currently playing track.")
    public MetadataRetriever metadataRetriever = RetrievalHandler.getUsableRetriever();

    @SerialEntry(comment = "How often the currently playing track should be updated (in seconds)")
    public float interval = 5f;

    @SerialEntry(comment = "Only check on the above interval when the window is unfocused, otherwise use the music duration to check after each track is done.")
    public boolean smartChecking = true;

    // Appearance Settings
    // Colors
    @SerialEntry(comment = "Background color of the HUD")
    public Color background = new Color(0, 0, 0, 127);

    @SerialEntry(comment = "Title text color")
    public Color title = new Color(255, 255, 255, 255);

    @SerialEntry(comment = "Artist text color")
    public Color artist = new Color(170, 170, 170, 255);

    @SerialEntry(comment = "Current progress text color")
    public Color progress = new Color(255, 255, 255, 255);

    @SerialEntry(comment = "Track duration text color")
    public Color duration = new Color(255, 255, 255, 255);

    @SerialEntry(comment = "Progress bar background color")
    public Color progressBackground = new Color(255, 255, 255, 255);

    @SerialEntry(comment = "Progress bar foreground color")
    public Color progressForeground = new Color(29, 185, 84, 255);

    private static void colorSetting(String name, OptionGroup.Builder builder, @NotNull Color def, @NotNull Supplier<Color> getter, Consumer<Color> setter) {
        builder.option(Option.<Color>createBuilder()
                .name(Component.translatable("config.overture.appearance.colors." + name))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.colors." + name + ".description")))
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .binding(def, getter, setter)
                .build());
    }

    // Positioning
    @SerialEntry(comment = "Where the horizontal position should be based on")
    public Anchor xAnchor = Anchor.TOP;

    @SerialEntry(comment = "Where the vertical position should be based on")
    public Anchor yAnchor = Anchor.TOP;

    @SerialEntry(comment = "How many pixels the HUD should be shifted horizontally")
    public int xOffset = 2;

    @SerialEntry(comment = "How many pixels the HUD should be shifted vertically")
    public int yOffset = 2;

    @SerialEntry(comment = "The maximum width of the HUD element (0 = auto)")
    public int width = 196;

    @SerialEntry(comment = "Scaling factor for the entire HUD")
    public float scale = 1f;

    @SerialEntry(comment = "How many pixels should surround each side of the HUD")
    public int padding = 4;

    // Tweaks
    @SerialEntry(comment = "Prefer the album name to the artist name in the secondary text field")
    public boolean preferAlbumName = false;

    @SerialEntry(comment = "Which side the track image should appear on")
    public boolean imageOnRight = false;

    @SerialEntry(comment = "Reverses the vanilla GUI scale")
    public boolean ignoreGuiScale = false;

    private static final DecimalFormat SECONDS = new DecimalFormat("0.0s");
    public Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("overture.name"));

        // General settings
        ConfigCategory.Builder generalCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("config.overture.general"))
                .tooltip(Component.translatable("config.overture.general.tooltip"));

        // Backend settings
        OptionGroup.Builder retrievalSettings = OptionGroup.createBuilder()
                .name(Component.translatable("config.overture.general.retrieval"))
                .description(OptionDescription.of(Component.translatable("config.overture.general.retrieval.description")));

        // Metadata Retriever
        retrievalSettings.option(Option.<MetadataRetriever>createBuilder()
                .name(Component.translatable("config.overture.general.retrieval.backend"))
                .customController(option -> new CyclingListController<>(option, RetrievalHandler.getBackends().values(), backend -> backend.getType().getTitle()))
                .description(backend -> OptionDescription.of(backend.getType().getDescription()))
                .binding(this.metadataRetriever, () -> this.metadataRetriever, (m) -> this.metadataRetriever = m)
                .build());

        // Retrieval Interval
        retrievalSettings.option(Option.<Float>createBuilder()
                .name(Component.translatable("config.overture.general.retrieval.interval"))
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(1f, 60f)
                        .formatValue(value -> Component.literal(SECONDS.format(value)))
                        .step(0.1f))
                .description(OptionDescription.of(Component.translatable("config.overture.general.retrieval.interval.description")))
                .binding(this.interval, () -> this.interval, (m) -> this.interval = m)
                .build());

        // Smart Checking
        retrievalSettings.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.overture.general.retrieval.smart_checking"))
                .controller(TickBoxControllerBuilder::create)
                .description(OptionDescription.of(Component.translatable("config.overture.general.retrieval.smart_checking.description")))
                .binding(this.smartChecking, () -> this.smartChecking, (v) -> this.smartChecking = v)
                .build());

        generalCategory.group(retrievalSettings.build());

        builder.category(generalCategory.build());

        // Appearance settings
        ConfigCategory.Builder appearanceCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("config.overture.appearance"))
                .tooltip(Component.translatable("config.overture.appearance.tooltip"));

        // Colors
        OptionGroup.Builder colorSettings = OptionGroup.createBuilder()
                .name(Component.translatable("config.overture.appearance.colors"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.colors.description")));

        colorSetting("background", colorSettings, this.background, () -> this.background, (c) -> this.background = c);
        colorSetting("title", colorSettings, this.title, () -> this.title, (c) -> this.title = c);
        colorSetting("artist", colorSettings, this.artist, () -> this.artist, (c) -> this.artist = c);
        colorSetting("progress", colorSettings, this.progress, () -> this.progress, (c) -> this.progress = c);
        colorSetting("duration", colorSettings, this.duration, () -> this.duration, (c) -> this.duration = c);
        colorSetting("progress_background", colorSettings, this.progressBackground, () -> this.progressBackground, (c) -> this.progressBackground = c);
        colorSetting("progress_foreground", colorSettings, this.progressForeground, () -> this.progressForeground, (c) -> this.progressForeground = c);

        appearanceCategory.group(colorSettings.build());

        // Positioning
        OptionGroup.Builder positionSettings = OptionGroup.createBuilder()
                .name(Component.translatable("config.overture.appearance.position"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.description")));

        positionSettings.option(Option.<Anchor>createBuilder()
                .name(Component.translatable("config.overture.appearance.position.x_anchor"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.x_anchor.description")))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(Anchor.class)
                        .formatValue(Anchor::getHorizontalName))
                .binding(this.xAnchor, () -> this.xAnchor, (a) -> this.xAnchor = a)
                .build());

        positionSettings.option(Option.<Anchor>createBuilder()
                .name(Component.translatable("config.overture.appearance.position.y_anchor"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.y_anchor.description")))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(Anchor.class)
                        .formatValue(Anchor::getVerticalName))
                .binding(this.yAnchor, () -> this.yAnchor, (a) -> this.yAnchor = a)
                .build());

        ValueFormatter<Integer> pixelFormatter = value -> Component.literal(value + "px");
        positionSettings.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.overture.appearance.position.x_offset"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.x_offset.description")))
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).formatValue(pixelFormatter).range(-20, 20))
                .binding(this.xOffset, () -> this.xOffset, (a) -> this.xOffset = a)
                .build());

        positionSettings.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.overture.appearance.position.y_offset"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.y_offset.description")))
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).formatValue(pixelFormatter).range(-20, 20))
                .binding(this.yOffset, () -> this.yOffset, (a) -> this.yOffset = a)
                .build());

        positionSettings.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.overture.appearance.position.width"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.width.description")))
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).formatValue(pixelFormatter).min(32))
                .binding(this.width, () -> this.width, (a) -> this.width = a)
                .build());


        ValueFormatter<Float> percentFormatter = value -> Component.literal(NumberFormat.getPercentInstance().format(value));
        positionSettings.option(Option.<Float>createBuilder()
                .name(Component.translatable("config.overture.appearance.position.scale"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.scale.description")))
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.25f, 5f).step(0.05f).formatValue(percentFormatter))
                .binding(this.scale, () -> this.scale, (a) -> this.scale = a)
                .build());

        positionSettings.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.overture.appearance.position.padding"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.position.padding.description")))
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 8).step(1).formatValue(pixelFormatter))
                .binding(this.padding, () -> this.padding, (a) -> this.padding = a)
                .build());

        appearanceCategory.group(positionSettings.build());

        // Tweaks
        OptionGroup.Builder tweakSettings = OptionGroup.createBuilder()
                .name(Component.translatable("config.overture.appearance.tweaks"))
                .description(OptionDescription.of(Component.translatable("config.overture.appearance.tweaks.description")));

        tweakSettings.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.overture.general.tweaks.prefer_album_name"))
                .description(OptionDescription.of(Component.translatable("config.overture.general.tweaks.prefer_album_name.description")))
                .controller(TickBoxControllerBuilder::create)
                .binding(this.preferAlbumName, () -> this.preferAlbumName, (v) -> this.preferAlbumName = v)
                .build());

        tweakSettings.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.overture.general.tweaks.image_on_right"))
                .description(OptionDescription.of(Component.translatable("config.overture.general.tweaks.image_on_right.description")))
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> value ? Component.translatable("overture.position.bottom.horizontal") : Component.translatable("overture.position.top.horizontal")))
                .binding(this.imageOnRight, () -> this.imageOnRight, (v) -> this.imageOnRight = v)
                .build());

        tweakSettings.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.overture.general.tweaks.ignore_gui_scale"))
                .description(OptionDescription.of(Component.translatable("config.overture.general.tweaks.ignore_gui_scale.description")))
                .controller(TickBoxControllerBuilder::create)
                .binding(this.ignoreGuiScale, () -> this.ignoreGuiScale, (v) -> this.ignoreGuiScale = v)
                .build());

        appearanceCategory.group(tweakSettings.build());

        builder.category(appearanceCategory.build());

        return builder.build().generateScreen(parent);
    }
}
