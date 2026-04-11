package lol.sylvie.overture.util;

import com.mojang.blaze3d.platform.InputConstants;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.config.Configuration;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class KeyMappings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Constants.id("keybinds"));

    private static long lastRefresh = 0;
    public static void init() {
        KeyMapping refreshKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.overture.refresh",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY
        ));

        KeyMapping configKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.overture.config",
                InputConstants.Type.KEYSYM,
                -1,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (refreshKeybind.consumeClick() && (System.currentTimeMillis() > lastRefresh + 1000L)) {
                RetrievalHandler.lastFetch = 0;
                client.player.sendOverlayMessage(Component.translatable("message.overture.refreshed"));
                lastRefresh = System.currentTimeMillis(); // I don't want people to spam APIs
            }

            while (configKeybind.consumeClick()) {
                client.setScreen(Configuration.HANDLER.instance().createScreen(null));
            }
        });
    }
}
