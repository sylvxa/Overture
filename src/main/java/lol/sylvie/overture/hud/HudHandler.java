package lol.sylvie.overture.hud;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.config.Configuration;
import lol.sylvie.overture.util.Constants;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;
import org.joml.Matrix3x2fStack;

import java.util.concurrent.TimeUnit;

public class HudHandler {
    private static final Identifier ID = Constants.id("hud_element");
    public static final Identifier TEXTURE_ID = Constants.id("texture_id");

    private static final float TITLE_SCALE = 1.5f;
    private static final float TIME_SCALE = 0.5f;

    private static String formatMillisToMmSs(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static void textScaled(GuiGraphicsExtractor graphics, String text, int x, int y, int color, float scale) {
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.scale(scale);
        graphics.text(Minecraft.getInstance().font, text, 0, 0, color);
        poseStack.popMatrix();
    }

    private static void overflowText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int width, float scale, int buffer) {
        Font font = Minecraft.getInstance().font;
        graphics.enableScissor(x, y, x + width, y + (int) (font.lineHeight * scale) + 2);

        int textWidth = (int) (font.width(text) * scale);
        boolean overflow = textWidth > width;

        float animationTime = ((float) (System.currentTimeMillis() % 10000) / 10000);

        int titleX = overflow ? x - (int) ((textWidth + buffer) * animationTime) : x;
        textScaled(graphics, text, titleX, y, color, scale);
        if (overflow) textScaled(graphics, text, titleX + textWidth + buffer, y, color, scale);

        graphics.disableScissor();
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        MetadataRetriever.Result result = RetrievalHandler.RESULT;
        if (result == null) return;

        Minecraft minecraft = Minecraft.getInstance();

        Configuration config = Configuration.HANDLER.instance();
        Window window = minecraft.getWindow();
        Matrix3x2fStack poseStack = graphics.pose();
        if (config.ignoreGuiScale) {
            poseStack.pushMatrix();
            poseStack.scale(1f / window.getGuiScale());
        }

        int nonPaddedWidth = (config.width == 0 ? 99 : config.width); // TODO: Calculate width here
        int width = nonPaddedWidth + (config.padding * 2);
        int height = 32 + (config.padding * 2);

        int actualWidth = (int) (width * config.scale);
        int actualHeight = (int) (height * config.scale);

        int x = config.xAnchor.value(actualWidth, config.ignoreGuiScale ? window.getWidth() : graphics.guiWidth()) + config.xOffset;
        int y = config.yAnchor.value(actualHeight, config.ignoreGuiScale ? window.getHeight() : graphics.guiHeight()) + config.yOffset;

        poseStack.pushMatrix(); // 0, 0 is the origin of the HUD element
        poseStack.translate(x, y);
        poseStack.scale(config.scale);

        graphics.fill(0, 0, width, height, config.background.getRGB());

        poseStack.pushMatrix(); // 0, 0 is just past the padding
        poseStack.translate(config.padding, config.padding);

        int imageSize = 32;
        int imageX = config.imageOnRight ? nonPaddedWidth - imageSize : 0;
        graphics.blit(TEXTURE_ID, imageX, 0, imageX + imageSize, imageSize, 0f, 1f, 0f, 1f);

        int textX = config.imageOnRight ? 0 : imageSize + 4;
        int barWidth = nonPaddedWidth - imageSize - 4;
        Font font = minecraft.font;

        String secondaryText = config.preferAlbumName ? result.album().isEmpty() || result.album().equals(result.name()) ? result.artist() : result.album() : result.artist();

        int titleHeight = (int) (font.lineHeight * TITLE_SCALE);
        overflowText(graphics, result.name(), textX, 0, config.title.getRGB(), barWidth, TITLE_SCALE, 48);
        overflowText(graphics, secondaryText, textX, titleHeight, config.artist.getRGB(), barWidth, 1f, 24);

        // Time bar
        long durationMs = RetrievalHandler.RESULT.duration();
        long currentMs = Math.clamp(RetrievalHandler.RESULT.current() + (System.currentTimeMillis() - RetrievalHandler.lastFetch), 0, durationMs);
        float progress = Math.clamp((float) currentMs / durationMs, 0f, 1f);

        int barX2 = textX + barWidth;
        int barY = imageSize - 2;
        graphics.fill(textX, barY, barX2, imageSize, config.progressBackground.getRGB());
        graphics.fill(textX, barY, (int) (textX + (barWidth * progress)), imageSize, config.progressForeground.getRGB());

        int timeTextHeight = (int) (font.lineHeight * TIME_SCALE);
        int timeTextY = barY - timeTextHeight - 2;
        textScaled(graphics, formatMillisToMmSs(currentMs), textX, timeTextY, config.progress.getRGB(), TIME_SCALE);

        String durationText = formatMillisToMmSs(durationMs);
        int durationX = (int) (barX2 - (font.width(durationText) * TIME_SCALE));
        textScaled(graphics, durationText, durationX, timeTextY, config.duration.getRGB(), TIME_SCALE);

        poseStack.popMatrix();
        poseStack.popMatrix();
        if (config.ignoreGuiScale) poseStack.popMatrix();
    }

    public static void init() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, ID, HudHandler::render);
    }
}
