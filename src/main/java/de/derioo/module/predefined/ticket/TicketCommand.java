package de.derioo.module.predefined.ticket;

import de.derioo.annotations.NeedsRole;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;

@Command(name = "ticket")
public class TicketCommand {

    private final DiscordBot bot;


    public TicketCommand(DiscordBot bot) {
        this.bot = bot;
    }

    @Execute(name = "adduser")
    @NeedsRole(Config.Id.Role.ADD_USER_TO_TICKET)
    public void add(@Arg("user") @Description("Der Nutzer, der hinzugefügt werden soll") User user, @Context SlashCommandInteractionEvent event) {
        Ticket ticket = validateChannel(event);
        if (ticket == null) return;
        Member member = event.getGuild().getMemberById(user.getIdLong());
        if (member.hasPermission(event.getGuildChannel(), Permission.VIEW_CHANNEL)) {
            event.reply("Dieser Nutzer kann bereits das Ticket sehen").setEphemeral(true).queue();
            return;
        }
        event.getChannel().asTextChannel().getManager().putPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null).queue();
        event.replyEmbeds(
                DiscordBot.Default
                        .builder()
                        .setTitle("Varilx.de | Ticket")
                        .setColor(Color.GREEN)
                        .setDescription(member.getAsMention() + " wurde von " + event.getUser().getAsMention() + " hinzugefügt")
                        .build()
        ).queue();
        member.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(DiscordBot.Default.builder()
                    .setTitle("Varilx.de | Ticket")
                    .setColor(Color.GREEN)
                    .setDescription("Du wurdest zu " + event.getChannel().getAsMention() + " hinzugefügt")
                    .build()).queue();
        });

        ticket.getHistory().add(Ticket.HistoryItem.builder()
                .senderId(null)
                .id(new ObjectId())
                .content(member.getEffectiveName() + " wurde zum Ticket " + event.getUser().getEffectiveName() + " hinzugefügt")
                .build());
        bot.getRepo(TicketRepo.class).save(ticket);

    }

    @Execute(name = "removeuser")
    @NeedsRole(Config.Id.Role.REMOVE_USER_FROM_TICKET)
    public void remove(@Arg("user") @Description("Der Nutzer, der entfernt werden soll") User user, @Context SlashCommandInteractionEvent event) {
        Ticket ticket = validateChannel(event);
        Member member = event.getGuild().getMemberById(user.getIdLong());
        if (ticket == null) return;
        if (!member.hasPermission(event.getGuildChannel(), Permission.VIEW_CHANNEL)) {
            event.reply("Dieser Nutzer kann das Ticket nicht sehen").setEphemeral(true).queue();
            return;
        }
        event.getChannel().asTextChannel().getManager().putPermissionOverride(member, null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
        event.replyEmbeds(
                DiscordBot.Default
                        .builder()
                        .setTitle("Varilx.de | Ticket")
                        .setColor(Color.GREEN)
                        .setDescription(member.getAsMention() + " wurde von " + event.getUser().getAsMention() + " entfernt")
                        .build()
        ).queue();
        member.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(DiscordBot.Default.builder()
                    .setTitle("Varilx.de | Ticket")
                    .setColor(Color.GREEN)
                    .setDescription("Du wurdest von einem Ticket entfernt entfernt")
                    .build()).queue();
        });
        ticket.getHistory().add(Ticket.HistoryItem.builder()
                .senderId(null)
                .id(new ObjectId())
                .content(member.getEffectiveName() + " wurde von " + event.getUser().getEffectiveName() + " entfernt")
                .build());
        bot.getRepo(TicketRepo.class).save(ticket);

    }

    @Nullable
    private Ticket validateChannel(SlashCommandInteractionEvent event) {
        Channel channel = event.getChannel();
        if (!channel.getType().isMessage()) {
            noTicketChannel(event);
            return null;
        }
        if (!channel.getName().contains("-")) {
            noTicketChannel(event);
            return null;
        }
        String id = List.of(channel.getName().split("-")).getLast();
        if (!ObjectId.isValid(id)) {
            noTicketChannel(event);
            return null;
        }
        ObjectId objectId = new ObjectId(id);
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(objectId);
        return ticket;
    }

    private void noTicketChannel(SlashCommandInteractionEvent event) {
        event.reply("Dies ist kein Ticket Kanal").setEphemeral(true).queue();
    }

}
