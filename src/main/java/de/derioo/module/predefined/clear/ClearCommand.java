package de.derioo.module.predefined.clear;

import de.derioo.annotations.NeedsRole;
import de.derioo.config.Config;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@Command(name = "clear")
public class ClearCommand {

    @Execute
    @Description("Löscht die x neusten Nachrichten im aktuellen Channel")
    @NeedsRole(Config.Id.Role.CLEAR)
    void executeClear(@Arg("menge") @Description("Wie viele Nachrichten sollen gelöscht werden?") int amount, @Context @NotNull SlashCommandInteractionEvent event) {
        if (amount <= 0) {
            event.reply("Bitte gib eine positive Zahl an.").setEphemeral(true).queue();
            return;
        }
        event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
            for (Message message : messages) {
                message.delete().queue();
            }
            event.reply("Du hast " + amount + " Nachrichten gelöscht.").setEphemeral(true).queue();
        });
    }

}
