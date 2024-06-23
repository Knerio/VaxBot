package de.derioo.module.predefined.stafflist;

import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.module.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class StafflistModule extends Module {

    private final DiscordBot bot;

    public StafflistModule(DiscordBot bot) {
        super(bot, "stafflist");
        this.bot = bot;
    }

    @Override
    public void timer() throws Throwable {
        for (Guild guild : bot.getJda().getGuilds()) {
            Long channelId = bot.get(guild).getChannels().get(Config.Id.Channel.STAFFLIST_CHANNEL.name());
            if (channelId == null) continue;
            TextChannel channel = guild.getTextChannelById(channelId);
            channel.getHistory().retrievePast(1).queue(messages -> {
                EmbedBuilder embed =
                        DiscordBot.Default.builder()
                                .setColor(Color.GREEN)
                                .setTitle("Varilx Team Liste");
                List<Long> data = bot.get(guild).getData(Config.Id.Data.TEAM_ROLE.name(), List.class);
                if (data == null) new ArrayList<>();

                StringBuilder desc = new StringBuilder();

                for (Long roleId : data) {
                    Role role = guild.getRoleById(roleId);
                    List<Member> members = guild.getMembersWithRoles(role);
                    desc
                            .append("➥ ")
                            .append(role.getAsMention())
                            .append(" (")
                            .append(members.size())
                            .append(") \n")
                            .append(members.stream().map(IMentionable::getAsMention).collect(Collectors.joining(",")));
                }
                embed.setDescription(desc.toString());

                if (messages.isEmpty() || messages.getFirst().getAuthor().getIdLong() != bot.getJda().getSelfUser().getIdLong()) {
                    channel.sendMessageEmbeds(embed.build()).queue();
                } else {
                    messages.getFirst().editMessageEmbeds(embed.build()).queue();
                }
            });
        }
    }
}
