package lol.sylvie.overture.backend.impl;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.backend.art.ByteArrayArtProvider;
import lol.sylvie.overture.util.Constants;
import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/*
 * forgive me if this is bad,
 * i have never, ever, ever used windows anything with java
 */
public class WinRTRetriever extends MetadataRetriever {
    private boolean failedLoading = false;
    private IWindowsRetriever library;

    public WinRTRetriever() {
        super(RetrievalHandler.Type.WINRT);
        if (isAvailable())
            try {
                File extractedFile = Native.extractFromResourcePath("natives/overture/overture-windows-retriever.dll");
                System.setProperty("jna.library.path", extractedFile.getParent());
                library = Native.load(extractedFile.getAbsolutePath(), IWindowsRetriever.class);
            } catch (IOException exception) {
                Constants.LOGGER.error("Couldn't load Windows native library", exception);
                failedLoading = true;
            }
    }

    @Override
    public @Nullable Result fetch() {
        Pointer pointer = library.Retrieve();
        if (pointer == null) return null;

        ResultData resultData = new ResultData(pointer);

        byte[] dataBuffer = resultData.thumbnailData.getByteArray(0, resultData.thumbnailSize);
        long current = resultData.current + (System.currentTimeMillis() - resultData.timestamp);
        return new Result(resultData.name, resultData.artist, resultData.album, new ByteArrayArtProvider(dataBuffer), resultData.duration, current);
    }

    @Override
    public boolean isAvailable() {
        return SystemUtils.IS_OS_WINDOWS && !failedLoading;
    }

    @Override
    public void cleanup() {
        // cleanup something here probably
    }

    public interface IWindowsRetriever extends Library {
        Pointer Retrieve();
    }

    public static class ResultData extends Structure {
        public String name;
        public String artist;
        public String album;
        public Pointer thumbnailData;
        public int thumbnailSize;
        public long duration;
        public long current;
        public long timestamp;

        public ResultData(Pointer p) {
            super(p);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("name", "artist", "album", "thumbnailData", "thumbnailSize", "duration", "current", "timestamp");
        }
    }
}
