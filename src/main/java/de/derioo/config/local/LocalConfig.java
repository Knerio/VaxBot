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

    public static LocalConfig loadByENV() throws IOException {
        Map<String, String> envs = System.getenv();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper
                .disable(JsonGenerator.Feature.IGNORE_UNKNOWN)
                .readValue(objectMapper.writeValueAsString(envs), LocalConfig.class);
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
