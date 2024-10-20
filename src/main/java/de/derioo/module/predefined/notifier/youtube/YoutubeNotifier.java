package de.derioo.module.predefined.notifier.youtube;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.gson.JsonParser;
import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import de.derioo.module.predefined.notifier.NotifierModule;
import kotlin.Pair;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YoutubeNotifier {


    private static final String APPLICATION_NAME = "Varilx";
    private static final Long DELAY = TimeUnit.MINUTES.toMillis(30);

    private final NotifierModule module;
    private final DiscordBot bot;
    private final YouTube service;

    public YoutubeNotifier(NotifierModule module, DiscordBot bot) throws GeneralSecurityException, IOException {
        this.module = module;
        this.bot = bot;
        this.service = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                null
        ).setApplicationName(APPLICATION_NAME).build();

        timer();
    }



    private void timer() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<Guild, List<YoutubeCreatorObject>> entry : getTrackedYoutuberIds().entrySet()) {
                    Guild guild = entry.getKey();
                    ConfigData config = bot.get(guild);
                    for (YoutubeCreatorObject pair : entry.getValue()) {
                        try {
                            for (SearchResult video : getNewVideos(pair.getId())) {
                                TextChannel channel = guild.getTextChannelById(config.getChannels().get(Config.Id.Channel.YOUTUBE_NOTIFY_CHANNEL.name()));
                                List<Role> roles = config.getRoleObjects(Config.Id.Role.YOUTUBE_PING_ROLES, guild);
                                String videoUrl = getYoutubeVideoUrl(video.getId());
                                channel.sendMessage("> **" + pair.getName() + "** hat ein neues Video hochgeladen, schaut vorbei! " +
                                                roles.stream().map(Role::getAsMention).collect(Collectors.joining(",")))
                                        .addEmbeds(DiscordBot.Default.builder()
                                                .setAuthor("Youtube - " + pair.getName(), videoUrl)
                                                .setTitle(video.getSnippet().getTitle(), videoUrl)
                                                .setImage(video.getSnippet().getThumbnails().getHigh().getUrl())
                                                .setColor(Color.RED)
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
    private @NotNull String getYoutubeVideoUrl(@NotNull ResourceId id) {
        return "https://youtube.com/watch?v=" + id.getVideoId();
    }

    private @NotNull Map<Guild, List<YoutubeCreatorObject>> getTrackedYoutuberIds() {
        Map<Guild, List<YoutubeCreatorObject>> map = new HashMap<>();
        for (Guild guild : bot.getJda().getGuilds()) {
            List<YoutubeCreatorObject> data = bot.get(guild).getData(Config.Id.Data.YOUTUBE_NOTIFIER_IDS.name(), List.class);
            map.put(guild, data);
        }
        return map;
    }

    public String getYoutuberId(String channelName) throws IOException {
        YouTube.Search.List search = service.search().list(Collections.singletonList("snippet"));
        search.setKey(getAPIKey());
        search.setQ(channelName);
        search.setType(Collections.singletonList("channel"));
        search.setMaxResults(1L);

        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResults = searchResponse.getItems();

        if (!searchResults.isEmpty()) {
            SearchResult result = searchResults.getFirst();
            return result.getSnippet().getChannelId();
        } else {
            return null;
        }
    }

    private List<SearchResult> getNewVideos(String channelId) throws IOException {
        YouTube.Search.List search = service.search().list(Collections.singletonList("id,snippet"));
        search.setKey(getAPIKey());
        search.setChannelId(channelId);
        search.setType(Collections.singletonList("video"));
        search.setMaxResults(50L);
        search.setPublishedAfter(new DateTime(System.currentTimeMillis() - DELAY).toString());

        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResults = searchResponse.getItems();

        return searchResults.stream().filter(searchResult -> {
            long value = searchResult.getSnippet().getPublishedAt().getValue();
            return (System.currentTimeMillis() - value) < DELAY;
        }).toList();
    }


    private String getAPIKey() {
        return this.bot.getConfig().getYoutube().getToken();
    }

}
