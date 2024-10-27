package de.derioo.module.predefined.notifier.youtube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Getter
@ToString
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Jacksonized
public class YoutubeFeed {


    @JacksonXmlProperty(localName = "link")
    private Link selfLink;

    private String id;

    private String channelId;

    private String title;

    private Author author;

    private String published;

    @JacksonXmlProperty(localName = "entry")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Entry> entries;


    @Getter
    @ToString
    public static class Link {
        @JacksonXmlProperty(isAttribute = true)
        private String rel;

        @JacksonXmlProperty(isAttribute = true)
        private String href;
    }

    @Getter
    @ToString
    public static class Author {
        private String name;

        private String uri;
    }

    @Getter
    @ToString
    public static class Entry {

        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);


        private String id;

        @JacksonXmlProperty(localName = "videoId")
        private String videoId;

        @JacksonXmlProperty(localName = "channelId")
        private String channelId;

        private String title;

        private Link link;

        private Author author;

        private String published;


        private String updated;

        @JacksonXmlProperty(localName = "group")
        private MediaGroup mediaGroup;

        @JsonIgnore
        public Date getPublishedDate() {
            try {
                return DATE_FORMAT.parse(this.published);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Getter
    @ToString
    public static class MediaGroup {
        private String title;

        private MediaContent content;

        private MediaThumbnail thumbnail;

        private String description;

        private MediaCommunity community;
    }

    @Getter
    @ToString
    public static class MediaContent {
        @JacksonXmlProperty(isAttribute = true)
        private String url;

        @JacksonXmlProperty(isAttribute = true)
        private String type;

        @JacksonXmlProperty(isAttribute = true)
        private int width;

        @JacksonXmlProperty(isAttribute = true)
        private int height;
    }

    @Getter
    @ToString
    public static class MediaThumbnail {
        @JacksonXmlProperty(isAttribute = true)
        private String url;

        @JacksonXmlProperty(isAttribute = true)
        private int width;

        @JacksonXmlProperty(isAttribute = true)
        private int height;
    }

    @Getter
    @ToString
    public static class MediaCommunity {
        private MediaStarRating starRating;

        private MediaStatistics statistics;
    }

    @Getter
    @ToString
    public static class MediaStarRating {
        @JacksonXmlProperty(isAttribute = true)
        private int count;

        @JacksonXmlProperty(isAttribute = true)
        private double average;

        @JacksonXmlProperty(isAttribute = true)
        private int min;

        @JacksonXmlProperty(isAttribute = true)
        private int max;
    }

    @Getter
    @ToString
    public static class MediaStatistics {
        @JacksonXmlProperty(isAttribute = true)
        private int views;
    }


}
