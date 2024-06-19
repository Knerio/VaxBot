package de.derioo.module.predefined.ticket;

import de.derioo.bot.DiscordBot;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bson.types.ObjectId;

import java.util.List;

@Command(name = "unclaim")
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
        if (!channel.getName().contains("-")) {
            noTicketChannel(event);
            return;
        }
        String id = List.of(channel.getName().split("-")).getLast();
        if (!ObjectId.isValid(id)) {
            noTicketChannel(event);
            return;
        }
        ObjectId objectId = new ObjectId(id);
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(objectId);
        if (ticket.getClaimerId() != null && ticket.getClaimerId() != event.getUser().getIdLong()) {
            event.reply("Du kannst das Ticket nicht enclaimen").setEphemeral(true).queue();
            return;
        }
        ticket.setClaimerId(null);
        bot.getRepo(TicketRepo.class).save(ticket);
        event.reply("Du hast das Ticket enclaimed").setEphemeral(true).queue();
    }

    private void noTicketChannel(SlashCommandInteractionEvent event) {
        event.reply("Dies ist kein Ticket Kanal").setEphemeral(true).queue();
    }

}
