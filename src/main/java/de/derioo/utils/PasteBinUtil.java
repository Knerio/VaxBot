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
        String name = throwable.getClass().getName() + ": " + throwable.getMessage();
        try {
            return createPaste(name, getStacktrace(throwable));
        } catch (Exception e) {
            e.printStackTrace();
            throwable.printStackTrace();
        }
        return null;
    }

    public URI createPaste(String fileName, String text) {
        HttpRequest request;
        String bodyString;
        try {
            Request reqObject = new Request(new Request.RequestFile[]{new Request.RequestFile(fileName, new Request.RequestFile.Content("text", text))});
            bodyString = new ObjectMapper().writeValueAsString(reqObject);
            request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "pastes"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyString)).build();
        } catch (Exception e) {
            System.out.println("An error occurred while creating request");
            e.printStackTrace();
            return null;
        }
        String body;
        try (HttpClient client = HttpClient.newHttpClient()) {
             body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("An error occurred while sending request");
            e.printStackTrace();
            return null;
        }
        try {
            PasteCreatedResponse response = new ObjectMapper()
                    .readValue(body, PasteCreatedResponse.class);
            if (response.result == null) throw new RuntimeException("Error: " + body + "\nSent body: " + bodyString);
            return URI.create("https://paste.gg/" + response.result.id);
        } catch(Exception e) {
            System.out.println("Error occurred while parsing response");
            System.out.println("Response Body:" + body);
            e.printStackTrace();
            return null;
        }
    }

    public String getStacktrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
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
