package de.derioo.module.predefined.log;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.javautils.common.StringUtility;
import de.derioo.module.Module;
import de.derioo.utils.UserUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildTimeoutEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import org.eclipse.jetty.util.StringUtil;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class LogModule extends Module {
    public LogModule(DiscordBot bot) {
        super(bot, "log");
    }

    @ModuleListener
    public void onButton(ButtonInteractionEvent event) {
        if (event.getButton().getId() == null) return;
        switch (event.getButton().getId()) {
            case "delete-message" -> {
                event.getMessage().delete().queue();
            }
        }
    }

    @ModuleListener
    public void onUsernameChange(UserUpdateNameEvent event) {
        log(event.getEntity(), UserUtils.getMention(event.getEntity()) + " hat seinen Usernamen geändert (" + event.getOldName() + " -> " + event.getNewName() + ")");
    }

    @ModuleListener
    public void onNickNameChange(GuildMemberUpdateNicknameEvent event) {
        log(event.getEntity(), UserUtils.getMention(event.getEntity()) + " hat seinen Nickname geändert (" + event.getOldNickname() + " -> " + event.getNewNickname() + ")");
    }

    @ModuleListener
    public void onTimeout(GuildMemberUpdateTimeOutEvent event) {
        log((User) null, DiscordBot.Default.builder()
                .setTitle("User getimeouted")
                .addField("Beschreibung",
                        UserUtils.getMention(event.getUser()) + " wurde getimeouted", false)
                .setColor(Color.GREEN)
                .build());

    }


    @ModuleListener
    public void onNickNameChange(GuildBanEvent event) {
        event.getGuild().retrieveAuditLogs()
                .type(ActionType.BAN).queueAfter(1, TimeUnit.SECONDS, entries -> {
                    for (AuditLogEntry entry : entries) {
                        if (!entry.getTargetId().equals(event.getUser().getId())) continue;
                        log((Member) null, DiscordBot.Default.builder()
                                .setTitle("User gebannt")
                                .addField("Beschreibung",
                                        UserUtils.getMention(event.getUser()) + " wurde von " +
                                        UserUtils.getMention(event.getGuild().getMemberById(entry.getTargetId())) + " gebannt", false)
                                .setColor(Color.GREEN)
                                .build());
                        break;
                    }
                });
    }

    @ModuleListener
    public void onMessageEdit(MessageUpdateEvent event) {
        if (event.getMember() == null) return;
        if (event.getMember().getUser().isBot()) return;
        if (!event.getMessage().isEdited()) return;
        log(event.getMember(), DiscordBot.Default.builder()
                .setTitle("Nachricht bearbeitet")
                .addField("Neue Nachricht", StringUtility.capAtNCharacters(event.getMessage().getContentRaw(), 1023), false)
                .addField("Channel", event.getChannel().getAsMention(), false)
                .addField("User", UserUtils.getMention(event.getMember()), false)
                .setColor(Color.GREEN)
                .build());
    }

    public void log(Member user, MessageEmbed embed) {
        log(user.getUser(), embed);
    }

    public void log(User user, MessageEmbed embed) {
        for (Guild guild : this.bot.getJda().getGuilds()) {
            Long channelId = this.bot.get(guild)
                    .getChannels().get(Config.Id.Channel.LOG_CHANNEL.name());
            TextChannel channel = guild.getTextChannelById(channelId);
            channel.sendMessageEmbeds(embed).queue();
        }

    }

    public void log(Member user, String message) {
        log(user.getUser(), message);
    }

    public void log(User user, String message) {
        log(user, DiscordBot.Default.builder()
                .addField("Beschreibung", message, false)
                .addField("User", UserUtils.getMention(user), false)
                .setColor(Color.GREEN)
                .build());
    }

}
