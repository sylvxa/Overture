package lol.sylvie.overture.config.lastfm;

import com.google.gson.JsonObject;
import lol.sylvie.overture.util.AirQuotesEncryption;
import lol.sylvie.overture.util.Constants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

import static lol.sylvie.overture.util.Constants.GSON;

public record LastFmAccount(String name, String apiKey) {
    private static final File FILE = FabricLoader.getInstance().getGameDir().resolve("lastfm.json").toFile();
    private static final String USER_INFO_API = "https://ws.audioscrobbler.com/2.0/?method=user.getinfo&api_key=%api_key%&format=json";

    public static LastFmAccount load() {
        try (FileReader reader = new FileReader(FILE)) {
            JsonObject object = GSON.fromJson(reader, JsonObject.class);

            String name = object.get("name").getAsString();

            String apiKeyBlob = object.get("api_key").getAsString();
            String apiKey = AirQuotesEncryption.decrypt(apiKeyBlob);

            return new LastFmAccount(name, apiKey);
        } catch (FileNotFoundException _) {
        } catch (IOException e) {
            Constants.LOGGER.error("Couldn't load Last.fm account!", e);
        }
        return null;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            JsonObject root = new JsonObject();

            root.addProperty("name", this.name);
            root.addProperty("api_key", AirQuotesEncryption.encrypt(this.apiKey));

            GSON.toJson(root, writer);
        } catch (IOException e) {
            Constants.LOGGER.error("Couldn't save Last.fm account!", e);
        }
    }
}
