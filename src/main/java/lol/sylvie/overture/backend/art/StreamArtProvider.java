package lol.sylvie.overture.backend.art;

import com.mojang.blaze3d.platform.NativeImage;
import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.hud.HudHandler;
import lol.sylvie.overture.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class StreamArtProvider implements ArtProvider {
    protected void load(MetadataRetriever.Result result, InputStream data) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            TextureManager manager = minecraft.getTextureManager();
            try {
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                BufferedImage rawImage = ImageIO.read(data);
                ImageIO.write(rawImage, "png", byteArrayOut);
                data.close();

                InputStream usableStream = new ByteArrayInputStream(byteArrayOut.toByteArray());

                NativeImage image = NativeImage.read(usableStream);
                DynamicTexture texture = new DynamicTexture(result::name, image);
                manager.register(HudHandler.TEXTURE_ID, texture);

                usableStream.close();
            } catch (IOException e) {
                Constants.LOGGER.error("Could not load track art!", e);
            }

        });
    }
}
