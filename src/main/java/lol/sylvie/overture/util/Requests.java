package lol.sylvie.overture.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static lol.sylvie.overture.util.Constants.GSON;

public class Requests {
    /*
     * Returns either the proper JSON object or an error message
     */
    public static Either<JsonObject, String> get(String uri) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject body = null;
            try {
                body = GSON.fromJson(response.body(), JsonObject.class);
            } catch (JsonSyntaxException _) {}

            if (response.statusCode() == 200 && body != null) {
                return Either.left(body);
            } else {
                Constants.LOGGER.error("Response {} from GET {}: {}",  response.statusCode(), uri, response.body());
                if (body != null && body.has("message")) {
                    return Either.right(body.get("message").getAsString());
                }
                return Either.right(response.body());
            }
        } catch (Exception e) {
            Constants.LOGGER.error("Couldn't GET {}", uri, e);
            return Either.right(e.toString());
        }
    }
}
