package de.derioo.module.predefined.notifier.tiktok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import de.derioo.module.predefined.notifier.NotifierModule;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TikTokNotifier {

    private static final long DELAY = TimeUnit.MINUTES.toMillis(30);
    private final NotifierModule module;
    private final DiscordBot bot;

    public TikTokNotifier(NotifierModule module, DiscordBot bot) throws IOException {
        this.module = module;
        this.bot = bot;

        timer();
    }


    private void timer() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<Guild, List<String>> entry : getTrackedTiktokNames().entrySet()) {
                    Guild guild = entry.getKey();
                    ConfigData config = bot.get(guild);
                    for (String name : entry.getValue()) {
                        try {
                            TikTokNotifier.Response response = getNewVideos(name);
                            if (response.getVideos() == null) continue;
                            for (TikTokNotifier.Response.Video video : response.getVideos()) {
                                if (System.currentTimeMillis() - video.getCreateTime() * 1000 > DELAY) continue;

                                TextChannel channel = guild.getTextChannelById(config.getChannels().get(Config.Id.Channel.TIKTOK_NOTIFY_CHANNEL.name()));
                                List<Role> roles = config.getRoleObjects(Config.Id.Role.TIKTOK_PING_ROLES, guild);
                                String videoUrl = getYoutubeVideoUrl(video.id, response.username);

                                channel.sendMessage("> **" + video.author + "** hat ein neues Video auf Tiktok hochgeladen, schaut vorbei! " + roles.stream().map(Role::getAsMention).collect(Collectors.joining(",")))
                                        .addEmbeds(DiscordBot.Default.builder()
                                                .setAuthor("Tiktok - " + video.author, videoUrl, video.avatar)
                                                .setTitle(video.getDescription(), videoUrl)
                                                .setImage(video.cover)
                                                .setColor(Color.BLACK)
                                                .build())
                                        .addActionRow(Button.link(videoUrl, "Schau vorbei!"))
                                        .queue();
                            }
                        } catch (Exception e) {
                            Module.logThrowable(bot, e);
                        }
                    }
                }
            }
        }, DELAY, DELAY);
    }

    @Contract(pure = true)
    private @NotNull String getYoutubeVideoUrl(@NotNull String id, String userName) {
        return "https://tiktok.com/@" + userName + "/video/" + id;
    }

    private @NotNull Map<Guild, List<String>> getTrackedTiktokNames() {
        Map<Guild, List<String>> map = new HashMap<>();
        for (Guild guild : bot.getJda().getGuilds()) {
            List<String> data = bot.get(guild).getData(Config.Id.Data.TIKTOK_NOTIFIER.name(), List.class);
            map.put(guild, data);
        }
        return map;
    }

    private Response getNewVideos(String channelName) throws IOException {
        long start = System.currentTimeMillis();
        System.out.println("Fetching new videos");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url("https://tiktok-api6.p.rapidapi.com/user/videos?username=" + channelName)
                .get()
                .addHeader("x-rapidapi-key", getAPIKey())
                .addHeader("x-rapidapi-host", "tiktok-api6.p.rapidapi.com")
                .build();
        try (okhttp3.Response response = client.newCall(request).execute();) {
            String string = response.body().string();
            System.out.println("Body: " + string + " (" + (System.currentTimeMillis() - start) + "ms)");
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(string, Response.class);
        }
    }


    private String getAPIKey() {
        return this.bot.getConfig().getTiktok().getToken();
    }

    @Jacksonized
    @Builder
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Response {

        String username;

        List<Video> videos;


        @Jacksonized
        @Builder
        @Getter
        @Setter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Video {

            @JsonProperty("video_id")
            String id;

            @JsonProperty("author_name")
            String author;

            String description;

            @JsonProperty("create_time")
            long createTime;

            String cover;

            @JsonProperty("avatar_thumb")
            String avatar;

        }
    }
}
