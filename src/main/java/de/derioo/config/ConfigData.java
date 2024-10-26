package de.derioo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    Map<String, List<Long>> roles;

    Map<String, String> data;

    @Contract("_ -> new")
    public static @NotNull ConfigData defaultData(String guild) {
        return new ConfigData(guild, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }


    public boolean isRoleValid(Config.Id.Role role, Role id) {
        return isRoleValid(role, id.getIdLong());
    }


    public boolean isRoleValid(Config.Id.Role role, Long id) {
        return roles.get(role.name()).contains(id);
    }

    public String getMentions(Config.Id.Role role, Guild guild) {
        return roles.get(role.name()).stream().map(id -> guild.getRoleById(id).getAsMention()).collect(Collectors.joining(","));
    }

    public List<Role> getRoleObjects(Config.Id.Role role, Guild guild) {
        return roles.get(role.name()).stream().map(guild::getRoleById).toList();
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

    public <T> T getData(String id, TypeReference<T> reference) {
        String json = data.getOrDefault(id, null);
        if (json == null) return null;
        try {
            return new ObjectMapper().readValue(json, reference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }



    public <T> T getData(Config.Id.Data id, Class<T> clazz) {
        return getData(id.name(), clazz);
    }

    public <T> T getData(Config.Id.Data id, Class<T> clazz, T defaultValue) {
        T value = getData(id.name(), clazz);
        return value == null ? defaultValue : value;
    }

    public void putData(String id, Object data) {
        try {
            this.data.put(id, new ObjectMapper().writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void putData(Config.Id.Data id, Object data) {
        putData(id.name(), data);
    }

}
