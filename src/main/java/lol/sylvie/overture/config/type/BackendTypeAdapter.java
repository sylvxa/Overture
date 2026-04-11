package lol.sylvie.overture.config.type;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;

import java.io.IOException;

public class BackendTypeAdapter extends TypeAdapter<MetadataRetriever> {
    @Override
    public void write(JsonWriter out, MetadataRetriever value) throws IOException {
        out.value(value.getType().name());
    }

    @Override
    public MetadataRetriever read(JsonReader in) throws IOException {
        String name = in.nextString();
        RetrievalHandler.Type type = RetrievalHandler.Type.valueOf(name);

        return RetrievalHandler.getBackends().getOrDefault(type, RetrievalHandler.MPRIS);
    }
}
