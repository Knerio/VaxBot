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
import org.jetbrains.annotations.Async;

import java.util.concurrent.TimeUnit;

@Command(name = "ban")
@Description("Simpler Bann Command")
public class BanCommand {

    @NeedsRole(Config.Id.Role.BAN_USER)
    @Execute
    public void execute(@Arg("user") @Description("Der User der gebannt werden soll") User user, @Arg("grund") @Description("Der Grund für den Bann") String reason, @Context SlashCommandInteractionEvent event) {
        Member member = event.getGuild().getMember(user);
        member.ban(0, TimeUnit.SECONDS).reason(reason).queue();
        event.reply("Du hast den Nutzer erfolgreich gebannt").setEphemeral(true).queue();
    }

}
