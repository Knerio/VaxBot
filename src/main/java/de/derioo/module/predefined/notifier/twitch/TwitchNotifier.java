package de.derioo.module.predefined.notifier.twitch;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.Stream;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.predefined.notifier.NotifierModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TwitchNotifier {

    private final NotifierModule module;
    private final DiscordBot bot;
    private final TwitchClient client;

    private final Map<String, Long> notifyCache = new HashMap<>();

    public TwitchNotifier(NotifierModule module, DiscordBot bot) {
        this.module = module;
        this.bot = bot;
        client = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId(bot.getConfig().getTwitch().getClientId())
                .withTimeout(10000)
                .withClientSecret(bot.getConfig().getTwitch().getClientSecret())
                .build();
        client.getEventManager().onEvent(ChannelGoLiveEvent.class, this::onGoLive);

        for (Guild guild : bot.getJda().getGuilds()) {
            List<String> data = bot.get(guild).getData(Config.Id.Data.TWITCH_NOTIFIER.name(), List.class);
            System.out.println("Enabling streamers: "+ data);
            client.getClientHelper().enableStreamEventListener(data);
        }
    }

    private void onGoLive(ChannelGoLiveEvent event) {
        System.out.println(event.getChannel().getName() + " is now live");
        for (Guild guild : this.bot.getJda().getGuilds()) {
            System.out.println("Trying to notifiy " + guild.getName());
            ConfigData config = bot.get(guild);
            List<String> data = config.getData(Config.Id.Data.TWITCH_NOTIFIER.name(), List.class);
            if (!data.contains(event.getChannel().getName())) continue;
            TextChannel channel = guild.getTextChannelById(config.getChannels().get(Config.Id.Channel.TWITCH_NOTIFY_CHANNEL.name()));
            List<Role> roles = config.getRoleObjects(Config.Id.Role.TWITCH_PING_ROLES, guild);
            System.out.println("Notifying " + guild.getName());
            notify(event.getStream(), channel, roles);
        }
    }

    private void notify(Stream stream, TextChannel channel, List<Role> roles) {
        notifyCache.putIfAbsent(stream.getUserName(), System.currentTimeMillis());
        if (System.currentTimeMillis() - notifyCache.get(stream.getUserName()) > TimeUnit.SECONDS.toMillis(120)) {
            System.out.println("Skipping notification for " + stream.getUserName() + " because of cache");
            return;
        }
        notifyCache.put(stream.getUserName(), System.currentTimeMillis());

        String imageUrl = client.getHelix().getUsers(null, null, List.of(stream.getUserName())).execute().getUsers().getFirst().getProfileImageUrl();
        String streamUrl = "https://twitch.tv/" + stream.getUserName();
        channel.sendMessage("> **" + stream.getUserName() + "** ist live auf Twitch gegangen, schaut vorbei! " +
                        roles.stream().map(Role::getAsMention).collect(Collectors.joining(",")))
                .addEmbeds(DiscordBot.Default.builder()
                        .setAuthor(stream.getUserName() + " ist jetzt live", streamUrl, imageUrl)
                        .setTitle(stream.getTitle(), streamUrl)
                        .addField("Game", stream.getGameName(), true)
                        .setImage(stream.getThumbnailUrl(1920, 1080))
                        .setColor(Color.MAGENTA)
                        .build())
                .addActionRow(Button.link(streamUrl, "Schau vorbei!"))
                .queue();


    }


}
