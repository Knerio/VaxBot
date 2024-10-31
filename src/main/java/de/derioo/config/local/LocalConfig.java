package de.derioo.config.local;


import de.derioo.shadow.jackson.annotation.JsonIgnoreProperties;
import de.derioo.shadow.jackson.annotation.JsonProperty;
import de.derioo.shadow.jackson.core.JsonGenerator;
import de.derioo.shadow.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Jacksonized
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalConfig {

    @JsonProperty("token")
    String token;


    @JsonProperty("connection-string")
    String connectionString;

    @Builder.Default
    String db = "discord-bot";


    Twitch twitch;

    Youtube youtube;

    TikTok tiktok;


    public static LocalConfig load(File file) throws IOException {
        return new ObjectMapper().readValue(file, LocalConfig.class);
    }

    public static LocalConfig loadByENV() {
        return LocalConfig.builder()
                .token(System.getenv("DISCORD_BOT_TOKEN"))
                .connectionString(System.getenv("DB_CONNECTION_STRING"))
                .db(System.getenv().getOrDefault("DB_NAME", "discord-bot"))
                .twitch(Twitch.builder()
                        .clientId(System.getenv("TWITCH_CLIENT_ID"))
                        .clientSecret(System.getenv("TWITCH_CLIENT_SECRET"))
                        .build())
                .youtube(Youtube.builder()
                        .token(System.getenv("YOUTUBE_TOKEN"))
                        .build())
                .tiktok(TikTok.builder()
                        .token(System.getenv("TIKTOK_TOKEN"))
                        .build())
                .build();
    }



    @Jacksonized
    @Builder
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Twitch {

        @JsonProperty("client-id")
        String clientId;
        @JsonProperty("client-secret")
        String clientSecret;

    }

    @Jacksonized
    @Builder
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Youtube {

        String token;

    }

    @Jacksonized
    @Builder
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TikTok {

        String token;

    }

}
