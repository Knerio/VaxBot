package de.derioo.module.predefined.notifier.youtube;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import de.derioo.module.predefined.notifier.NotifierModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class YoutubeNotifier {


    private static final String APPLICATION_NAME = "Varilx";
    private static final Long DELAY = 20_000L;

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
        ConfigData globalConfig = this.bot.get().getGlobal();
        long lastCheckedTimestamp = globalConfig.getData(Config.Id.Data.LAST_CHECKED_YOUTUBE_TIMESTAMP, Long.class, System.currentTimeMillis());
        long relativeLastCheckedTime = System.currentTimeMillis() - lastCheckedTimestamp;

        System.out.println("Running check in " + (DELAY - relativeLastCheckedTime) + "ms");
        System.out.println("Checking till " + (new Date(lastCheckedTimestamp)));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    runVideoCheck(lastCheckedTimestamp);
                } catch (Exception e) {
                    Module.logThrowable(bot, e);
                }
            }
        }, Math.max(0, DELAY - relativeLastCheckedTime));

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    runVideoCheck(System.currentTimeMillis() - DELAY);
                } catch (Exception e) {
                    Module.logThrowable(bot, e);
                }
            }
        }, Math.max(0, DELAY - relativeLastCheckedTime) + DELAY, DELAY);
    }

    private void runVideoCheck(Long delayTimeStamp) throws Exception {
         for (Map.Entry<Guild, List<YoutubeCreatorObject>> entry : getTrackedYoutuberIds().entrySet()) {
            Guild guild = entry.getKey();
            ConfigData config = bot.get(guild);
            for (YoutubeCreatorObject pair : entry.getValue()) {
                YoutubeFeed feed = getCurrentFeed(pair.getId(), delayTimeStamp);
                if (feed == null) continue;

                for (YoutubeFeed.Entry video : feed.getEntries()) {
                    TextChannel channel = guild.getTextChannelById(config.getChannels().get(Config.Id.Channel.YOUTUBE_NOTIFY_CHANNEL.name()));
                    List<Role> roles = config.getRoleObjects(Config.Id.Role.YOUTUBE_PING_ROLES, guild);
                    String videoUrl = video.getLink().getHref();

                    channel.sendMessage("> **" + pair.getName() + "** hat ein neues Video hochgeladen, schaut vorbei! " +
                                    roles.stream().map(Role::getAsMention).collect(Collectors.joining(",")))
                            .addEmbeds(DiscordBot.Default.builder()
                                    .setAuthor("Youtube - " + pair.getName(), videoUrl)
                                    .setTitle(video.getTitle(), videoUrl)
                                    .setImage(video.getMediaGroup().getThumbnail().getUrl())
                                    .setColor(Color.RED)
                                    .build())
                            .addActionRow(Button.link(videoUrl, "Schau vorbei!"))
                            .queue();
                }
            }
        }

        Config globalConfig = this.bot.get();
        globalConfig.getGlobal().putData(Config.Id.Data.LAST_CHECKED_YOUTUBE_TIMESTAMP, System.currentTimeMillis());
        this.bot.save(globalConfig);
    }

    private @NotNull Map<Guild, List<YoutubeCreatorObject>> getTrackedYoutuberIds() {
        Map<Guild, List<YoutubeCreatorObject>> map = new HashMap<>();
        for (Guild guild : bot.getJda().getGuilds()) {
            List<YoutubeCreatorObject> data = bot.get(guild).getData(Config.Id.Data.YOUTUBE_NOTIFIER_IDS.name(), new TypeReference<>() {});
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

    private YoutubeFeed getCurrentFeed(String channelId, Long checkTimeStamp) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url("https://youtube.com/feeds/videos.xml?channel_id=" + channelId)
                .get()
                .build();

        YoutubeFeed feed;
        try (okhttp3.Response response = client.newCall(request).execute();) {
            String string = response.body().string();
            feed = new XmlMapper().readValue(string, YoutubeFeed.class);
        } catch (Exception e) {
            Module.logThrowable(bot, e);
            return null;
        }

        for (YoutubeFeed.Entry entry : new ArrayList<>(feed.getEntries())) {
            Date date = entry.getPublishedDate();
            if (date.getTime() < checkTimeStamp) feed.getEntries().remove(entry);
        }
        return feed;
    }


    private String getAPIKey() {
        return this.bot.getConfig().getYoutube().getToken();
    }

}
