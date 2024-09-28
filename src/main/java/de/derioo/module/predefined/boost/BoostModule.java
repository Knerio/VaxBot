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
                        .setThumbnail("https://cdn.discordapp.com/attachments/1055223755909111808/1132753302182961222/Undassdabedsddsdsadsanannt-1.png?ex=66f50a78&is=66f3b8f8&hm=8b3b09d696ff282620dffd696966ce5362c48394651b235e566a293ac651b8f3&")
                        .setDescription("Danke an " + event.getMember().getAsMention() + ", dass du **" + event.getGuild().getName() + "** geboosted hast!")
                .build()).queue();
    }

}
