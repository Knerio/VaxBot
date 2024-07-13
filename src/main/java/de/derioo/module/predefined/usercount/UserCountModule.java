package de.derioo.module.predefined.usercount;

import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import de.derioo.utils.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class UserCountModule extends Module {

    public UserCountModule(DiscordBot bot) {
        super(bot, "usercount");
    }

    @Override
    public void timer() throws Throwable {
        for (Guild guild : bot.getJda().getGuilds()) {
            ConfigData data = bot.get(guild);
            Long userCountChannelId = data.getChannels().getOrDefault(Config.Id.Channel.USER_COUNT_CHANNEL.name(), null);
            if (userCountChannelId == null) continue;
            GuildChannel channel = guild.getGuildChannelById(userCountChannelId);
            channel.getManager().setName(Emote.PUSH_PIN.getData() + "・ | User: " + guild.getMembers().size()).queue();
        }
    }
}
