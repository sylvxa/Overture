package lol.sylvie.overture.backend.impl;

import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.util.Constants;
import org.apache.commons.lang3.SystemUtils;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.interfaces.Properties;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class MPRISRetriever extends MetadataRetriever {
    private static final String INTERFACE_NAME = "org.mpris.MediaPlayer2.Player";
    private final DBusConnection connection;

    public MPRISRetriever() {
        super(RetrievalHandler.Type.MPRIS);
        if (!this.isAvailable()) {
            connection = null;
            return;
        }
        try {
            this.connection = DBusConnectionBuilder.forSessionBus().build();
        } catch (DBusException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable Result fetch() {
        try {
            DBus dbus = connection.getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus", DBus.class);
            List<String> potentialPlayers = Arrays.stream(dbus.ListNames())
                    .filter(name -> name.startsWith("org.mpris.MediaPlayer2."))
                    .toList();

            Properties player = null;
            for (String name : potentialPlayers) {
                Properties potentialObject = connection.getRemoteObject(name, "/org/mpris/MediaPlayer2", Properties.class);
                String playbackStatus = potentialObject.Get(INTERFACE_NAME, "PlaybackStatus");
                if (playbackStatus.equalsIgnoreCase("Playing")) {
                    player = potentialObject;
                    break;
                }
            }

            if (player == null) return null;

            LinkedHashMap<String, Object> metadata = player.Get(INTERFACE_NAME, "Metadata");

            String album = (String) metadata.getOrDefault("xesam:album", "");
            String title = (String) metadata.get("xesam:title");
            String artUrl = (String) metadata.get("mpris:artUrl");

            ArrayList<String> artists = (ArrayList<String>) metadata.get("xesam:artist");
            String artist = artists == null ? album : String.join(", ", artists);

            long length = (Long) metadata.get("mpris:length") / 1000;
            long position = ((Long) player.Get(INTERFACE_NAME, "Position")) / 1000; // DBus returns Us, we want ms

            return new Result(title, artist, album, artUrl, length, position);
        } catch (DBusException e) {
            Constants.LOGGER.error("Couldn't retrieve MPRIS data!", e);
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        return SystemUtils.IS_OS_LINUX; // TODO: Not sure if D-Bus is available on *all* Linux systems
    }

    @Override
    public void cleanup() {
        try {
            connection.close();
        } catch (IOException ignored) {}
    }
}
