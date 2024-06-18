package de.derioo.config;

import de.derioo.config.repository.ConfigRepo;
import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Config {

    public static Config get(@NotNull ConfigRepo repo) {
        return repo.findAll().stream().findFirst().orElse(Config.defaultConfig());
    }

    @Contract(" -> new")
    public static @NotNull Config defaultConfig() {
        return new Config(new ObjectId(), new HashMap<>(), new HashMap<>());
    }


    @eu.koboo.en2do.repository.entity.Id
    ObjectId id;

    // id, channel-id
    Map<String, Long> channels;


    // id, role-id
    Map<String, Long> roles;


    public static class Id {

        public enum Channel {
            TICKET_CREATION_CHANNEL,
            TICKET_LOGS_CHANNEL
        }

        public enum Category {
            TICKET_CATEGORY
        }

        public enum Role {
        }

    }

}
