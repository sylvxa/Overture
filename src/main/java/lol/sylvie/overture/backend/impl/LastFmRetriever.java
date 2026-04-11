package lol.sylvie.overture.backend.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.config.lastfm.LastFmAccount;
import lol.sylvie.overture.util.Constants;
import lol.sylvie.overture.util.Requests;
import org.jspecify.annotations.Nullable;

public class LastFmRetriever extends MetadataRetriever {
    public LastFmAccount account;
    private static final String CURRENT_DATA = "https://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=%user%&api_key=%api_key%&format=json&limit=1";
    private static final String TRACK_DATA = "https://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=%api_key%&format=json&mbid=%mbid%";

    private int failedRequests = 0;

    public LastFmRetriever() {
        super(RetrievalHandler.Type.LASTFM);
    }

    @Override
    public @Nullable Result fetch() {
        // Get what track we are playing
        Either<JsonObject, String> data = Requests.get(CURRENT_DATA
                .replace("%user%", account.name())
                .replace("%api_key%", account.apiKey()));

        if (data.right().isPresent()) {
            failedRequests += 1;
            Constants.LOGGER.error("Couldn't grab Last.fm track: {}", data.right().get());
            return null;
        }

        failedRequests = 0;

        JsonArray tracks = data.orThrow()
                .getAsJsonObject("recenttracks")
                .getAsJsonArray("track");

        if (tracks.isEmpty()) return null;

        JsonObject track = tracks.get(0).getAsJsonObject();

        JsonObject attr = track.getAsJsonObject("@attr");
        if (attr == null) return null;

        boolean isPlaying = attr.get("nowplaying").getAsBoolean();
        if (!isPlaying) return null;

        // Last.fm doesn't give us
        JsonObject date = track.getAsJsonObject("date");
        long start = date.get("uts").getAsLong();
        String mbid = track.get("mbid").getAsString();

        // Get track specifics
        Either<JsonObject, String> trackData = Requests.get(TRACK_DATA
                .replace("%api_key%", account.apiKey())
                .replace("%mbid%", mbid));
        if (trackData.right().isPresent()) {
            failedRequests += 1;
            Constants.LOGGER.error("Couldn't grab Last.fm track data: {}", trackData.right().get());
            return null;
        }
        JsonObject trackObject = trackData.orThrow().getAsJsonObject("track");

        JsonArray images = track.get("image").getAsJsonArray();
        JsonObject image = images.get(images.size() - 1).getAsJsonObject();

        long duration = trackObject.get("duration").getAsLong();
        return new Result(
                track.get("name").getAsString(),
                track.get("artist").getAsJsonObject().get("#text").getAsString(),
                track.get("album").getAsJsonObject().get("#text").getAsString(),
                image.get("#text").getAsString(),
                duration,
                System.currentTimeMillis() - start
        );
    }

    @Override
    public boolean isSetUp() {
        return account != null && failedRequests < 5;
    }
}
