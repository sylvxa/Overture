package lol.sylvie.overture.backend;

import com.mojang.blaze3d.platform.NativeImage;
import lol.sylvie.overture.backend.impl.MPRISRetriever;
import lol.sylvie.overture.config.Configuration;
import lol.sylvie.overture.hud.HudHandler;
import lol.sylvie.overture.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class RetrievalHandler {
    public enum Type {
        MPRIS(Component.translatable("overture.backend.mpris"), Component.translatable("overture.backend.mpris.description"));

        public Component getTitle() {
            return title;
        }

        public Component getDescription() {
            return description;
        }

        private final Component title;
        private final Component description;

        Type(Component title, Component description) {
            this.title = title;
            this.description = description;
        }
    }

    private static final HashMap<Type, MetadataRetriever> BACKENDS = new HashMap<>();

    private static <T extends MetadataRetriever> T register(T backend) {
        if (!backend.isAvailable()) {
            Constants.LOGGER.info("{} backend is unavailable", backend.type.name());
            return null;
        }
        BACKENDS.put(backend.getType(), backend);
        return backend;
    }

    public static HashMap<Type, MetadataRetriever> getBackends() {
        return BACKENDS;
    }

    public static final MPRISRetriever MPRIS = register(new MPRISRetriever());

    // Actually fetching
    public static Thread THREAD;

    public static volatile MetadataRetriever.Result RESULT = null;
    private static volatile boolean closing = false;

    private static void updateTexture(String artUrl) {
        Minecraft minecraft = Minecraft.getInstance();

        if (artUrl == null) {
            minecraft.execute(() -> {
                TextureManager manager = minecraft.getTextureManager();
                manager.release(HudHandler.TEXTURE_ID);
            });
            return;
        }

        try {
            URL url = new URI(artUrl).toURL();
            InputStream stream = url.openStream();
            minecraft.execute(() -> {
                TextureManager manager = minecraft.getTextureManager();
                try {
                    InputStream usableStream = stream;
                    if (artUrl.toLowerCase().endsWith(".jpg")) {
                        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                        BufferedImage jpgImage = ImageIO.read(usableStream);
                        ImageIO.write(jpgImage, "png", byteArrayOut);
                        usableStream.close();
                        usableStream = new ByteArrayInputStream(byteArrayOut.toByteArray());
                    }

                    NativeImage image = NativeImage.read(usableStream);
                    DynamicTexture texture = new DynamicTexture(() -> RESULT.name(), image);
                    manager.register(HudHandler.TEXTURE_ID, texture);
                    usableStream.close();
                } catch (IOException e) {
                    Constants.LOGGER.error("Could not load track art!", e);
                }
            });
        } catch (IOException | URISyntaxException e) {
            Constants.LOGGER.error("Could not fetch track art!", e);
        }
    }

    public static volatile long lastFetch = 0;

    @SuppressWarnings("BusyWait")
    private static void run() {
        String lastArtUrl = "";

        while (!closing) {
            try {
                Thread.sleep(50L); // There is no reason to check more than 20x a second

                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level == null) continue;

                Configuration config = Configuration.HANDLER.instance();
                if (!config.metadataRetriever.isAvailable() || !config.metadataRetriever.isSetUp()) continue;

                long nextCheck = (config.smartChecking && minecraft.isWindowActive() && RESULT != null) ?
                        ((lastFetch - RESULT.current()) + RESULT.duration()) + 500L : // 500ms buffer for loading time
                        (lastFetch + (long) (config.interval * 1000L));
                long currentTime = System.currentTimeMillis();

                if (currentTime < nextCheck) continue;

                RESULT = config.metadataRetriever.fetch();
                lastFetch = currentTime;

                // Update image
                if (RESULT == null) continue;
                String newArtUrl = RESULT.artUrl();
                if (!lastArtUrl.equals(newArtUrl)) updateTexture(newArtUrl);
                lastArtUrl = newArtUrl == null ? "" : newArtUrl;
            } catch (InterruptedException ignored) {
            } catch (Exception exception) {
                Constants.LOGGER.error("Error while fetching", exception);
            };


        }
        Configuration.HANDLER.instance().metadataRetriever.cleanup();
    }

    public static void init() {
        THREAD = new Thread(RetrievalHandler::run, "Overture-Fetching");
        THREAD.start();
    }

    public static void close() {
        closing = true;
    }
}
