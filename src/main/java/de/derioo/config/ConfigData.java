package de.derioo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
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
public class ConfigData {

    String guildId;

    // id, channel-id
    Map<String, Long> channels;


    // id, role-id
    Map<String, Long> roles;

    Map<String, String> data;

    @Contract("_ -> new")
    public static @NotNull ConfigData defaultData(String guild) {
        return new ConfigData(guild,new HashMap<>(), new HashMap<>(), new HashMap<>());
    }


    public Guild getGuildId(@NotNull JDA jda) {
        return jda.getGuildById(guildId);
    }

    public <T> T getData(String id, Class<T> clazz) {
        String json = data.getOrDefault(id, null);
        if (json == null) return null;
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void putData(String id, Object data) {
        try {
            this.data.put(id, new ObjectMapper().writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
