package de.derioo.module.predefined.notifier;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.module.Module;
import de.derioo.module.predefined.notifier.tiktok.TikTokNotifier;
import de.derioo.module.predefined.notifier.twitch.TwitchNotifier;
import de.derioo.module.predefined.notifier.youtube.YoutubeCreatorObject;
import de.derioo.module.predefined.notifier.youtube.YoutubeNotifier;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotifierModule extends Module {

    private YoutubeNotifier youtubeNotifier;

    public NotifierModule(DiscordBot bot) {
        super(bot, "notifier");
    }

    @Override
    public void once() throws Throwable {
        new TwitchNotifier(this, this.bot);
        new TikTokNotifier(this, this.bot);
        youtubeNotifier = new YoutubeNotifier(this, this.bot);
    }

    @ModuleListener
    public void onMessageReceive(MessageReceivedEvent event) {
        if (!event.getChannel().getType().isGuild()) return;
        if (!event.getMessage().getContentRaw().equals("!youtube reload")) return;
        if (!event.getGuild().getMemberById(event.getAuthor().getIdLong()).getPermissions().contains(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) return;

        event.getMessage().reply("Reloading").queue();
        List<YoutubeCreatorObject> ids = new ArrayList<>();
        ConfigData config = bot.get(event.getGuild());
        for (Object obj : config.getData(Config.Id.Data.YOUTUBE_NOTIFIER.name(), List.class)) {
            String name = (String) obj;
            try {
                ids.add(YoutubeCreatorObject.builder().name(name).id(youtubeNotifier.getYoutuberId(name)).build());
            } catch (IOException e) {
                Module.logThrowable(bot, e);
            }
        }
        config.putData(Config.Id.Data.YOUTUBE_NOTIFIER_IDS.name(), ids);
        this.bot.save(config);
    }
}
