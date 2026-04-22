package lol.sylvie.overture.backend.art;

import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.util.Constants;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLArt extends StreamArtProvider {
    private final String artUrl;

    public URLArt(String artUrl) {
        this.artUrl = artUrl;
    }

    @Override
    public void load(MetadataRetriever.Result result) {
        try {
            URL url = new URI(this.artUrl).toURL();
            InputStream stream = url.openStream();
            load(result, stream);
        } catch (IOException | URISyntaxException e) {
            Constants.LOGGER.error("Could not fetch track art!", e);
        }
    }
}
