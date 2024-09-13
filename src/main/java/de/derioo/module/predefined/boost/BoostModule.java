package de.derioo.module.predefined.boost;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.module.Module;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostTierEvent;

import java.awt.*;

public class BoostModule extends Module {
    public BoostModule(DiscordBot bot) {
        super(bot, "boost");
    }

    @ModuleListener
    public void onBoost(GuildMemberUpdateBoostTimeEvent event) {
        if (!event.getMember().isBoosting()) return;
        Long channelId = bot.get(event.getGuild()).getChannels().get(Config.Id.Channel.BOOST_CHANNEL.name());
        if (channelId == null) return;
        event.getGuild().getTextChannelById(channelId).sendMessageEmbeds(DiscordBot.Default.builder()
                        .setColor(Color.MAGENTA)
                        .setTitle("Varilx Boost")
                        .setDescription("Danke an " + event.getMember().getAsMention() + ", dass du **" + event.getGuild().getName() + " geboosted hast!")
                .build()).queue();
    }

}
