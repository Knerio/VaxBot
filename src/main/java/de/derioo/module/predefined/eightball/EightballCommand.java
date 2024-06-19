package de.derioo.module.predefined.eightball;

import de.derioo.bot.DiscordBot;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Command(name = "8ball")
public class EightballCommand {

    private final List<String> replies = new ArrayList<>(List.of("Ja", "Nein", "Vielleicht...", "Sehr wahrscheinlich", "Wahrscheinlich nicht...", "Auf jeden Fall!", "Nein, niemals!", "Woher soll ich das wissen?"));

    @Execute
    public void ask(@Arg("frage") @Description("Deine Frage bzw. Nachricht") String message, @Context SlashCommandInteractionEvent event) {
        Collections.shuffle(replies);
        event.replyEmbeds(DiscordBot.Default.builder()
                        .setTitle(event.getUser().getEffectiveName())
                        .setDescription("Frage: **" + message + "**\n\nAntwort: **" + replies.getFirst() + "**")
                        .setColor(Color.CYAN)
                .build()).queue();
    }

}
