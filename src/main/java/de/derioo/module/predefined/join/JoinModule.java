package de.derioo.module.predefined.join;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.awt.*;
import java.util.List;

public class JoinModule extends Module {

    private final DiscordBot bot;

    public JoinModule(DiscordBot bot) {
        super(bot, "join");
        this.bot = bot;
    }

    @ModuleListener
    public void onUserJoin(GuildMemberJoinEvent event) {
        ConfigData configData = bot.get(event.getGuild());
        Long l = configData.getChannels().get(Config.Id.Channel.JOIN_CHANNEL.name());
        event.getGuild().getTextChannelById(l).sendMessageEmbeds(DiscordBot.Default.builder()
                        .setDescription("Hey " + event.getUser().getAsMention() + " willkommen auf \n**" + event.getGuild().getName() + "**\n" +
                                "\n" +
                                "Ich Bitte dich das Regelwerk durchzulesen damit keine Unannehmlichkeiten enstehen.\n" +
                                "Wir danken dir und viel Spaß!\n" +
                                "\n" +
                                "\n" +
                                "\n" + "Unser Host Partner »\n" + "https://discord.gg/tube-hosting -> <:partner:1140656134504599552>")
                        .setThumbnail("https://cdn.discordapp.com/attachments/1055223755909111808/1160508092757319740/Unbedasasdasdasddsasdasdadsdadasadnannt.png?ex=668bb062&is=668a5ee2&hm=837e1757017c78cf2a1aac5cd95a61cc5a84f6e01f27853f0726941361dd3683&")
                        .setColor(Color.GREEN)
                        .setImage("https://cdn.discordapp.com/attachments/1055223755909111808/1160507955507101736/Varilx_Tube-hosting_version.png?ex=668bb041&is=668a5ec1&hm=f82784ef44a4d5f7eff42cc85e8cc032b1dda100781be165a604edaf6e78f858&")
                .build()).queue();
        List<Role> roleObjects = configData.getRoleObjects(Config.Id.Role.PLAYER_ROLE, event.getGuild());
        for (Role roleObject : roleObjects) {
            event.getGuild().addRoleToMember(event.getUser(), roleObject).queue();
        }
    }

}
