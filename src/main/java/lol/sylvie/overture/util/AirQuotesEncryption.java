package lol.sylvie.overture.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/*
 * This isn't meant for security!
 *
 * This is mainly to stop weird mods or silly modpack makers from syncing the file for whatever reason and having other clients pick it up.
 */
public class AirQuotesEncryption {
    private static String KEY;

    public static void init() {
        String hostname = "device";
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            hostname = localhost.getHostName();
        } catch (UnknownHostException _) {}

        KEY = hostname + "@" + System.getProperty("user.name", "anonymous");
    }

    private static byte[] xor(String input, String key) {
        byte[] data = input.getBytes();
        byte[] keyBytes = key.getBytes();
        for (int i = 0; i < data.length; i++)
            data[i] ^= keyBytes[i % keyBytes.length];
        return data;
    }

    public static String encrypt(String apiKey) {
        return Base64.encodeBase64String(xor(apiKey, KEY));
    }

    public static String decrypt(String blob) {
        return new String(xor(new String(Base64.decodeBase64(blob)), KEY));
    }
}
