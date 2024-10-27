package de.derioo.module.predefined.ticket;

import de.derioo.config.Config;
import de.derioo.utils.Emote;
import de.derioo.utils.UserUtils;
import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
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

    public Set<User> getParticipantUsers(Guild guild) {
        Set<User> participants = new HashSet<>();
        if (this.userId != null) participants.add(guild.getJDA().getUserById(this.userId));
        if (this.claimerId != null) participants.add(guild.getJDA().getUserById(this.claimerId));
        for (HistoryItem item : this.history) {
           try {
                if (item == null || item.getSenderId() == null) continue;
               User user = guild.getJDA().getUserById(item.getSenderId());
               if (user == null) continue;
               participants.add(user);
           } catch (Exception e) {
               e.printStackTrace();
           }
        }
        return participants;
    }

    public String getParticipants(Guild guild) {
        return getParticipantUsers(guild).stream().map(UserUtils::getMention).collect(Collectors.joining("\n"));
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

        BUG(Config.Id.Role.BUG_REPORT_EDIT, Config.Id.Category.BUG_CATEGORY, "❌ Bugreport", "Du hast einen Fehler gefunden? Melde ihn hier!"),
        QUESTIONS(Config.Id.Role.TICKET_EDIT, Config.Id.Category.TICKET_CATEGORY, "❓Allgemeine Fragen", "Allgemeine Fragen"),
        PARTNER(Config.Id.Role.TICKET_EDIT, Config.Id.Category.TICKET_CATEGORY, Emote.HANDSHAKE.getData() + "Partner Anfrage", "Du willst Partner werden?"),
        HELP_AND_SUPPORT(Config.Id.Role.TICKET_EDIT, Config.Id.Category.TICKET_CATEGORY, Emote.BUSTS.getData() + "Hilfe & Support", "Du brauchst allgemeine Hilfe?"),
        EVENT_TOKEN(Config.Id.Role.TICKET_EDIT, Config.Id.Category.TICKET_CATEGORY, "\uD83C\uDF20 Event Token Anfrage", "Du willst Event Tokens einlösen?");

        private final Config.Id.Role role;
        private final Config.Id.Category category;
        private final String tag;
        private final String desc;

        Type(Config.Id.Role role, Config.Id.Category category, String tag, String desc) {
            this.role = role;
            this.category = category;
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
