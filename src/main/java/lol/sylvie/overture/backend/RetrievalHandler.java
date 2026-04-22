package lol.sylvie.overture.backend;

import lol.sylvie.overture.backend.impl.DummyRetriever;
import lol.sylvie.overture.backend.impl.MPRISRetriever;
import lol.sylvie.overture.backend.impl.WinRTRetriever;
import lol.sylvie.overture.config.Configuration;
import lol.sylvie.overture.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.*;
import java.util.HashMap;

public class RetrievalHandler {
    public enum Type {
        MPRIS(Component.translatable("overture.backend.mpris"), Component.translatable("overture.backend.mpris.description")),
        WINRT(Component.translatable("overture.backend.winrt"), Component.translatable("overture.backend.winrt.description")),
        DUMMY(Component.translatable("overture.backend.dummy"), Component.translatable("overture.backend.dummy.description"));

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
    public static final WinRTRetriever WINRT = register(new WinRTRetriever());
    public static final DummyRetriever DUMMY = register(new DummyRetriever());

    public static MetadataRetriever getUsableRetriever() {
        for (MetadataRetriever retriever : BACKENDS.values()) {
            if (retriever.isAvailable() && retriever != DUMMY) return retriever;
        }
        return DUMMY;
    }

    // Actually fetching
    public static Thread THREAD;

    public static volatile MetadataRetriever.Result RESULT = null;
    private static volatile boolean closing = false;

    public static volatile long lastFetch = 0;

    @SuppressWarnings("BusyWait")
    private static void run() {
        while (!closing) {
            try {
                Thread.sleep(50L); // There is no reason to check more than 20x a second

                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level == null) continue;

                Configuration config = Configuration.HANDLER.instance();
                if (!config.metadataRetriever.isAvailable() || !config.metadataRetriever.isSetUp()) continue;

                long nextCheck = (config.smartChecking && minecraft.isWindowActive() && RESULT != null) ?
                        ((lastFetch - RESULT.current()) + RESULT.duration()) + 1000L : // 1s buffer for loading time
                        (lastFetch + (long) (config.interval * 1000L));
                long currentTime = System.currentTimeMillis();

                if (currentTime < nextCheck) continue;

                RESULT = config.metadataRetriever.fetch();
                lastFetch = currentTime;

                // Update image
                if (RESULT == null || RESULT.art() == null) continue;
                RESULT.art().load(RESULT);
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
