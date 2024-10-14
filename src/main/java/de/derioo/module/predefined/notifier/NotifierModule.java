package de.derioo.module.predefined.notifier;

import de.derioo.bot.DiscordBot;
import de.derioo.module.Module;
import de.derioo.module.predefined.notifier.tiktok.TikTokNotifier;
import de.derioo.module.predefined.notifier.twitch.TwitchNotifier;
import de.derioo.module.predefined.notifier.youtube.YoutubeNotifier;

public class NotifierModule extends Module {
    public NotifierModule(DiscordBot bot) {
        super(bot, "notifier");
    }

    @Override
    public void once() throws Throwable {
        new TwitchNotifier(this, this.bot);
        new TikTokNotifier(this, this.bot);
        new YoutubeNotifier(this, this.bot);
    }
}
