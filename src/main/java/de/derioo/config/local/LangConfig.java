package de.derioo.config.local;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@AllArgsConstructor
@Builder
@Jacksonized
@Getter
public class LangConfig {


    private final TicketConfig ticket;


    public static LangConfig load(File file) {
        try {
            return new ObjectMapper().readValue(file, LangConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LangConfig load(InputStream stream) {
        try {
            return new ObjectMapper().readValue(stream, LangConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    @Builder
    @Jacksonized
    @Getter
    public static class TicketConfig {

        @JsonProperty("ticket-creation-embed")
        Map<Object, Object> ticketCreationEmbed;

    }
}
