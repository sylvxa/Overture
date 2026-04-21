package lol.sylvie.overture;

import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.config.Configuration;
import lol.sylvie.overture.hud.HudHandler;
import lol.sylvie.overture.util.KeyMappings;
import net.fabricmc.api.ClientModInitializer;

import static lol.sylvie.overture.util.Constants.LOGGER;

public class Overture implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LOGGER.info("the Overture is my favorite part of every musical");

		Configuration.HANDLER.load();

		RetrievalHandler.init();

		HudHandler.init();
		KeyMappings.init();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Configuration.HANDLER.save();
			RetrievalHandler.close();
		}));
	}
}