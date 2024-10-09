package de.derioo.module.predefined.randommeme.db;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RandomMeme {

    @Id
    ObjectId id;

    Long guildId;

    Binary data;

    String fileExtension;

}
