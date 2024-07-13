package de.derioo.module.predefined.ticket;

import de.derioo.config.Config;
import de.derioo.utils.Emote;
import de.derioo.utils.UserUtils;
import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.stream.Collectors;

import static de.derioo.utils.UserUtils.getMention;

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

    public String getParticipants(Guild guild) {
        Set<String> participants = new HashSet<>();
        history.forEach(historyItem -> {
            Member member = Objects.requireNonNull(guild.getMemberById(historyItem.getSenderId()));
            participants.add(UserUtils.getMention(member));
        });
        return String.join("\n", participants);
    }

    public String getInformations(Guild guild) {
        StringBuilder builder = new StringBuilder();
        for (Ticket.HistoryItem historyItem : history) {
            if (historyItem.getSenderId() == null) {
                builder.append("*").append(historyItem.getContent()).append("*").append("\n");
                continue;
            }
            Member member = Objects.requireNonNull(guild.getMemberById(historyItem.getSenderId()));
            builder.append(getMention(member))
                    .append(" -> ")
                    .append(historyItem.getContent())
                    .append("\n");
        }
        builder.append("~~**---»-----------------------------------------«---**~~");
        return builder.toString();
    }

    @Getter
    public enum Type {

        BUG(Config.Id.Role.BUG_REPORT_EDIT, "❌ Bugreport", "Du hast einen Fehler gefunden? Melde ihn hier!"),
        QUESTIONS(Config.Id.Role.TICKET_EDIT, "❓Allgemeine Fragen", "Allgemeine Fragen"),
        PARTNER(Config.Id.Role.TICKET_EDIT, Emote.HANDSHAKE.getData() + "Partner Anfrage", "Du willst Partner werden?"),
        HELP_AND_SUPPORT(Config.Id.Role.TICKET_EDIT, Emote.BUSTS.getData() + "Hilfe & Support", "Du brauchst allgemeine Hilfe?");

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
