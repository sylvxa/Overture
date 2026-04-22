package lol.sylvie.overture.backend.art;

import lol.sylvie.overture.backend.MetadataRetriever;

public interface ArtProvider {
    void load(MetadataRetriever.Result result);
}
