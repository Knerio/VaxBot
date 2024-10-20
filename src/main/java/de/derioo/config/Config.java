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
        return data.getOrDefault(guild.getId(), ConfigData.defaultData(guild.getId()));
    }

    public ConfigData get(String id) {
        return data.getOrDefault(id, ConfigData.defaultData(id));
    }


    public static class Id {

        public enum User {
            RANDOM_MEME_USERS
        }

        public enum Channel {
            TICKET_CREATION_CHANNEL,
            TICKET_LOGS_CHANNEL,
            ERROR_CHANNEL,
            RULE_CHANNEL,
            APPLY_CHANNEL,
            PROMOTE_CHANNEL, SUGGESTION_CREATE_CHANNEL, SUGGESTION_CHANNEL, SUGGESTION_ADMIN_CHANNEL, JOIN_CHANNEL, USER_COUNT_CHANNEL, SUPPORT_CHANNEL, TEAM_CHAT, FEEDBACK_CREATION_CHANNEL, FEEDBACK_ADMIN_CHANNEL, BOOST_CHANNEL, LOG_CHANNEL, TWITCH_NOTIFY_CHANNEL, EVENT_TOKEN_CHANNEL, YOUTUBE_NOTIFY_CHANNEL, TIKTOK_NOTIFY_CHANNEL, STAFFLIST_CHANNEL
        }

        public enum Category {
            BUG_CATEGORY, TICKET_CATEGORY
        }

        public enum Role {
            TICKET_EDIT,
            REMOVE_USER_FROM_TICKET,
            TEAM_ADD,
            TEAM_ROLE,
            ADD_USER_TO_TICKET,
            GIVEAWAY_CREATE_ROLE,
            CLEAR,
            MOVE_ALL,
            BUG_REPORT_EDIT,
            BAN_USER,
            KICK_USER,
            TIMEOUT_USER,
            WARN_USER,
            PLAYER_ROLE, SUPPORT_PING, RULE_ACCEPT_ROLES, LEVEL_50_ROLE, LEVEL_30_ROLE, LEVEL_20_ROLE, TWITCH_PING_ROLES, YOUTUBE_PING_ROLES, TIKTOK_PING_ROLES, GIVEAWAY_PING_ROLE
        }

        public enum Data {

            TWITCH_NOTIFIER, YOUTUBE_NOTIFIER, TIKTOK_NOTIFIER, YOUTUBE_NOTIFIER_IDS, TEAM_ROLE

        }
    }

}
