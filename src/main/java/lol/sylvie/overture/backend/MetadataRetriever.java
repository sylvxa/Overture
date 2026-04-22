package lol.sylvie.overture.backend;

import lol.sylvie.overture.backend.art.ArtProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class MetadataRetriever {
    protected RetrievalHandler.Type type;

    protected MetadataRetriever(RetrievalHandler.Type type) {
        this.type = type;
    }

    public RetrievalHandler.Type getType() {
        return type;
    }

    /**
     * This method should fetch whatever track is currently playing, or null if there isn't one.
     * <p>
     * Fetching is done on a separate thread, and you should expect it to be done at most every few seconds.
     *
     * @return all available track data
     */
    public abstract @Nullable Result fetch();

    /**
     * This method is used to determine whether this retriever can be used *ever*
     * <p>
     * For example, a D-Bus retriever will only work under a Linux system.
     *
     * @return whether this retriever can be used on this system
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * This method is used to determine whether this retriever is ready to be used
     * <p>
     * For example, a Spotify retriever can only be used after the proper credentials have been stored.
     *
     * @return whether this retriever
     */
    public boolean isSetUp() {
        return true;
    }

    /**
     * Called as the retrieval thread is being closed
     */
    public void cleanup() {};

    /**
     * MPRIS D-Bus fields that are returned from playing Spotify in the browser, as kind of "baseline" case,
     * @param name the track's name
     * @param artist the track's artist
     * @param album the track's album
     * @param art the track's thumbnail
     * @param duration the track's duration, in milliseconds.
     * @param current how far into the track the user is, or at least a guess
     */
    public record Result(@NonNull String name, String artist, String album, @Nullable ArtProvider art, long duration, long current) {};
}
