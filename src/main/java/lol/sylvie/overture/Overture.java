package lol.sylvie.overture;

import com.mojang.blaze3d.platform.InputConstants;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.config.Configuration;
import lol.sylvie.overture.config.lastfm.LastFmAccount;
import lol.sylvie.overture.hud.HudHandler;
import lol.sylvie.overture.util.AirQuotesEncryption;
import lol.sylvie.overture.util.Constants;
import lol.sylvie.overture.util.KeyMappings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import static lol.sylvie.overture.util.Constants.LOGGER;

public class Overture implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LOGGER.info("the Overture is my favorite part of every musical");

		AirQuotesEncryption.init();
		Configuration.HANDLER.load();

		RetrievalHandler.init();

		HudHandler.init();
		KeyMappings.init();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Configuration.HANDLER.save();
			RetrievalHandler.close();

			//LastFmAccount account = RetrievalHandler.LASTFM.account;
			//if (account != null) account.save();
		}));
	}
}