package de.derioo.module.predefined.suggestion;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@Builder
public class Suggestion {

    @Id
    ObjectId id;

    Long guildId;

    Long userId;

    Long adminMessageId;

    Long messageId;

    Status status;

    String suggestion;

    enum Status {

        NONE,
        ACCEPTED,
        DECLINED

    }

}
