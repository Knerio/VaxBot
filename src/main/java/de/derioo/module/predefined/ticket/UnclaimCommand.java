package de.derioo.module.predefined.ticket;

import de.derioo.bot.DiscordBot;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.bson.types.ObjectId;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;

@Command(name = "unclaim")
@Description("Unclaimed ein Ticket")
public class UnclaimCommand {

    private final DiscordBot bot;

    public UnclaimCommand(DiscordBot bot) {
        this.bot = bot;
    }

    @Execute
    public void run(@Context SlashCommandInteractionEvent event) {
        Channel channel = event.getChannel();
        if (!channel.getType().isMessage()) {
            noTicketChannel(event);
            return;
        }
        Ticket ticket = getTicket(event.getChannelIdLong());
        if (ticket == null) {
            noTicketChannel(event);
            return;
        }
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (ticket.getClaimerId() == null || ticket.getClaimerId() != event.getUser().getIdLong()) {
                event.reply("Du kannst das Ticket nicht enclaimen").setEphemeral(true).queue();
                return;
            }
        }

        TextChannelManager manager = ((TextChannel) channel).getManager();
        for (Role role : bot.get(event.getGuild()).getRoleObjects(ticket.getType().getRole(), event.getGuild())) {
            System.out.println(role.getName());
            manager = manager.putPermissionOverride(role, EnumSet.of(VIEW_CHANNEL), null);
        }
        manager.queue();

        ticket.setClaimerId(null);
        ticket.getHistory().add(Ticket.HistoryItem.builder()
                .id(new ObjectId())
                .content("Das Ticket wurde von " + event.getUser().getAsMention() + " entclaimed")
                .build());
        bot.getRepo(TicketRepo.class).save(ticket);
        event.replyEmbeds(DiscordBot.Default.builder()
                .setTitle("Ticket wurde entclaimed")
                .setDescription("Das Ticket wurde von " + event.getUser().getAsMention() + " entclaimed")
                .setColor(Color.GREEN)
                .build()).queue();
    }

    private void noTicketChannel(SlashCommandInteractionEvent event) {
        event.reply("Dies ist kein Ticket Kanal").setEphemeral(true).queue();
    }


    private Ticket getTicket(Long channelId) {
        for (Ticket ticket : bot.getRepo(TicketRepo.class).findAll()) {
            if (Objects.equals(ticket.getChannelId(), channelId)) return ticket;
        }
        return null;
    }
}
