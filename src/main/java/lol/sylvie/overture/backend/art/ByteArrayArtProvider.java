package lol.sylvie.overture.backend.art;

import lol.sylvie.overture.backend.MetadataRetriever;

import java.io.ByteArrayInputStream;

public class ByteArrayArtProvider extends StreamArtProvider {
    private final byte[] data;

    public ByteArrayArtProvider(byte[] data) {
        this.data = data;
    }

    @Override
    public void load(MetadataRetriever.Result result) {
        load(result, new ByteArrayInputStream(data));
    }
}
