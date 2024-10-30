package de.derioo.module.predefined.punishment;

import de.derioo.annotations.NeedsRole;
import de.derioo.config.Config;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.join.Join;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Duration;

@Command(name = "timeout")
@Description("Ein simpler Timeout Command")
public class TimeoutCommand {


    @NeedsRole(Config.Id.Role.TIMEOUT_USER)
    @Execute
    public void execute(@Arg("user") @Description("Der User der getimeouted werden soll") User user, @Join("grund") @Description("Der Grund für den timeout") String reason, @Arg("dauer") @Description("Die Dauer des Timeouts") Duration duration, @Context SlashCommandInteractionEvent event) {
        Member member = event.getGuild().getMember(user);
        member.timeoutFor(duration).reason(reason).queue();
        event.reply("Du hast den Nutzer erfolgreich getimeouted").setEphemeral(true).queue();
    }


}
