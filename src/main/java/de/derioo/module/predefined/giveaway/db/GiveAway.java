package de.derioo.module.predefined.giveaway.db;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bson.types.ObjectId;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GiveAway {

    @Id
    ObjectId id;

    Long creatorId;

    String reward;

    int winnersCount;

    Long duration;

    Long channelId;

    Long messageId;

    Long guildId;

    List<Long> participants;

    List<Long> winners;


}
