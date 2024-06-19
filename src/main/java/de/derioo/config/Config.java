package de.derioo.config;

import de.derioo.config.repository.ConfigRepo;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Guild;
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

    public static Config get(@NotNull Repository<Config, ?> repo) {
        return repo.findAll().stream().findFirst().orElse(Config.defaultConfig());
    }

    @Contract(" -> new")
    public static @NotNull Config defaultConfig() {
        return new Config(new ObjectId(), new HashMap<>());
    }


    @eu.koboo.en2do.repository.entity.Id
    ObjectId id;

    Map<String, ConfigData> data;


    public ConfigData get(@NotNull Guild guild) {
        return data.get(guild.getId());
    }

    public ConfigData get(String id) {
        return data.get(id);
    }


    public static class Id {

        public enum Channel {
            TICKET_CREATION_CHANNEL,
            TICKET_LOGS_CHANNEL,
            ERROR_CHANNEL,
            PROMOTE_CHANNEL, STAFFLIST_CHANNEL
        }

        public enum Category {
            TICKET_CATEGORY
        }

        public enum Role {
            TICKET_EDIT,
            TICKET_MANAGE,
            REMOVE_USER_FROM_TICKET, TEAM_ADD, TEAM_ROLE, ADD_USER_TO_TICKET
        }

        public enum Data {

            TEAM_ROLE

        }
    }

}
