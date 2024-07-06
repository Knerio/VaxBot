package de.derioo.module.predefined.punishment;

import de.derioo.annotations.NeedsRole;
import de.derioo.config.Config;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.TimeUnit;

@Command(name = "kick")
public class KickCommand {

    @NeedsRole(Config.Id.Role.KICK_USER)
    @Execute
    public void execute(@Arg("user") @Description("Der User der gekickt werden soll") User user, @Arg("grund") @Description("Der Grund für den Kick") String reason, @Context SlashCommandInteractionEvent event) {
        Member member = event.getGuild().getMember(user);
        member.kick().reason(reason).queue();
        event.reply("Du hast den Nutzer erfolgreich gekicket").setEphemeral(true).queue();
    }

}
