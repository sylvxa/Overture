package lol.sylvie.overture.config.lastfm;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import lol.sylvie.overture.backend.MetadataRetriever;
import lol.sylvie.overture.backend.RetrievalHandler;
import lol.sylvie.overture.screen.GuideScreen;
import lol.sylvie.overture.util.Requests;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class LastFmSetup extends GuideScreen {
    private String authorization = null;

    public LastFmSetup(Screen parent) {
        super(Component.translatable("overture.lastfm.setup"), parent);
    }

    private static final String authPortal = "https://www.last.fm/api/auth/?api_key=%api_key%&token=%token%";;
    private String getAuthPortal(String apiKey) {
        return authPortal.replace("%api_key%", apiKey).replace("%token%", authorization);
    }

    @Override
    protected void initSteps() {
        Component apiKey = Component.translatable("overture.lastfm.api_key");
        EditBox apiKeyBox = new EditBox(font, 300, 11, apiKey);
        apiKeyBox.setHint(apiKey);

        Component username = Component.translatable("overture.lastfm.username");
        EditBox usernameBox = new EditBox(font, 300, 11, apiKey);
        usernameBox.setHint(username);

        // Get API key
        //final String getToken = "https://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=%api_key%&format=json";
        final String checkUsername = "https://ws.audioscrobbler.com/2.0/?method=user.getinfo&user=%user%&api_key=%api_key%&format=json";

        addStep(new Step(() -> {
            Either<JsonObject, String> response = Requests.get(checkUsername
                    .replace("%user%", usernameBox.getValue())
                    .replace("%api_key%", apiKeyBox.getValue()));

            if (response.right().isPresent()) {
                return Component.literal(response.right().get());
            }

            return null;
        },
                text(Component.translatable("overture.lastfm.account", link("https://www.last.fm/join"))),
                text(Component.translatable("overture.lastfm.api_account", link("https://www.last.fm/api/account/create"))),
                text(Component.translatable("overture.lastfm.input_api")),
                usernameBox,
                apiKeyBox
        ));
        /*
        // Authorize API account
        Button openInBrowser = Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button -> {
            Util.getPlatform().openUri(getAuthPortal(apiKeyBox.getValue()));
        }).build();

        Button copyToClipboard = Button.builder(CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, button -> {
            this.minecraft.keyboardHandler.setClipboard(getAuthPortal(apiKeyBox.getValue()));
        }).build();

        final String getUserInfo = "https://ws.audioscrobbler.com/2.0/?method=user.getinfo&api_key=%api_key%&format=json";

        addStep(new Step(() -> {
            Either<JsonObject, String> userInfo = Requests.get(getUserInfo.replace(apiKeyBox.getValue()));
        },
            new MultiLineTextWidget(Component.translatable("overture.lastfm.authorization"), font),
            openInBrowser,
            copyToClipboard
        ));*/

        // Link platforms
        addStep(new Step(() -> {
            RetrievalHandler.LASTFM.account = new LastFmAccount(usernameBox.getValue(), apiKeyBox.getValue());
            return null;
        },
                text(Component.translatable("overture.lastfm.link_platforms", link("https://www.last.fm/about/trackmymusic#spotify"))),
                text(Component.translatable("overture.lastfm.set_spotify", link("https://www.last.fm/settings/website#playback")))
        ));
    }

    /*
    private static final String GET_AUTH_TOKEN = "http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=%key%&format=json";

    private static final List<Component> FIRST_GUIDE = List.of(
            C
            ,
            Component.translatable("")
    );

    // #1: Go to https://www.last.fm/join and make an account, verify email
    // #2: Make an API account https://www.last.fm/api/account/create
    //     - Application name: Whatever (Overture)
    //     - Application description: Display music information in Minecraft via Fabric mod
    //     - Callback URL: http://localhost:39855/overture
    // #3: Put the API key and Shared secret here

    @Override
    protected void init() {
        //addRenderableOnly()
    }

    private static Component literalLink(String uri) {
        return Component.literal(uri).withStyle(Style.EMPTY
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenUrl(URI.create(uri))));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int white = 0xFFFFFFFF;
        int centerX = this.width / 2;

        graphics.centeredText(font, this.title, centerX, 4, white);

        int pageWidth = 200;
        int textX = centerX - (pageWidth / 2);
        int y = 22;

        for (Component component : FIRST_GUIDE) {
            for (FormattedCharSequence line : font.split(component, pageWidth)) {
                graphics.text(font, line, textX, y, white, true);
                Objects.requireNonNull(font);
                y += 9;
            }
            y += 9;
        }
    }*/
}
