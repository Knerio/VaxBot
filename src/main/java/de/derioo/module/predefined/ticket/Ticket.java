package de.derioo.module.predefined.ticket;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Ticket {

    @Id
    ObjectId id;

    Long guildId;

    Long userId;

    Long claimerId;

    Long channelId;

    String type;

    @Builder.Default
    List<HistoryItem> history = new ArrayList<>();



    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    @ToString
    public static class HistoryItem {

        ObjectId id;

        @Builder.Default
        Long senderId = null;

        String content;

    }



}
