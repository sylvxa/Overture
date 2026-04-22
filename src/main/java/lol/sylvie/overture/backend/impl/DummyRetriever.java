package lol.sylvie.overture.backend.impl;

import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;
import org.jspecify.annotations.Nullable;

// Used if someone tries to use Overture on macOS or something weird
public class DummyRetriever extends MetadataRetriever {
    public DummyRetriever() {
        super(RetrievalHandler.Type.DUMMY);
    }

    @Override
    public @Nullable Result fetch() {
        return null;
    }
}
