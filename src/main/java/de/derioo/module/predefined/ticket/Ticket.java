package de.derioo.module.predefined.ticket;

import de.derioo.config.Config;
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

    Type type;

    @Builder.Default
    List<HistoryItem> history = new ArrayList<>();


    @Getter
    public enum Type {

        BUG(Config.Id.Role.BUG_REPORT_EDIT, "❌ BugReport", "Du hast einen Fehler gefunden? Melde ihn hier!"),
        QUESTIONS(Config.Id.Role.TICKET_EDIT, "❓Allgemeine Fragen", "Allgemeine Fragen"),
        PARTNER(Config.Id.Role.TICKET_EDIT, "\uD83E\uDD1D Partner Anfrage", "Du willst Partner werden?"),
        HELP_AND_SUPPORT(Config.Id.Role.TICKET_EDIT, "\uD83D\uDC65 Hilfe & Support", "Du brauchst allgemeine Hilfe?")
        ;

        private final Config.Id.Role role;
        private final String tag;
        private final String desc;

        Type(Config.Id.Role role, String tag, String desc) {
            this.role = role;
            this.tag = tag;
            this.desc = desc;
        }
    }

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
