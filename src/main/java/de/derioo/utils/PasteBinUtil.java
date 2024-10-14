package de.derioo.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class PasteBinUtil {

    private final String BASE_URL = "https://api.paste.gg/v1/";

    public URI createPasteOfThrowable(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        String name = throwable.getClass().getName() + ": " + throwable.getMessage();
        try {
            return createPaste(name, sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throwable.printStackTrace();
        }
        return null;
    }

    public URI createPaste(String fileName, String text) {
        try {
            Request reqObject = new Request(new Request.RequestFile[]{new Request.RequestFile(fileName, new Request.RequestFile.Content("text", text))});
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "pastes"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(reqObject))).build();


            try (HttpClient client = HttpClient.newHttpClient()) {
                String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                PasteCreatedResponse response = new ObjectMapper()
                        .readValue(body, PasteCreatedResponse.class);
                if (response.result == null) throw new RuntimeException("Error: " + body);
                return URI.create("https://paste.gg/" + response.result.id);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PasteCreatedResponse {


        Result result;


        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Result {

            String id;
        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Request {

        RequestFile[] files;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @FieldDefaults(level = AccessLevel.PRIVATE)
        static class RequestFile {

            String name;
            Content content;

            @Getter
            @Setter
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            @FieldDefaults(level = AccessLevel.PRIVATE)
            static class Content {

                String format;
                String value;

            }

        }
    }

}
